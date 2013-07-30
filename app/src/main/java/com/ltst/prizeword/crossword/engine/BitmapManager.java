package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.SparseArray;

import javax.annotation.Nonnull;

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

    public void recycle()
    {
        for (int i = 0; i < mBitmaps.size(); i++)
        {
            int key = mBitmaps.keyAt(i);
            mBitmaps.get(key).recycle();
        }
    }
}
