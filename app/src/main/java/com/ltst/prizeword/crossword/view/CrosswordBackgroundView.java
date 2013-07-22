package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import javax.annotation.Nonnull;

public class CrosswordBackgroundView extends ImageView
{
    private @Nonnull Bitmap mItemBitmap;
    private @Nonnull Bitmap mBackgroundBitmap;
    private @Nonnull Matrix mMatrix;

    private int mBackgroundWidth;
    private int mBackgroundHeight;

    private @Nonnull DetectedMotion mMotion = DetectedMotion.NONE;

    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float mStartX = 0f;
    private float mStartY = 0f;

    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float mTranslateX = 0f;
    private float mTranslateY = 0f;

    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float mPreviousTranslateX = 0f;
    private float mPreviousTranslateY = 0f;

    private boolean mIsDragging;

    private @Nonnull ScaleGestureDetector mScaleDetector;

    private static final float MIN_SCALE_FACTOR = 1.0f;
    private static final float MAX_SCALE_FACTOR = 2.0f;
    private float mScaleFactor = MIN_SCALE_FACTOR;

    public CrosswordBackgroundView(Context context)
    {
        this(context, null);
    }

    public CrosswordBackgroundView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public CrosswordBackgroundView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        Bitmap bgBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.crossword_background);
        mBackgroundBitmap = bgBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mItemBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.crossword_cell_background);
        mMatrix = new Matrix();

        mBackgroundWidth = mBackgroundBitmap.getWidth();
        mBackgroundHeight = mBackgroundBitmap.getHeight();

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        configureBounds();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int saveCount = canvas.getSaveCount();
        canvas.save();

        canvas.scale(mScaleFactor, mScaleFactor);
//        canvas.setMatrix(mMatrix);

        canvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
        canvas.drawBitmap(mItemBitmap, 10, 10, null);

        canvas.restoreToCount(saveCount);
    }

    private void configureBounds()
    {
        mMatrix.setScale(mScaleFactor, mScaleFactor);

        int displayWidth = getMeasuredWidth();
        int displayHeight = getMeasuredHeight();

        //If translateX times -1 is lesser than zero, let's set it to zero. This takes care of the left bound
        if ((mTranslateX * -1) < 0)
        {
            mTranslateX = 0;
        }

        //This is where we take care of the right bound. We compare translateX times -1 to (scaleFactor - 1) * displayWidth.
        //If translateX is greater than that value, then we know that we've gone over the bound. So we set the value of
        //translateX to (1 - scaleFactor) times the display width. Notice that the terms are interchanged; it's the same
        //as doing -1 * (scaleFactor - 1) * displayWidth
        else if ((mTranslateX * -1) > (mScaleFactor - 1) * displayWidth)
        {
            mTranslateX = (1 - mScaleFactor) * displayWidth;
        }

        if (mTranslateY * -1 < 0)
        {
            mTranslateY = 0;
        }

        //We do the exact same thing for the bottom bound, except in this case we use the height of the display
        else if((mTranslateY * -1) > (mScaleFactor - 1) * displayHeight)
        {
            mTranslateY = (1 - mScaleFactor) * displayHeight;
        }

        mMatrix.postTranslate(mTranslateX / mScaleFactor, mTranslateY / mScaleFactor);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
//        switch (event.getAction() & MotionEvent.ACTION_MASK)
//        {
//            case MotionEvent.ACTION_DOWN:
//                mMotion = DetectedMotion.DRAG;
//
//                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
//                //amount for each coordinates This works even when we are translating the first time because the initial
//                //values for these two variables is zero.
//                mStartX = event.getX() - mPreviousTranslateX;
//                mStartY = event.getY() - mPreviousTranslateY;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                mTranslateX = event.getX() - mStartX;
//                mTranslateY = event.getY() - mStartY;
//
//                //We cannot use startX and startY directly because we have adjusted their values using the previous translation values.
//                //This is why we need to add those values to startX and startY so that we can get the actual coordinates of the finger.
//                double distance = Math.sqrt(
//                        Math.pow(event.getX() - (mStartX + mPreviousTranslateX), 2) +
//                                Math.pow(event.getY() - (mStartY + mPreviousTranslateY), 2)
//                );
//
//                if (distance > 0)
//                {
//                    mIsDragging = true;
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                mMotion = DetectedMotion.ZOOM;
//                break;
//            case MotionEvent.ACTION_UP:
//                mMotion = DetectedMotion.NONE;
//                mIsDragging = false;
//
//                //All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
//                //previousTranslate
//                mPreviousTranslateX = mTranslateX;
//                mPreviousTranslateY = mTranslateY;
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                mMotion = DetectedMotion.DRAG;
//
//                //This is not strictly necessary; we save the value of translateX and translateY into previousTranslateX
//                //and previousTranslateY when the second finger goes up
//                mPreviousTranslateX = mTranslateX;
//                mPreviousTranslateY = mTranslateY;
//                break;
//        }
//
        mScaleDetector.onTouchEvent(event);

//        if ((mMotion == DetectedMotion.DRAG && mScaleFactor != 1f && mIsDragging) || mMotion == DetectedMotion.ZOOM)
//        {
//            configureBounds();
//            invalidate();
//        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));
            return true;
        }
    }

    private enum DetectedMotion
    {
        NONE,
        DRAG,
        ZOOM
    }
}
