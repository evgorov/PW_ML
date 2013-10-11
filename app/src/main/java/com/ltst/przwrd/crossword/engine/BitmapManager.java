package com.ltst.przwrd.crossword.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.SparseArray;

import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapManager
{
    private @Nonnull Context mContext;
    private @Nonnull final SparseArray<BitmapEntity> mBitmaps;
    private @Nonnull Paint mPaint;
    private @Nonnull IBitmapResourceModel mBitmapResourceModel;

    public BitmapManager(@Nonnull Context context, @Nonnull IBitmapResourceModel bitmapModel)
    {
        mContext = context;
        mBitmaps = new SparseArray<BitmapEntity>();
        mBitmapResourceModel = bitmapModel;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void addBitmap(final int resource,
                          final @Nullable IListenerVoid loadingHandler)
    {
        if(hasResource(resource))
            return;

//        BitmapEntity entity = new BitmapEntity(resource);
//        entity.loadResource(mContext);
//        mBitmaps.append(resource, entity);

        mBitmapResourceModel.loadBitmapEntity(resource, new
            IListener<BitmapEntity>()
            {
                @Override
                public void handle(@Nullable BitmapEntity entity)
                {
                    synchronized (this)
                    {
                        mBitmaps.append(resource, entity);
                        if (loadingHandler != null)
                        {
                            loadingHandler.handle();
                        }
                    }
                }
            });

    }

    public boolean hasResource(int resource)
    {
        return mBitmaps.indexOfKey(resource) >= 0;
    }

    public void drawResource(int resource, @Nonnull Canvas canvas, float posX, float posY)
    {
        @Nullable BitmapEntity bm = mBitmaps.get(resource);
        if (bm != null)
        {
            bm.draw(canvas, posX, posY, mPaint);
        }
    }

    public void drawResource(int resource, @Nonnull Canvas canvas, @Nonnull RectF rect)
    {
        @Nullable BitmapEntity bm = mBitmaps.get(resource);
        if (bm != null)
        {
            bm.draw(canvas, rect, mPaint);
        }
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