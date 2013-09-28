package com.ltst.przwrd.dowloading;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.ltst.przwrd.db.DbService;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.errors.OmOutOfMemoryException;
import org.omich.velo.events.PairListeners;
import org.omich.velo.handlers.IListener;
import org.omich.velo.lists.ILoadingQueue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BgImageDownloader implements ILoadingQueue.IBgDownloader<String, Bitmap>
{
    private final @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mImageLoadingTaskId;
    private boolean mIsDestroyed;
    private Class<? extends LoadImageTask> mLoadImageTask;

    public BgImageDownloader(@Nonnull IBcConnector mBcConnector, @Nonnull Class<? extends LoadImageTask> mLoadImageTask)
    {
        this.mBcConnector = mBcConnector;
        this.mLoadImageTask = mLoadImageTask;
    }

    abstract public @Nonnull Intent createIntentFromTask(@Nonnull String previewImageUrl);

    public void destroy()
    {
        String taskId = mImageLoadingTaskId;
        if (taskId != null)
        {
            mBcConnector.cancelTask(taskId);
            mBcConnector.unsubscribeTask(taskId);
            mImageLoadingTaskId = null;
        }

        mIsDestroyed = true;
    }

    //==== IBgDownloader =================================================
    @Override
    public void loadSlowData(@Nonnull String previewUrl, final @Nonnull PairListeners.IListenerBooleanObject<Bitmap> handler)
    {
        String taskId = mImageLoadingTaskId;
        if (taskId != null)
        {
            mBcConnector.cancelTask(taskId);
            mBcConnector.unsubscribeTask(taskId);
        }
        Intent intent = createIntentFromTask(previewUrl);
        mImageLoadingTaskId = mBcConnector.startTask(DbService.class,
                mLoadImageTask,
                intent, new IListener<Bundle>()
        {
            public void handle (@Nullable Bundle result)
            {
                if(mIsDestroyed)
                {
                    Log.i("dasd", "Get to NewsModel.Downloader.onImageLoad after destroying. Howewer I supposed we can't be here at this moment");
                    return;
                }

                mImageLoadingTaskId = null;
                Bitmap bitmap = null;
                try
                {
                    bitmap = LoadImageTask.extractBitmapFromResult(result);
                }
                catch(OmOutOfMemoryException e)
                {
                    //Just can't create news preview image and display it in list.
                    //Do nothing with it.
                }
                handler.handle(bitmap != null, bitmap);
            }
        });

    }

    @Override public void cancelLoadingSlowData()
    {
        String taskId = mImageLoadingTaskId;
        if(mIsDestroyed || taskId == null)
            return;

        mBcConnector.cancelTask(taskId);
        mBcConnector.unsubscribeTask(taskId);
        mImageLoadingTaskId = null;
    }

    public static class SimpleBgImageDownloader extends BgImageDownloader
    {
        public SimpleBgImageDownloader(@Nonnull IBcConnector mBcConnector, @Nonnull Class<? extends LoadImageTask> mLoadImageTask)
        {
            super(mBcConnector, mLoadImageTask);
        }

        @Nonnull
        @Override
        public Intent createIntentFromTask(@Nonnull String previewImageUrl)
        {
            return LoadImageTask.createIntent(previewImageUrl);
        }
    }
}
