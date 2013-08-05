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
import com.ltst.prizeword.tools.decoding.BitmapDecoder;
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
    private @Nonnull final SparseArray<BitmapEntity> mBitmaps;
    private @Nonnull Paint mPaint;
    private @Nonnull IBitmapResourceModel mBitmapResourceModel;

    public BitmapManager(@Nonnull Context context, @Nonnull IBitmapResourceModel bitmapModel)
    {
        Log.i("Creating manager..");
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
                    Log.i("Appending bitmap");
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

//        mBitmapResourceModel.loadBitmapEntity(resource, null);

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

    private class DecodingTask extends AsyncTask<Bundle, Void, Bundle>
    {
        @Override
        protected Bundle doInBackground(Bundle... params)
        {
            Bundle b = params[0];
            if (b == null)
            {
                return null;
            }
            int res = b.getInt(BitmapDecoderTask.BF_RESOURCE_ID);
            Bitmap bm = BitmapDecoder.decode(mContext, res);
            Bundle ret = new Bundle();
            ret.putInt(BitmapDecoderTask.BF_RESOURCE_ID, res);
            ret.putParcelable(BitmapDecoderTask.BF_BITMAP, bm);
            return ret;
        }

        @Override
        protected void onPostExecute(Bundle bundle)
        {
            if (bundle == null)
            {
                return;
            }
            int res = bundle.getInt(BitmapDecoderTask.BF_RESOURCE_ID);
            Bitmap bm = bundle.getParcelable(BitmapDecoderTask.BF_BITMAP);
            BitmapEntity entity = new BitmapEntity(res);
            entity.setBitmap(bm);
            mBitmaps.append(res, entity);
            super.onPostExecute(bundle);
        }
    }

}
