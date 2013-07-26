package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleView extends View
{

    private @Nonnull Context mContext;

    private @Nonnull Canvas mPuzzleCanvas;
    private @Nonnull Matrix mMatrix;
    private @Nonnull Rect mViewRect;
    private @Nonnull Paint mPaint;

    private int mCanvasWidth;
    private int mCanvasHeight;
    private @Nullable Bitmap mCanvasBitmap;
    private @Nullable Bitmap mBackgroundTileBitmap;

    private int mViewWidth;
    private int mViewHeight;

    private @Nonnull PuzzleBackgroundLayer mBackgroundLayer;
    private @Nonnull PuzzleTilesLayer mQuestionsAndLettersLayer;

    private @Nonnull ScaleGestureDetector mScaleDetector;

    private static final float MIN_SCALE_FACTOR = 0.5f;
    private static final float MAX_SCALE_FACTOR = 1.0f;
    private float mScaleFactor = MAX_SCALE_FACTOR;
    private float mMinScaleFactor = MIN_SCALE_FACTOR;

    private @Nullable PuzzleViewInformation mPuzzleInfo;

    public PuzzleView(Context context)
    {
        this(context, null);
    }

    public PuzzleView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PuzzleView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setBackgroundTileBitmap(int resId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        mBackgroundTileBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    public void setPuzzleInfo(@Nullable PuzzleViewInformation puzzleInfo)
    {
        mPuzzleInfo = puzzleInfo;
        initCanvasDimensions();
        invalidate(mViewRect);
    }

    // ==== measuring ====

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = mCanvasWidth;
        int height = mCanvasHeight;

        mViewWidth = resolveSize(width, widthMeasureSpec);
        mViewHeight = resolveSize(height, heightMeasureSpec);

        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mViewRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // ==== canvas initiation and pre drawing ====

    private void initCanvasDimensions()
    {
        if (mPuzzleInfo == null)
        {
            return;
        }

        int padding = mPuzzleInfo.getPadding();
        int framePadding = mPuzzleInfo.getFramePadding();
        int cellWidth = mPuzzleInfo.getPuzzleCellWidth();
        int cellHeight = mPuzzleInfo.getPuzzleCellHeight();
        int tileGap = mPuzzleInfo.getTileGap();

        // init questions layer
        mQuestionsAndLettersLayer = new PuzzleTilesLayer(mContext.getResources(), cellWidth, cellHeight);
        mQuestionsAndLettersLayer.initBitmaps(mPuzzleInfo.getLetterEmpty(),
                                            mPuzzleInfo.getLetterEmptyInput(),
                                            mPuzzleInfo.getOverlayLetterCorrect(),
                                            mPuzzleInfo.getQuestionEmpty(),
                                            mPuzzleInfo.getQuestionInput(),
                                            mPuzzleInfo.getQuestionWrong(),
                                            mPuzzleInfo.getQuestionCorrect());
        mQuestionsAndLettersLayer.setPadding(padding + framePadding);
        mQuestionsAndLettersLayer.setTileGap(tileGap);
        mQuestionsAndLettersLayer.setStateMatrix(mPuzzleInfo.getStateMatrix());
        mQuestionsAndLettersLayer.setQuestions(mPuzzleInfo.getPuzzleQuestions());

        // compute canvas size
        int tileWidth = mQuestionsAndLettersLayer.getTileWidth();
        int tileHeigth = mQuestionsAndLettersLayer.getTileHeight();
        mCanvasWidth = 2 * (padding + framePadding) + cellWidth * tileWidth + (cellWidth - 1) * tileGap;
        mCanvasHeight = 2 * (padding + framePadding) + cellHeight * tileHeigth + (cellHeight - 1) * tileGap;

        // init background layer
        mBackgroundLayer = new PuzzleBackgroundLayer(mContext.getResources(), mCanvasWidth, mCanvasHeight,
                mPuzzleInfo.getBackgroundTile(), mPuzzleInfo.getBackgroundFrame(), framePadding);

        // init drawing canvas
        mCanvasBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
        mPuzzleCanvas = new Canvas(mCanvasBitmap);
        mBackgroundLayer.drawLayer(mPuzzleCanvas);
        mQuestionsAndLettersLayer.drawLayer(mPuzzleCanvas);

        // compute scale factor
        float scaleWidth = (float)mCanvasWidth/(float)mViewWidth;
        float scaleHeight = (float)mCanvasHeight/(float)mViewHeight;
        float minWidth = 1/scaleWidth;
        float minHeight = 1/scaleHeight;
        mMinScaleFactor = ((int)(Math.min(minWidth, minHeight)/0.1f)) * 0.1f;
    }

    // ==== drawing, scaling, translating ====

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawBackground(canvas);

        int saveCount = canvas.getSaveCount();
        canvas.save();

        configureBounds();
        canvas.concat(mMatrix);

        drawPuzzle(canvas);

        canvas.restoreToCount(saveCount);
    }

    private void drawBackground(Canvas canvas)
    {
        if (mBackgroundTileBitmap != null)
        {
            PuzzleBackgroundLayer.fillBackgroundWithTile(canvas, mBackgroundTileBitmap, mPaint);
        }
        else
        {
            canvas.drawColor(Color.DKGRAY);
        }
    }

    private void drawPuzzle(Canvas canvas)
    {
        if (mCanvasBitmap != null)
        {
            canvas.drawBitmap(mCanvasBitmap, 0, 0, mPaint);
        }
    }

    private void configureBounds()
    {
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postTranslate((int)((mViewWidth - mCanvasWidth * mScaleFactor) * 0.5f  + 0.5f),
                         (int) ((mViewHeight - mCanvasHeight * mScaleFactor) * 0.5f + 0.5f));
    }

    public void recycle()
    {
        mCanvasBitmap.recycle();
        if (mBackgroundTileBitmap != null)
        {
            mBackgroundTileBitmap.recycle();
        }
        mBackgroundLayer.recycle();
        mQuestionsAndLettersLayer.recycle();
    }

    // ==== touch events ======

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mScaleDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(mMinScaleFactor, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

            invalidate(mViewRect);
            return true;
        }
    }

}
