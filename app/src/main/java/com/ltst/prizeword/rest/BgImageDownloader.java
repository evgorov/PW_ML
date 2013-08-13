package com.ltst.prizeword.rest;

import android.content.Intent;
import android.database.sqlite.SQLiteOutOfMemoryException;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.dowloading.LoadImageTask;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.events.PairListeners;
import org.omich.velo.handlers.IListener;
import org.omich.velo.lists.ILoadingQueue;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BgImageDownloader implements ILoadingQueue.IBgDownloader<String, Bitmap>
{
    private final @Nonnull IBcConnector mBcConnector;
    private @Nullable String mImageLoadingTaskId;
    private boolean mIsDestroyed;
    private Class<? extends LoadImageTask> mLoadImageTask;

    public BgImageDownloader(@Nonnull IBcConnector mBcConnector, Class<? extends LoadImageTask> mLoadImageTask)
    {
        this.mBcConnector = mBcConnector;
        this.mLoadImageTask = mLoadImageTask;
    }

    abstract public @Nonnull Intent createIntentFromTask(@Nonnull String previewImageUrl);

    //==== На всякий случай =========================================
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

    //==== IBgDownloader ============================================
    @Override
    public void loadSlowData(@Nonnull String previewUrl, final @Nonnull PairListeners.IListenerBooleanObject<Bitmap> handler)
    {
        String taskId = mImageLoadingTaskId;
        if (taskId != null)
        {
            Log.wtf(getClass(),"Must not be here. There must be bug in SlowSource.");
            mBcConnector.cancelTask(taskId);
            mBcConnector.unsubscribeTask(taskId);
        }

        Intent intent = createIntentFromTask(previewUrl);
        mImageLoadingTaskId = mBcConnector.startTask(DbService.class, mLoadImageTask, intent, new IListener<Bundle>()
        {

            @Override public void handle(@Nullable Bundle result)
            {
                if (mIsDestroyed)
                {
                    Log.w(getClass(), "Get to NewsModel.Downloader.onImageLoad after destroying. Howewer I supposed we can't be here at this moment");
                    return;
                }
                mImageLoadingTaskId = null;
                Bitmap bitmap = null;
                try
                {
                    bitmap = LoadImageTask.extractBitmapFromResult(result);
                } catch (OmOutOfMemoryExeption e)
                {
                    handler.handle(bitmap != null, bitmap);
                }
            }
        });
    }

    @Override public void cancelLoadingSlowData()
    {
        String taskId = mImageLoadingTaskId;
        if (mIsDestroyed || taskId == null)
            return;
        mBcConnector.cancelTask();
        mBcConnector.unsubscribeTask(taskId);
        mImageLoadingTaskId = null;
    }

}
