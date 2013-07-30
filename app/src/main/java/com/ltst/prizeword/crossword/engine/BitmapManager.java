package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.SparseArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapManager
{
    private @Nonnull Context mContext;
    private @Nonnull SparseArray<BitmapEntity> mBitmaps;
    private @Nonnull Paint mPaint;

    public BitmapManager(@Nonnull Context context)
    {
        mContext = context;
        mBitmaps = new SparseArray<BitmapEntity>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void addBitmap(int resource)
    {
        BitmapEntity entity = new BitmapEntity(resource);
        mBitmaps.append(resource, entity);
        entity.loadResource(mContext.getResources());
    }

    public void drawResource(int resource, @Nonnull Canvas canvas, float posX, float posY)
    {
        BitmapEntity bm = mBitmaps.get(resource);
        bm.draw(canvas, posX, posY, mPaint);
    }

    public void drawResource(int resource, @Nonnull Canvas canvas, @Nonnull RectF rect)
    {
        BitmapEntity bm = mBitmaps.get(resource);
        bm.draw(canvas, rect, mPaint);
    }

    public int getWidth(int resource)
    {
        @Nullable BitmapEntity bm = mBitmaps.get(resource);
        if (bm != null)
        {
            return bm.width;
        }
        return 0;
    }

    public int getHeight(int resource)
    {
        @Nullable BitmapEntity bm = mBitmaps.get(resource);
        if (bm != null)
        {
            return bm.height;
        }
        return 0;
    }

    public void recycle()
    {
        for (int i = 0; i < mBitmaps.size(); i++)
        {
            int key = mBitmaps.keyAt(i);
            mBitmaps.get(key).recycle();
        }
    }
}
