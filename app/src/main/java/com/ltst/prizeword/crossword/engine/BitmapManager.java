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

//
//        Bundle params = new Bundle();
//        params.putInt(BitmapDecoderTask.BF_RESOURCE_ID, resource);
//        BitmapDecoderAsyncTask task = new BitmapDecoderAsyncTask(mBitmaps);
//        task.execute(params);


//        BitmapDecoder decoder = new BitmapDecoder()
//        {
//            @Nonnull
//            @Override
//            protected Intent createIntent()
//            {
//                return BitmapDecoderTask.createIntent(resource, null);
//            }
//
//        };
//        decoder.updateResources(new IListenerVoid()
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

    private abstract class BitmapDecoder extends ModelUpdater<IBcTask.BcTaskEnv>
    {

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return BitmapDecoderTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            Log.i("Handling result");
            if (result == null)
            {
                return;
            }
            Bitmap bitmap = result.getParcelable(BitmapDecoderTask.BF_BITMAP);
            if (bitmap == null)
            {
                return;
            }
            int resource = result.getInt(BitmapDecoderTask.BF_RESOURCE_ID);

            BitmapEntity entity = new BitmapEntity(resource);
            entity.setBitmap(bitmap);
            mBitmaps.append(resource, entity);
            Log.i("Bitmap handled: " + bitmap.getWidth() + " " + bitmap.getHeight());
        }
    }

    private class BitmapDecoderAsyncTask extends AsyncTask<Bundle, Integer, Bundle>
    {
        private @Nonnull WeakReference<SparseArray<BitmapEntity>> mBitmapsRef;

        public BitmapDecoderAsyncTask(@Nonnull SparseArray<BitmapEntity> bitmaps)
        {
            mBitmapsRef = new WeakReference<SparseArray<BitmapEntity>>(bitmaps);
        }

        @Override
        protected Bundle doInBackground(Bundle... params)
        {
            Log.i("do in background");
            int resource = params[0].getInt(BitmapDecoderTask.BF_RESOURCE_ID);
            Bitmap bm = com.ltst.prizeword.tools.decoding.BitmapDecoder.decode(mContext, resource);
            Bundle bundle = new Bundle();
            bundle.putParcelable(BitmapDecoderTask.BF_BITMAP, bm);
            bundle.putInt(BitmapDecoderTask.BF_RESOURCE_ID, resource);
            return bundle;
        }

        @Override
        protected void onPreExecute()
        {
            Log.i("pre execute");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bundle result)
        {
            Log.i("post execute");
            if (result == null)
            {
                return;
            }
            Bitmap bitmap = result.getParcelable(BitmapDecoderTask.BF_BITMAP);
            if (bitmap == null)
            {
                return;
            }
            int resource = result.getInt(BitmapDecoderTask.BF_RESOURCE_ID);

            BitmapEntity entity = new BitmapEntity(resource);
            entity.setBitmap(bitmap);
            SparseArray<BitmapEntity> bitmaps = mBitmapsRef.get();
            if (bitmaps != null)
            {
                bitmaps.append(resource, entity);
            }
        }

        @Override
        protected void onCancelled()
        {
            Log.i("cancelled");
            super.onCancelled();
        }
    }
}
