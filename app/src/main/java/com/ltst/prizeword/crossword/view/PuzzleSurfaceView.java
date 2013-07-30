package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ltst.prizeword.crossword.engine.PuzzleResources;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
    private @Nonnull Context mContext;
    private @Nonnull DrawingThread mDrawingThread;
    private @Nonnull Rect mViewScreenRect;
    private @Nullable PuzzleManager mPuzzleManager;

    protected static int ACTION_MODE_NONE = 0;
    protected static int ACTION_MODE_PAN = 1;
    private int mActionMode;
    private @Nonnull PointF mPointPanStart;
    private @Nonnull PointF mPanTranslation;
    private boolean mIsPanning = false;
    private @Nonnull GestureDetector mGestureDetector;


    public PuzzleSurfaceView(Context context)
    {
        this(context, null);
    }

    public PuzzleSurfaceView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PuzzleSurfaceView(@Nonnull Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        getHolder().addCallback(this);
        mPointPanStart = new PointF();
        mPanTranslation = new PointF();
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        mDrawingThread = new DrawingThread(this);
        mDrawingThread.setRunning(true);
        mDrawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mViewScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        boolean retry = true;
        mDrawingThread.setRunning(false);
        while (retry)
        {
            try
            {
                mDrawingThread.join();
                retry = false;
            }
            catch (InterruptedException e)
            {
                Log.i(e.getMessage());
            }
        }
    }

    public void initializePuzzle(@Nonnull PuzzleResources info)
    {
        if (mPuzzleManager == null)
        {
            mPuzzleManager = new PuzzleManager(mContext, info, mViewScreenRect);
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
//        switch (event.getAction() & MotionEvent.ACTION_MASK)
//        {
//            case MotionEvent.ACTION_DOWN:
//                mPointPanStart.set(event.getX(), event.getY());
//                setActionMode(ACTION_MODE_PAN);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if(mActionMode == ACTION_MODE_PAN)
//                {
//                    double distance = Math.sqrt(Math.pow(event.getX() - mPointPanStart.x,2.0)
//                            + Math.pow(event.getY() - mPointPanStart.y, 2.0));
////                    if(distance > 0.05f)
//                    {
//                        mPanTranslation = new PointF(event.getX() - mPointPanStart.x,
//                                event.getY() - mPointPanStart.y);
//                        mIsPanning = true;
//                        if (mPuzzleManager != null)
//                        {
//                            mPuzzleManager.onScrollEvent((int)mPanTranslation.x, (int)mPanTranslation.y);
//                        }
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                mIsPanning = false;
//                break;
//            case MotionEvent.ACTION_UP:
//                mIsPanning = false;
//                setActionMode(ACTION_MODE_NONE);
//                break;
//        }
//        return true;
        return  mGestureDetector.onTouchEvent(event);
    }

    public int getActionMode()
    {
        return mActionMode;
    }

    public void setActionMode(int actionMode)
    {
        mActionMode = actionMode;
    }

    public void recycle()
    {
        if (mPuzzleManager != null)
        {
            mPuzzleManager.recycle();
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
            }
            return true;
        }
    }

}
