package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;

import org.omich.velo.handlers.IListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleView extends View
{
    private @Nonnull Context mContext;
    private @Nullable Rect mViewScreenRect;
    private @Nullable PuzzleManager mPuzzleManager;

    private @Nonnull GestureDetector mGestureDetector;
    private @Nonnull ScaleGestureDetector mScaleGestureDetector;
    private static final float MIN_SCALE_FACTOR_DETECTABLE = 0.2f;
    private boolean mScaled = true;

    private @Nonnull PuzzleResourcesAdapter mAdapter;


    public PuzzleView(Context context)
    {
        this(context, null);
    }

    public PuzzleView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PuzzleView(@Nonnull Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setAdapter(@Nonnull PuzzleResourcesAdapter adapter)
    {
        recycle();
        mAdapter = adapter;
        mPuzzleManager = new PuzzleManager(mContext, mAdapter, new IListener<Rect>()
        {
            @Override
            public void handle(@Nullable Rect rect)
            {
                if (rect != null)
                {
                    invalidate(rect);
                }
            }
        });
        mAdapter.addResourcesUpdater(new IListener<PuzzleResources>()
        {
            @Override
            public void handle(@Nullable PuzzleResources puzzleResources)
            {
                if (mViewScreenRect != null && mPuzzleManager != null)
                {
                    mPuzzleManager.setPuzzleViewRect(mViewScreenRect);
                }
            }
        });

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mViewScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        if (mPuzzleManager != null)
        {
            mPuzzleManager.recycle();
            mPuzzleManager.setPuzzleViewRect(mViewScreenRect);
        }
        invalidate(mViewScreenRect);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas == null)
            return;
        if (mPuzzleManager != null)
        {
            mPuzzleManager.drawPuzzle(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(@Nonnull MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    public void recycle()
    {
        if (mPuzzleManager != null)
        {
            mPuzzleManager.recycle();
            mPuzzleManager = null;
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if (mPuzzleManager != null && mViewScreenRect != null)
            {
                mPuzzleManager.onScrollEvent(distanceX, distanceY);
                invalidate(mViewScreenRect);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            if (mPuzzleManager != null && mViewScreenRect != null)
            {
                mPuzzleManager.onScaleEvent(PuzzleView.this);
                mScaled = !mScaled;
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            PointF p = new PointF(e.getX(), e.getY());
            if (mPuzzleManager != null && mViewScreenRect != null)
            {
                mPuzzleManager.onTapEvent(p);
            }
            return true;
        }

    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            float scaleFactor = detector.getScaleFactor();
            if(scaleFactor >= 1 + MIN_SCALE_FACTOR_DETECTABLE && !mScaled)
            {
                if (mPuzzleManager != null && mViewScreenRect != null)
                {
                    mPuzzleManager.onScaleEvent(PuzzleView.this);
                    mScaled = true;
                }
                return true;
            }
            if(scaleFactor <= 1 - MIN_SCALE_FACTOR_DETECTABLE && mScaled)
            {
                if (mPuzzleManager != null && mViewScreenRect != null)
                {
                    mPuzzleManager.onScaleEvent(PuzzleView.this);
                    mScaled = false;
                }
                return true;
            }
            return false;
        }
    }

}
