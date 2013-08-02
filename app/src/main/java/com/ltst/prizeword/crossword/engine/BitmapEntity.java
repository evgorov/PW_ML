package com.ltst.prizeword.crossword.engine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

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

    public void loadResource(@Nonnull Resources res)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        mBitmap = BitmapFactory.decodeResource(res, resourceId, options);
        mIsRecycled = false;
        width = options.outWidth;
        height = options.outHeight;
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
}
