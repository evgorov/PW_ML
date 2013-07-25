package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

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

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleView extends View
{
    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;

    private int mPuzzleCellWidth = DEFAULT_CELL_WIDTH;
    private int mPuzzleCellHeigth = DEFAULT_CELL_HEIGHT;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;

    private @Nonnull Context mContext;

    private @Nonnull Canvas mPuzzleCanvas;
    private @Nonnull Matrix mMatrix;
    private @Nonnull Rect mViewRect;
    private @Nonnull Paint mPaint;

    private int mCanvasWidth;
    private int mCanvasHeight;
    private @Nonnull Bitmap mCanvasBitmap;
    private @Nullable Bitmap mBackgroundTileBitmap;

    private int mViewWidth;
    private int mViewHeight;

    private @Nonnull PuzzleBackgroundLayer mBackgroundLayer;
    private @Nonnull PuzzleTilesLayer mQuestionsAndLettersLayer;

    private @Nonnull ScaleGestureDetector mScaleDetector;

    private static final float MIN_SCALE_FACTOR = 0.5f;
    private static final float MAX_SCALE_FACTOR = 1.2f;
    private float mScaleFactor = MAX_SCALE_FACTOR;
    private float mMinScaleFactor = MIN_SCALE_FACTOR;

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
        initCanvasDimensions();
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setPuzzleCellWidth(int puzzleCellWidth)
    {
        mPuzzleCellWidth = puzzleCellWidth;
        initCanvasDimensions();
    }

    public void setPuzzleCellHeigth(int puzzleCellHeigth)
    {
        mPuzzleCellHeigth = puzzleCellHeigth;
        initCanvasDimensions();
    }

    public void setBackgroundTileBitmap(int resId)
    {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
        mBackgroundTileBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
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
        float scaleWidth = (float)mCanvasWidth/(float)mViewWidth;
        float scaleHeight = (float)mCanvasHeight/(float)mViewHeight;
        float minWidth = 1/scaleWidth;
        float minHeight = 1/scaleHeight;
        mMinScaleFactor = ((int)(Math.min(minWidth, minHeight)/0.1f)) * 0.1f;

        mViewRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        initCanvasDimensions();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // ==== canvas initiation and pre drawing ====

    private void initCanvasDimensions()
    {
        mQuestionsAndLettersLayer = new PuzzleTilesLayer(mContext.getResources(), mPuzzleCellWidth, mPuzzleCellHeigth);
        int tileWidth = mQuestionsAndLettersLayer.getTileWidth();
        int tileHeigth = mQuestionsAndLettersLayer.getTileHeight();
        mCanvasWidth = 2 * mPadding + mPuzzleCellWidth * tileWidth + (mPuzzleCellWidth - 1) * mTileGap;
        mCanvasHeight = 2 * mPadding + mPuzzleCellHeigth * tileHeigth + (mPuzzleCellHeigth - 1) * mTileGap;

        mQuestionsAndLettersLayer.setPadding(mPadding);
        mQuestionsAndLettersLayer.setTileGap(mTileGap);
        mBackgroundLayer = new PuzzleBackgroundLayer(mContext.getResources(), mCanvasWidth, mCanvasHeight, R.drawable.bg_sand_tile2x);

        mCanvasBitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
        mPuzzleCanvas = new Canvas(mCanvasBitmap);
        mBackgroundLayer.drawLayer(mPuzzleCanvas);
        mQuestionsAndLettersLayer.drawLayer(mPuzzleCanvas);
    }

    // ==== drawing, scaling, traslating ====

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Log.i("w: " + canvas.getWidth() + " h: " + canvas.getHeight());
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
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mPaint);
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
