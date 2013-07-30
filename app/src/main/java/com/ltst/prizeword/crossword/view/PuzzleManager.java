package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.ltst.prizeword.crossword.engine.PuzzleFieldDrawer;
import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.tools.BitmapHelper;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    private Point mFocusViewPoint;
    private @Nonnull Paint mPaint;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleFieldDrawer mFieldDrawer;
    private @Nonnull Matrix mMatrix;
    private @Nonnull Rect mPuzzleViewRect;
    private @Nonnull Rect mScaledViewRect;

    private float MIN_SCALE;
    private float MAX_SCALE = 1.0f;
    private boolean mScaled;
    private float mCurrentScale = MAX_SCALE;

    public PuzzleManager(@Nonnull Context context, @Nonnull PuzzleResources info, @Nonnull Rect puzzleViewRect)
    {
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPuzzleViewRect = puzzleViewRect;
        mFieldDrawer = new PuzzleFieldDrawer(context, info);
        mFieldDrawer.loadResources();
        mMatrix = new Matrix();

        float scaleWidth = (float)mPuzzleViewRect.width()/(float)mFieldDrawer.getWidth();
        float scaleHeight = (float)mPuzzleViewRect.height()/(float)mFieldDrawer.getHeight();
        MIN_SCALE = Math.min(scaleHeight, scaleWidth);
        mFieldDrawer.enableScaling(1 + scaleWidth, 1 + scaleHeight);
        mScaled = true;

        mScaledViewRect = new Rect(0, 0,
                mFieldDrawer.getActualWidth(),
                mFieldDrawer.getActualHeight());
        mFocusViewPoint = new Point(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
    }

    private int curScrollX = 0;
    private int curScrollY = 0;

    public void onScrollEvent(float offsetX, float offsetY)
    {
//        curScrollX += Math.abs(offsetX);
//        curScrollY += Math.abs(offsetY);

        mFocusViewPoint.x += offsetX;
        mFocusViewPoint.y += offsetY;
        mFieldDrawer.checkFocusPoint(mFocusViewPoint, mScaled ? mPuzzleViewRect : mScaledViewRect);
//        Log.i("TOUCH", "view x: " + mFocusViewPoint.x + " y: " + mFocusViewPoint.y);
    }

    public void onScaleEvent()
    {
        mScaled = !mScaled;
        mCurrentScale = (mScaled) ? MAX_SCALE : MIN_SCALE;
        mFocusViewPoint.set(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
    }

    private void configureMatrix()
    {
        mMatrix.reset();
        float translateX = mFocusViewPoint.x - (mScaled ? mPuzzleViewRect.width()/2 : mScaledViewRect.width()/2);
        float translateY = mFocusViewPoint.y - (mScaled ? mPuzzleViewRect.height()/2 : mScaledViewRect.height()/2);
        mMatrix.postTranslate(-translateX, -translateY);
        mMatrix.postScale(mCurrentScale, mCurrentScale);

//        mMatrix.postTranslate(-(mDrawingRect.width() - mPuzzleViewRect.width()), -(mDrawingRect.height() - mPuzzleViewRect.height()));
//        mMatrix.postTranslate(-(mDrawingRect.width() - mPuzzleViewRect.width()), 0);
    }

    private void configureViewport()
    {
//        int viewLeft = mFocusDrawPoint.x - mDrawingRect.width()/2;
//        int viewTop = mFocusDrawPoint.y - mDrawingRect.height()/2;
//        int viewRight = mFocusDrawPoint.x + mDrawingRect.width()/2;
//        int viewBottom = mFocusDrawPoint.y + mDrawingRect.height()/2;
//        mViewport.set(viewLeft, viewTop, viewRight, viewBottom);
////        mViewport.set(mPuzzleRect.width() - mDrawingRect.width(),
//                mPuzzleRect.height() - mDrawingRect.height(),
//                mPuzzleRect.width(), mPuzzleRect.height());
    }

    public void drawPuzzle(@Nonnull Canvas screenCanvas)
    {
        int saveCount = screenCanvas.getSaveCount();
        screenCanvas.save();

        configureMatrix();
        screenCanvas.concat(mMatrix);

        mFieldDrawer.drawBackground(screenCanvas);

        screenCanvas.restoreToCount(saveCount);
    }


    public void recycle()
    {
        mFieldDrawer.unloadResources();
    }

}
