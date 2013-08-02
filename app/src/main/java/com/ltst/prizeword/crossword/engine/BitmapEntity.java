package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.ltst.prizeword.tools.decoding.BitmapDecoderTask;

import org.omich.velo.handlers.IListener;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapEntity
{
    public int width = 0;
    public int height = 0;
    private @Nullable Bitmap mBitmap;
    private int resourceId;
    private boolean mIsRecycled;

    public BitmapEntity(int resource)
    {
        this.resourceId = resource;
        mIsRecycled = false;
    }

    public BitmapEntity(@Nonnull Bitmap bitmap)
    {
        mBitmap = bitmap;
        mIsRecycled = false;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
    }

    public void loadResource(@Nonnull Context context)
    {
        mBitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        mIsRecycled = false;
        width = mBitmap.getWidth();
        height = mBitmap.getHeight();
    }

    public void setBitmap(@Nullable Bitmap bitmap)
    {
        mBitmap = bitmap;
        if (mBitmap != null)
        {
            width = mBitmap.getWidth();
            height = mBitmap.getHeight();
        }
    }

    public void draw(@Nonnull Canvas canvas, float posX, float posY, @Nonnull Paint paint)
    {
        if (mBitmap != null && !mIsRecycled)
        {
            canvas.drawBitmap(mBitmap, posX, posY, paint);
        }
    }

    public void draw(@Nonnull Canvas canvas, @Nonnull RectF rect, @Nonnull Paint paint)
    {
        if (mBitmap != null && !mIsRecycled)
        {
            canvas.drawBitmap(mBitmap, null, rect, paint);
        }
    }

    public void recycle()
    {
        if (mBitmap != null)
        {
            mBitmap.recycle();
            mIsRecycled = true;
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }


}
