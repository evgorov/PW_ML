package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.tools.decoding.BitmapDecoderTask;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapManager
{
    private @Nonnull Context mContext;
    private @Nonnull SparseArray<BitmapEntity> mBitmaps;
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
                          final @Nonnull IListener<Rect> invalidateListener,
                          final @Nonnull Rect rect)
    {
        BitmapEntity entity = new BitmapEntity(resource);
        entity.loadResource(mContext);
        mBitmaps.append(resource, entity);
        invalidateListener.handle(rect);

//        mBitmapResourceModel.loadBitmapEntity(resource, new
//            IListener<BitmapEntity>()
//            {
//                @Override
//                public void handle(@Nullable BitmapEntity entity)
//                {
//                    mBitmaps.append(resource, entity);
//                    invalidateListener.handle(rect);
//                }
//            });

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
