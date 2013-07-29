package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.omich.velo.log.Log;

import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
    private @Nonnull Context mContext;
    private @Nonnull DrawingThread mDrawingThread;
    private @Nonnull Rect mViewRect;
    private @Nonnull Rect mViewScreenRect;
    private @Nullable PuzzleManager mPuzzleManager;

    private LinkedList<Long> times = new LinkedList<Long>();

    public PuzzleSurfaceView(Context context)
    {
        this(context, null);
    }

    public PuzzleSurfaceView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PuzzleSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        getHolder().addCallback(this);
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
        mViewRect = new Rect(0, 0, width, height);
        mViewScreenRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
        if (mPuzzleManager != null)
        {
            mPuzzleManager.setPuzzleViewRect(mViewRect);
        }
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

    public void initializePuzzle(@Nonnull PuzzleViewInformation info)
    {
        if (mPuzzleManager == null)
        {
            mPuzzleManager = new PuzzleManager(mContext, info);
            mPuzzleManager.setPuzzleViewRect(mViewRect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (canvas == null)
            return;
        if (mPuzzleManager != null && !mPuzzleManager.isRecycled())
        {
            mPuzzleManager.drawPuzzle(canvas);
        }
    }

    public void recycle()
    {
        if (mPuzzleManager != null)
        {
            mPuzzleManager.recycle();
        }
    }
}
