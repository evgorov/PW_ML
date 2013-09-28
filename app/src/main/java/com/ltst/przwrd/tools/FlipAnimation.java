package com.ltst.przwrd.tools;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;


import com.ltst.przwrd.R;

import javax.annotation.Nonnull;

public class FlipAnimation extends Animation
{
    private Camera mCamera;
    private @Nonnull View mTopView;
    private @Nonnull View mBottomView;
    private @Nonnull View mMiddleView;
    private float centerX;
    private float centerY;
    int id;
    int[] drawTop;
    int[] drawBottom;
    int mCountIter;

    public FlipAnimation(@Nonnull View nextView, @Nonnull View beforeView, @Nonnull View midView, int id)
    {
        mTopView = nextView;
        mBottomView = beforeView;
        mMiddleView = midView;
        this.id = id;
        setDuration(300);
        setFillAfter(false);
        drawTop = new int[]{R.drawable.final_flip_clock_bg_top_0, R.drawable.final_flip_clock_bg_top_1,
                R.drawable.final_flip_clock_bg_top_2, R.drawable.final_flip_clock_bg_top_3,
                R.drawable.final_flip_clock_bg_top_4, R.drawable.final_flip_clock_bg_top_5,
                R.drawable.final_flip_clock_bg_top_6, R.drawable.final_flip_clock_bg_top_7,
                R.drawable.final_flip_clock_bg_top_8, R.drawable.final_flip_clock_bg_top_9};
        drawBottom = new int[]{R.drawable.final_flip_clock_bg_bottom_0, R.drawable.final_flip_clock_bg_bottom_1,
                R.drawable.final_flip_clock_bg_bottom_2, R.drawable.final_flip_clock_bg_bottom_3,
                R.drawable.final_flip_clock_bg_bottom_4, R.drawable.final_flip_clock_bg_bottom_5,
                R.drawable.final_flip_clock_bg_bottom_6, R.drawable.final_flip_clock_bg_bottom_7,
                R.drawable.final_flip_clock_bg_bottom_8, R.drawable.final_flip_clock_bg_bottom_9};
        mBottomView.setBackgroundResource(drawBottom[0]);
        mMiddleView.setBackgroundResource(drawTop[0]);
    }

    public int getId()
    {
        return id;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight)
    {
        super.initialize(width, height, parentWidth, parentHeight);
        centerX = width / 2;
        centerY = height / 2;
        mCamera = new Camera();
    }

    public void setCountIter(int count)
    {
        if (count > drawTop.length - 1)
        {
            mCountIter = 0;
        } else
            mCountIter = count;

    }

    public int getCountIter()
    {
        return mCountIter;
    }

    @Override protected void applyTransformation(float interpolatedTime, Transformation t)
    {
        float degrees = (float) (-180.0 * interpolatedTime);

        if (interpolatedTime == 0.0f)
        {

            if (mCountIter == 0)
                mMiddleView.setBackgroundResource(drawTop[drawTop.length - 1]);
            else
                mMiddleView.setBackgroundResource(drawTop[mCountIter - 1]);
            mTopView.setBackgroundResource(drawTop[mCountIter]);
            mMiddleView.setVisibility(View.VISIBLE);
        }
        if (interpolatedTime >= 0.5f)
        {
            degrees -= 180.f;
            mMiddleView.setBackgroundResource(drawBottom[mCountIter]);
        }
        if (interpolatedTime == 1.0f)
        {

            mBottomView.setBackgroundResource(drawBottom[mCountIter]);
            mMiddleView.setVisibility(View.GONE);
        }
        final Matrix matrix = t.getMatrix();
        mCamera.save();
        mCamera.rotateX(degrees);
        mCamera.getMatrix(matrix);
        mCamera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}
