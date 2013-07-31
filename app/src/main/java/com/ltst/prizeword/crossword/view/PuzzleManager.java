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
        mFieldDrawer = new PuzzleFieldDrawer(context, info);
        mFieldDrawer.loadResources();
        mMatrix = new Matrix();
        setPuzzleViewRect(puzzleViewRect);
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
        mFocusViewPoint.x += offsetX;
        mFocusViewPoint.y += offsetY;
        mFieldDrawer.checkFocusPoint(mFocusViewPoint, mScaled ? mPuzzleViewRect : mScaledViewRect);
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

        if(mCurrentScale == MIN_SCALE)
        {
            float translateX = (mFocusViewPoint.x - mPuzzleViewRect.width()/2/mCurrentScale);
            float translateY = (mFocusViewPoint.y- mPuzzleViewRect.height()/2/mCurrentScale);
            mMatrix.postTranslate(-translateX, -translateY);
        }
        if(mCurrentScale == MAX_SCALE)
        {
            float translateX = mFocusViewPoint.x - mPuzzleViewRect.width()/2;
            float translateY = mFocusViewPoint.y - mPuzzleViewRect.height()/2;
            mMatrix.postTranslate(-translateX, -translateY);
        }
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

}
