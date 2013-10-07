package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.ltst.przwrd.crossword.engine.PuzzleFieldDrawer;
import com.ltst.przwrd.crossword.engine.PuzzleResources;
import com.ltst.przwrd.crossword.engine.PuzzleResourcesAdapter;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerBoolean;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleManager
{
    public static final @Nonnull String BF_FOCUS_PUZZLE_POINT = "PuzzleManager.focusPuzzlePoint";
    public static final @Nonnull String BF_FOCUS_POINT = "PuzzleManager.focusPoint";
    public static final @Nonnull String BF_VIEW_RECT = "PuzzleManager.viewRect";

    private volatile @Nullable Point mFocusViewPoint;
    private @Nullable PointF mFocusPuzzlePoint;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleFieldDrawer mFieldDrawer;
    private @Nonnull Matrix mMatrix;
    private @Nullable Rect mPuzzleViewRect;
    private @Nullable Rect mScaledViewRect;
    private @Nonnull PuzzleResourcesAdapter mResourcesAdapter;
    private @Nonnull IListener<Rect> mInvalidateHandler;

    private float MIN_SCALE;
    private float MAX_SCALE = 1.0f;
    private boolean mScaled = true;
    private volatile float mCurrentScale = MAX_SCALE;
    private boolean mIsAnimating;

    private @Nullable Point mLastQuestionTapPoint;
    private @Nullable Point mInputFocusViewPoint;

    private boolean isRestored;

    public final static ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public PuzzleManager(@Nonnull Context context,
                         @Nonnull PuzzleResourcesAdapter adapter,
                         @Nonnull IListener<Rect> invalidateHandler)
    {
        mContext = context;
        mResourcesAdapter = adapter;
        mMatrix = new Matrix();
        mInvalidateHandler = invalidateHandler;
        mFieldDrawer = new PuzzleFieldDrawer(context, adapter, invalidateHandler);
        isRestored = false;

    }

    public void setDrawText(boolean drawText)
    {
        mFieldDrawer.setDrawText(drawText);
    }

    public void setResourcesDecodedHandler(@Nullable IListenerVoid resourcesDecodedHandler)
    {
        mFieldDrawer.setResourcesDecodedHandler(resourcesDecodedHandler);
    }

    public void saveState(@Nonnull Bundle dest)
    {
        if (mFocusViewPoint != null)
        {
            dest.putParcelable(BF_FOCUS_POINT, mFocusViewPoint);
            PointF focusPuzzlePoint = new PointF(mFocusViewPoint.x, mFocusViewPoint.y);
            mFieldDrawer.convertPointFromScreenCoordsToTilesAreaCoords(focusPuzzlePoint);

            dest.putParcelable(BF_FOCUS_PUZZLE_POINT, focusPuzzlePoint);
        }
        dest.putParcelable(BF_VIEW_RECT, mPuzzleViewRect);
    }

    public void restoreState(@Nonnull Bundle source)
    {
        isRestored = true;
        Log.i("restore state");
        mFocusViewPoint = source.getParcelable(BF_FOCUS_POINT);
        mPuzzleViewRect = source.getParcelable(BF_VIEW_RECT);
        mFocusPuzzlePoint = source.getParcelable(BF_FOCUS_PUZZLE_POINT);
        mScaled = source.getBoolean(PuzzleView.BF_SCALED);
        mInvalidateHandler.handle(mPuzzleViewRect);
    }

    // ==== events managing ==================================

    public boolean onScrollEvent(float offsetX, float offsetY)
    {
        if(mIsAnimating || mScaledViewRect == null || mFocusViewPoint == null)
            return false;
        mFocusViewPoint.x += offsetX;
        mFocusViewPoint.y += offsetY;
        checkFocusViewPoint();
        return true;
    }

    public boolean onScaleEvent(@Nonnull View view, @Nullable PointF tapPoint)
    {
        if(mIsAnimating || mScaledViewRect == null || mFocusViewPoint == null || mPuzzleViewRect == null)
            return false;
        if(tapPoint == null)
        {
            ScaleAndTranslateAnimationThread anim = null;
            if(mScaled)
                anim = new ScaleAndTranslateAnimationThread(view, MAX_SCALE, MIN_SCALE, null);
            else
                anim = new ScaleAndTranslateAnimationThread(view, MIN_SCALE, MAX_SCALE, null);
            mExecutor.execute(anim);
        }
        else
        {
            int scaleX = mScaledViewRect.width()/mPuzzleViewRect.width();
            int scaleY = mScaledViewRect.height()/mPuzzleViewRect.height();
            final float puzzleX = mFocusViewPoint.x + (tapPoint.x - mPuzzleViewRect.width()/2) * scaleX;
            final float puzzleY = mFocusViewPoint.y + (tapPoint.y - mPuzzleViewRect.height()/2) * scaleY;
            Point translatePoint = new Point((int)puzzleX, (int) puzzleY);
            mFieldDrawer.checkFocusPoint(translatePoint, mPuzzleViewRect);
            ScaleAndTranslateAnimationThread scale = new ScaleAndTranslateAnimationThread(view, MIN_SCALE, MAX_SCALE, translatePoint);
            mExecutor.execute(scale);
        }
        mScaled = !mScaled;
        return true;
    }

    public boolean onTapEvent(final @Nonnull View view, @Nonnull PointF point, final @Nullable IListenerVoid successHandler)
    {
        if(!mScaled || mFocusViewPoint == null || mPuzzleViewRect == null || mIsAnimating)
            return false;

        final float puzzleX = mFocusViewPoint.x + (point.x - mPuzzleViewRect.width()/2);
        final float puzzleY = mFocusViewPoint.y + (point.y - mPuzzleViewRect.height()/2);

        point.set(puzzleX, puzzleY);

        @Nullable Point coords = mFieldDrawer.getPuzzleTilesColumnRowPointByScreenCoords(point);
        if (coords == null)
        {
            return false;
        }

        final int col = coords.x;
        final int row = coords.y;
        cancelLastQuestion();
        mInputFocusViewPoint = null;
        mResourcesAdapter.updatePuzzleStateByTap(col, row, new IListenerVoid()
        {
            @Override
            public void handle()
            {
                if (successHandler != null)
                {
                    successHandler.handle();
                }
//                cancelLastQuestion();
                mLastQuestionTapPoint = new Point(col, row);

                mInputFocusViewPoint = mFieldDrawer.getInputFocusViewPoint(mResourcesAdapter.getCurrentTileFocusPoint());

                if (mInputFocusViewPoint == null)
                {
                    return;
                }

                mFieldDrawer.checkFocusPoint(mInputFocusViewPoint, mPuzzleViewRect);

                TranslateAnimationThread thread = new TranslateAnimationThread(view,
                        mFocusViewPoint, mInputFocusViewPoint);
                mInvalidateHandler.handle(mPuzzleViewRect);
                mExecutor.execute(thread);
            }
        });
        return true;
    }

    public void onKeyEvent(final @Nonnull View view, @Nonnull KeyEvent keyEvent, final @Nullable IListenerBoolean keyboardOpenHandler)
    {
        switch (keyEvent.getKeyCode())
        {
            // back button, enter key
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENTER:
                clearInput();
                break;
            // backspace key
            case KeyEvent.KEYCODE_DEL:
            {

                mResourcesAdapter.deleteLetterByBackspace();
                mInputFocusViewPoint = mFieldDrawer.getInputFocusViewPoint(mResourcesAdapter.getCurrentTileFocusPoint());

                if (mInputFocusViewPoint == null || mPuzzleViewRect == null || mFocusViewPoint == null)
                {
                    break;
                }

                mFieldDrawer.checkFocusPoint(mInputFocusViewPoint, mPuzzleViewRect);
                TranslateAnimationThread thread = new TranslateAnimationThread(view,
                        mFocusViewPoint, mInputFocusViewPoint);
                mExecutor.execute(thread);
                mInvalidateHandler.handle(mPuzzleViewRect);
                break;
            }
            // russian letters
            default:
            case KeyEvent.KEYCODE_UNKNOWN:
            {
                String letter = keyEvent.getCharacters();

                if (letter == null || letter.length() > 1)
                {
                    letter = String.valueOf(PuzzleResources.LETTER_UNKNOWN);
                }
                letter = letter.toUpperCase();

                mResourcesAdapter.updateLetterCharacterState(letter,
                        new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        if (keyboardOpenHandler != null)
                        {
                            keyboardOpenHandler.handle(false);
                        }
                        mInvalidateHandler.handle(mPuzzleViewRect);
                        triggerLetterAnimation(view);
                    }
                });
                mInputFocusViewPoint = mFieldDrawer.getInputFocusViewPoint(mResourcesAdapter.getCurrentTileFocusPoint());

                if (mInputFocusViewPoint == null || mPuzzleViewRect == null || mFocusViewPoint == null)
                {
                    break;
                }

                mFieldDrawer.checkFocusPoint(mInputFocusViewPoint, mPuzzleViewRect);
                TranslateAnimationThread thread = new TranslateAnimationThread(view,
                        mFocusViewPoint, mInputFocusViewPoint);
                mExecutor.execute(thread);

                mInvalidateHandler.handle(mPuzzleViewRect);
            }
                break;
        }
    }

    private void clearInput()
    {
        mResourcesAdapter.setCurrentQuestionWrong();
        mLastQuestionTapPoint = null;
        mInvalidateHandler.handle(mPuzzleViewRect);
    }

    public void cancelLastQuestion()
    {
        if(mLastQuestionTapPoint != null)
        {
            mResourcesAdapter.cancelLastQuestionState(mLastQuestionTapPoint.x, mLastQuestionTapPoint.y);
            mLastQuestionTapPoint = null;
        }
    }


    // ==== configuring view ==================================

    public void setPuzzleViewRect(@Nonnull Rect puzzleViewRect)
    {

        mPuzzleViewRect = puzzleViewRect;
        mFieldDrawer.disableScaling();
        float scaleWidth = (float)mPuzzleViewRect.width()/(float)mFieldDrawer.getWidth();
        float scaleHeight = (float)mPuzzleViewRect.height()/(float)mFieldDrawer.getHeight();
        MIN_SCALE = Math.min(scaleHeight, scaleWidth);
        mFieldDrawer.enableScaling(1/scaleHeight, 1/scaleWidth);

        mScaledViewRect = new Rect(0, 0,
                mFieldDrawer.getActualWidth(),
                mFieldDrawer.getActualHeight());

        if(isRestored)
        {
            mCurrentScale = mScaled ? MAX_SCALE : MIN_SCALE;
            if (mFocusPuzzlePoint != null)
            {
                mFieldDrawer.convertPointFromTilesAreaCoordsToScreenCoords(mFocusPuzzlePoint);
                mFocusViewPoint = new Point((int)mFocusPuzzlePoint.x, (int)mFocusPuzzlePoint.y);
            }
        }
        else
        {
            mInputFocusViewPoint = mFieldDrawer.getInputFocusViewPoint(mResourcesAdapter.getCurrentTileFocusPoint());

            if (mInputFocusViewPoint == null)
            {
                mFocusViewPoint = new Point(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
            }
            else
            {
                mFieldDrawer.checkFocusPoint(mInputFocusViewPoint, mPuzzleViewRect);
                mFocusViewPoint = new Point(mInputFocusViewPoint);
            }
        }

        checkFocusViewPoint();
        mInvalidateHandler.handle(mPuzzleViewRect);

    }

    private boolean checkFocusViewPoint()
    {
        if (mFocusViewPoint == null || mPuzzleViewRect == null || mScaledViewRect == null)
        {
            return false;
        }
        return mFieldDrawer.checkFocusPoint(mFocusViewPoint, mScaled ? mPuzzleViewRect : mScaledViewRect);
    }

    private void configureMatrix()
    {
        if (mFocusViewPoint == null || mPuzzleViewRect == null)
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
        mFieldDrawer.drawCurrentInputWithAnimation(screenCanvas, mResourcesAdapter.getOnInputAnimationEndHandler());

        screenCanvas.restoreToCount(saveCount);

    }

    public void recycle()
    {
        clearInput();
        mFieldDrawer.unloadResources();
    }


    // ==== animation ===========================

    public void triggerLetterAnimation(final @Nonnull View view)
    {
        mFieldDrawer.createInputRectList(mResourcesAdapter.getCurrentInputPuzzleStates(), new IListenerVoid()
        {
            @Override
            public void handle()
            {
                view.postInvalidate();
            }
        });
    }

    // ==== animation threads ===========================

    private class ScaleAndTranslateAnimationThread extends Thread
    {
        private static final long FPS_INTERVAL = 1000 / 60;
        static final float ANIMATION_SCALE_PER_ITERATION_IN = 1.1f;
        static final float ANIMATION_SCALE_PER_ITERATION_OUT = 0.9f;

        private final @Nonnull View view;
        private final float fromZoom;
        private final float toZoom;
        private final float mDeltaScale;
        private @Nullable Point focusPoint;

        private ScaleAndTranslateAnimationThread(@Nonnull View view, float fromZoom, float toZoom, @Nullable Point focusPoint)
        {
            this.toZoom = toZoom;
            this.fromZoom = fromZoom;
            this.view = view;
            this.focusPoint = focusPoint;
            if (fromZoom < toZoom) {
                mDeltaScale = ANIMATION_SCALE_PER_ITERATION_IN;
            } else {
                mDeltaScale = ANIMATION_SCALE_PER_ITERATION_OUT;
            }
        }

        @Override
        public void run()
        {
            if(mFocusViewPoint == null || mPuzzleViewRect == null)
                return;
            synchronized (this)
            {
                mIsAnimating = true;
                int scaleIteratons = 1;
                float tempScale = mCurrentScale;
                while((mDeltaScale > 1f && tempScale < toZoom)
                        || (mDeltaScale < 1f && toZoom < tempScale))
                {
                    tempScale *= mDeltaScale;
                    scaleIteratons++;
                }

                int stepX = 0;
                int stepY = 0;
                Point center = new Point(mFieldDrawer.getCenterX(), mFieldDrawer.getCenterY());
                if(focusPoint == null)
                {
                    stepX = (center.x - mFocusViewPoint.x)/scaleIteratons;
                    stepY = (center.y - mFocusViewPoint.y)/scaleIteratons;
                    focusPoint = new Point(center);
                }
                else
                {
                    stepX = (focusPoint.x - mFocusViewPoint.x)/scaleIteratons;
                    stepY = (focusPoint.y - mFocusViewPoint.y)/scaleIteratons;
                }

                int iter = 0;
                while(iter <= scaleIteratons)
                {
                    iter++;
                    if((mDeltaScale > 1f && mCurrentScale < toZoom)
                            || (mDeltaScale < 1f && toZoom < mCurrentScale))
                    {
                        mCurrentScale *= mDeltaScale;
                    }

                    if(mFocusViewPoint != focusPoint)
                    {
                        mFocusViewPoint.offset(stepX, stepY);
                    }

                    view.postInvalidate(mPuzzleViewRect.left, mPuzzleViewRect.top,
                            mPuzzleViewRect.right, mPuzzleViewRect.bottom);
                    try
                    {
                        ScaleAndTranslateAnimationThread.sleep(FPS_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(e.getMessage());
                    }
                }
                final float delta = toZoom / mCurrentScale;
                mCurrentScale *= delta;
                mFocusViewPoint.set(focusPoint.x, focusPoint.y);
                view.postInvalidate(mPuzzleViewRect.left, mPuzzleViewRect.top,
                        mPuzzleViewRect.right, mPuzzleViewRect.bottom);
                mIsAnimating = false;
            }
        }
    }

    private class TranslateAnimationThread extends Thread
    {
        private static final long FPS_INTERVAL = 1000 / 60;
        private static final int TRANSLATION_ = 5;

        private final @Nonnull View mView;
        private final @Nonnull Point mDestinationPoint;
        private int mDeltaX;
        private int mDeltaY;
        private int mIterations;

        private TranslateAnimationThread(@Nonnull View view,
                                         @Nonnull Point sourcePoint,
                                         @Nonnull Point destinationPoint)
        {
            mView = view;
            mDestinationPoint = new Point(destinationPoint);
            mDeltaX = (destinationPoint.x - sourcePoint.x)/(int)FPS_INTERVAL;
            mDeltaY = (destinationPoint.y - sourcePoint.y)/(int)FPS_INTERVAL;
            mIterations = Math.max((destinationPoint.x - sourcePoint.x)/((mDeltaX == 0) ? 1:mDeltaX),
                    (destinationPoint.y - sourcePoint.y)/((mDeltaY == 0)? 1:mDeltaY));
        }

        @Override
        public void run()
        {
            synchronized (this)
            {
                mIsAnimating = true;
                boolean xTranslation = false;
                boolean yTranslation = false;
                int iter = 0;
                while(iter <= mIterations)
                {
                    iter++;
                    if (Math.abs(mFocusViewPoint.x - mDestinationPoint.x) < TRANSLATION_)
                    {
                        xTranslation = true;
                        mDeltaX = 0;
                    }

                    if (Math.abs(mFocusViewPoint.y - mDestinationPoint.y) < TRANSLATION_)
                    {
                        yTranslation = true;
                        mDeltaY = 0;
                    }

                    if(xTranslation && yTranslation)
                        break;

                    mFocusViewPoint.offset(mDeltaX, mDeltaY);

                   if(checkFocusViewPoint())
                       break;

                    mView.postInvalidate(mPuzzleViewRect.left, mPuzzleViewRect.top,
                            mPuzzleViewRect.right, mPuzzleViewRect.bottom);
                    try
                    {
                        ScaleAndTranslateAnimationThread.sleep(FPS_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        Log.e(e.getMessage());
                    }
                }
                mIsAnimating = false;
            }
        }
    }

}
