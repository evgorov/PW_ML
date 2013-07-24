package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;

public class PuzzleView extends View
{
    private static final int DEFAULT_CELL_WIDTH = 14;
    private static final int DEFAULT_CELL_HEIGHT = 20;
    private static final int DEFAULT_PADDING = 16;
    private static final int DEFAULT_TILE_GAP = 4;

    private int mCanvasWidth;
    private int mCanvasHeight;
    private int mViewWidth;
    private int mViewHeight;

    private int mPuzzleCellWidth = DEFAULT_CELL_WIDTH;
    private int mPuzzleCellHeigth = DEFAULT_CELL_HEIGHT;
    private int mPadding = DEFAULT_PADDING;
    private int mTileGap = DEFAULT_TILE_GAP;

    private @Nonnull Context mContext;

    private @Nonnull Matrix mMatrix;
    private @Nonnull RectF mViewRect;
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
        initCanvasDimensions();
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

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
        Log.i("viewW: " + mViewWidth + " viewH: " + mViewHeight);
        Log.i("canvasW: " + mCanvasWidth + " canvasH: " + mCanvasHeight);
        Log.i("SW: " + scaleWidth + " SH: " + scaleHeight);
        float minWidth = 1/scaleWidth;
        float minHeight = 1/scaleHeight;
        Log.i("mW: " + minWidth + " mH: " + minHeight);
        mMinScaleFactor = ((int)(Math.min(minWidth, minHeight)/0.1f)) * 0.1f;
        Log.i("top: " + getTop() + " left: " + getLeft() + " right: " + getRight() + " bottom: " + getBottom());

        mViewRect = new RectF(getLeft(), getTop(), getRight(), getBottom());
        initCanvasDimensions();
        super.onSizeChanged(w, h, oldw, oldh);
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

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int saveCount = canvas.getSaveCount();
        canvas.save();

        configureBounds();
        canvas.setMatrix(mMatrix);

//        canvas.translate(mViewRect.left, mViewRect.top);
        mBackgroundLayer.drawLayer(canvas);
        mQuestionsAndLettersLayer.drawLayer(canvas);

//        mBackgroundLayer.recycle();
//        mQuestionsAndLettersLayer.recycle();

        canvas.restoreToCount(saveCount);
    }

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
    }

    private void configureBounds()
    {
        mMatrix.reset();
        mMatrix.postScale(mScaleFactor, mScaleFactor);
        mMatrix.postTranslate((int)((mViewWidth - mCanvasWidth * mScaleFactor) * 0.5f  + 0.5f),
                         (int) ((mViewHeight - mCanvasHeight * mScaleFactor) * 0.5f + 0.5f));

    }

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

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(mMinScaleFactor, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

            invalidate();
            return true;
        }
    }
}
