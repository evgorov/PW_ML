package com.ltst.prizeword.crossword.view;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import javax.annotation.Nonnull;

public class DrawingThread extends Thread
{
    private @Nonnull PuzzleSurfaceView mView;
    private boolean mIsRunning = false;

    public DrawingThread(@Nonnull PuzzleSurfaceView view)
    {
        mView = view;
    }

    public void setRunning(boolean isRunning)
    {
        mIsRunning = isRunning;
    }

    @Override
    public void run()
    {
        while (mIsRunning)
        {
            Canvas c = null;
            try
            {
                SurfaceHolder holder = mView.getHolder();
                c = holder.lockCanvas();
                synchronized (holder)
                {
                    if (c != null)
                        mView.onDraw(c);
                }
            }
            finally
            {
                if (c != null)
                {
                    mView.getHolder().unlockCanvasAndPost(c);
                }
            }
        }
    }
}
