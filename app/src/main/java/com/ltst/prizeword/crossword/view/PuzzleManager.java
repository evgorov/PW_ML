package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;

import com.ltst.prizeword.crossword.engine.PuzzleFieldDrawer;
import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;

import org.omich.velo.handlers.IListener;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    private @Nullable Point mFocusViewPoint;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleFieldDrawer mFieldDrawer;
    private @Nonnull Matrix mMatrix;
    private @Nonnull Rect mPuzzleViewRect;
    private @Nullable Rect mScaledViewRect;

    private float MIN_SCALE;
    private float MAX_SCALE = 1.0f;
    private boolean mScaled;
    private float mCurrentScale = MAX_SCALE;
    private boolean mIsAnimating;

    public PuzzleManager(@Nonnull Context context,
                         @Nonnull PuzzleResourcesAdapter adapter,
                         @Nonnull IListener<Rect> invalidateHandler)
    {
        mContext = context;
        mMatrix = new Matrix();
        mFieldDrawer = new PuzzleFieldDrawer(context, adapter, invalidateHandler);
    }

    public void setPuzzleViewRect(@Nonnull Rect puzzleViewRect)
    {
        mPuzzleViewRect = puzzleViewRect;
        float scaleWidth = (float)mPuzzleViewRect.width()/(float)mFieldDrawer.getWidth();
        float scaleHeight = (float)mPuzzleViewRect.height()/(float)mFieldDrawer.getHeight();
        MIN_SCALE = Math.min(scaleHeight, scaleWidth);
        mFieldDrawer.enableScaling(1/scaleHeight, 1/scaleWidth);
        mScaled = true;

        mScaledViewRect = new Rect(0, 0,
                mFieldDrawer.getActualWidth(),
                mFieldDrawer.getActualHeight());
        mFocusViewPoint = new Point(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
    }

    public void onScrollEvent(float offsetX, float offsetY)
    {
        if(mIsAnimating || mScaledViewRect == null || mFocusViewPoint == null)
            return;
        mFocusViewPoint.x += offsetX;
        mFocusViewPoint.y += offsetY;
        mFieldDrawer.checkFocusPoint(mFocusViewPoint, mScaled ? mPuzzleViewRect : mScaledViewRect);
    }

    public void onScaleEvent(@Nonnull View view)
    {
        if(mIsAnimating || mScaledViewRect == null || mFocusViewPoint == null)
            return;
        ScaleAnimationThread anim = null;
        if(mScaled)
            anim = new ScaleAnimationThread(view, MAX_SCALE, MIN_SCALE);
        else
            anim = new ScaleAnimationThread(view, MIN_SCALE, MAX_SCALE);
        anim.start();

        mScaled = !mScaled;
//        mFocusViewPoint.set(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
    }

    private void configureMatrix()
    {
        if (mFocusViewPoint == null)
        {
            return;
        }
        mMatrix.reset();

        float translateX = (mFocusViewPoint.x - mPuzzleViewRect.width()/2/mCurrentScale);
        float translateY = (mFocusViewPoint.y- mPuzzleViewRect.height()/2/mCurrentScale);
        mMatrix.postTranslate(-translateX, -translateY);

        mMatrix.postScale(mCurrentScale, mCurrentScale);

    }

    public void drawPuzzle(@Nonnull Canvas screenCanvas)
    {
        int saveCount = screenCanvas.getSaveCount();
        screenCanvas.save();

        configureMatrix();
        screenCanvas.concat(mMatrix);

        mFieldDrawer.drawBackground(screenCanvas);
        mFieldDrawer.drawPuzzles(screenCanvas);

        screenCanvas.restoreToCount(saveCount);
    }


    public void recycle()
    {
        mFieldDrawer.unloadResources();
    }

    private class ScaleAnimationThread extends Thread
    {
        private static final long FPS_INTERVAL = 1000 / 60;
        static final float ANIMATION_SCALE_PER_ITERATION_IN = 1.1f;
        static final float ANIMATION_SCALE_PER_ITERATION_OUT = 0.9f;

        private final @Nonnull View view;
        private final float fromZoom;
        private final float toZoom;
        private final float mDeltaScale;

        private ScaleAnimationThread(@Nonnull View view, float fromZoom, float toZoom)
        {
            this.toZoom = toZoom;
            this.fromZoom = fromZoom;
            this.view = view;
            if (fromZoom < toZoom) {
                mDeltaScale = ANIMATION_SCALE_PER_ITERATION_IN;
            } else {
                mDeltaScale = ANIMATION_SCALE_PER_ITERATION_OUT;
            }
        }

        @Override
        public void run()
        {
            mIsAnimating = true;
            while((mDeltaScale > 1f && mCurrentScale < toZoom)
                    || (mDeltaScale < 1f && toZoom < mCurrentScale))
            {
                mCurrentScale *= mDeltaScale;

                view.postInvalidate(mPuzzleViewRect.left, mPuzzleViewRect.top,
                        mPuzzleViewRect.right, mPuzzleViewRect.bottom);
                try
                {
                    ScaleAnimationThread.sleep(FPS_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    Log.e(e.getMessage());
                }
            }
            final float delta = toZoom / mCurrentScale;
            mCurrentScale *= delta;
            view.postInvalidate(mPuzzleViewRect.left, mPuzzleViewRect.top,
                    mPuzzleViewRect.right, mPuzzleViewRect.bottom);
            mIsAnimating = false;

        }
    }

}
