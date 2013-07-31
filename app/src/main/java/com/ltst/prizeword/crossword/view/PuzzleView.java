package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.ltst.prizeword.crossword.engine.PuzzleResources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleView extends View
{
    private @Nonnull Context mContext;
    private @Nullable Rect mViewScreenRect;
    private @Nullable PuzzleManager mPuzzleManager;

    private @Nonnull GestureDetector mGestureDetector;


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
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mViewScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        if (mPuzzleManager != null)
        {
            mPuzzleManager.setPuzzleViewRect(mViewScreenRect);
        }
        invalidate(mViewScreenRect);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void initializePuzzle(@Nonnull PuzzleResources info)
    {
        if (mViewScreenRect != null)
        {
            mPuzzleManager = new PuzzleManager(mContext, info, mViewScreenRect);
            invalidate(mViewScreenRect);
        }
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
        return  mGestureDetector.onTouchEvent(event);
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
            if (mPuzzleManager != null)
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
            if (mPuzzleManager != null)
            {
                mPuzzleManager.onScaleEvent();
                invalidate(mViewScreenRect);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            return super.onSingleTapConfirmed(e);
        }
    }

}
