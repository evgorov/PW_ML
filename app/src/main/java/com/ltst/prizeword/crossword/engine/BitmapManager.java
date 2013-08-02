package com.ltst.prizeword.crossword.engine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.SparseArray;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.tools.decoding.BitmapDecoderTask;
import com.ltst.prizeword.tools.decoding.BitmapDecodingService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapManager
{
    private @Nonnull Context mContext;
    private @Nonnull SparseArray<BitmapEntity> mBitmaps;
    private @Nonnull Paint mPaint;
    private @Nonnull IBcConnector mBcConnector;

    public BitmapManager(@Nonnull Context context)
    {
        mContext = context;
        mBcConnector = new BcConnector(context);
        mBitmaps = new SparseArray<BitmapEntity>();
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
//        BitmapDecoder decoder = new BitmapDecoder()
//        {
//            @Nonnull
//            @Override
//            protected Intent createIntent()
//            {
//                return BitmapDecoderTask.createIntent(resource, null);
//            }
//
//            @Override
//            protected void handleData(@Nullable Bundle result)
//            {
//                Log.i("Handling result");
//                if (result == null)
//                {
//                    return;
//                }
//                Bitmap bitmap = result.getParcelable(BitmapDecoderTask.BF_BITMAP);
//                if (bitmap == null)
//                {
//                    return;
//                }
//
//                BitmapEntity entity = new BitmapEntity(resource);
//                entity.setBitmap(bitmap);
//                mBitmaps.append(resource, entity);
//                Log.i("Bitmap handled: " + bitmap.getWidth() + " " + bitmap.getHeight());
//            }
//        };
//        decoder.update(new IListenerVoid()
//        {
//            @Override
//            public void handle()
//            {
//                Log.i("Invalidate handled");
//                invalidateListener.handle(rect);
//            }
//        });
    }

    public boolean hasReasource(int resource)
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

    private abstract class BitmapDecoder extends ModelUpdater<BitmapDecodingService.DecodeTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<BitmapDecodingService.DecodeTaskEnv>> getTaskClass()
        {
            return BitmapDecoderTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<BitmapDecodingService.DecodeTaskEnv>> getServiceClass()
        {
            return BitmapDecodingService.class;
        }
    }
}
