package com.ltst.prizeword.rating.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.dowloading.BgImageDownloader;
import com.ltst.prizeword.dowloading.LoadImageTask;

import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ArraySlowSource;
import org.omich.velo.lists.ILoadingQueue;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UsersListModel implements IUsersListModel
{
    private boolean mIsDestroyed;
    private @Nonnull String mSessionKey;
    private @Nonnull Source mSource;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull BgImageDownloader mDownloader;
    private @Nonnull Updater mUsersUpdater;

    public UsersListModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        Log.i("UsersListModel()"); //$NON-NLS-1$
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
        BgImageDownloader downloader = new BgImageDownloader.SimpleBgImageDownloader(mBcConnector, LoadImageTask.class);
        mDownloader = downloader;
        mSource = new Source(new ArrayList<ISlowSource.Item<UsersList.User, Bitmap>>(), downloader);
        mUsersUpdater = new Updater();
    }

    public void close()
    {
        Log.i("UsersListModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("UsersListModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mUsersUpdater.close();

        mSource.close();
        mDownloader.destroy();

        mIsDestroyed = true;
        Log.i("UsersListModel.destroy() end"); //$NON-NLS-1$
    }

    public void pauseLoading()
    {
        mSource.pauseResource(false);
    }

    public void resumeLoading()
    {
        mSource.resumeResource();
    }

    // ==== IUsersListModel

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        mUsersUpdater.update(handler);
    }

    @Nonnull
    @Override
    public ISlowSource<UsersList.User, Bitmap> getSource()
    {
        return mSource;
    }

    private class Updater extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        //==== ModelUpdater ===================================================
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadUsersFromInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return LoadUsersFromInternetTask.class;
        }

        @Override
        protected @Nonnull IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Override
        protected @Nonnull Class<BcService> getServiceClass()
        {
            return BcService.class;
        }

        @Override
        protected void handleData (@Nullable Bundle result)
        {
            Source source = mSource;
            BgImageDownloader downloader = mDownloader;
            if(mIsDestroyed)
                return;

            List<ISlowSource.Item<UsersList.User, Bitmap>> list = LoadUsersFromInternetTask.extractUsersList(result);

            if(list != null)
            {
                source.close();
                mSource = new Source(list, downloader);
            }
        }
    }

    /**
     * Передаётся в адаптер, отвечает за поочерёдную подгрузку картинок.
     */
    private static class Source extends ArraySlowSource<UsersList.User, Bitmap, String>
    {
        private Source(@Nonnull Collection<Item<UsersList.User, Bitmap>> items, @Nonnull ILoadingQueue.IBgDownloader<String, Bitmap> bgDownloader)
        {
            super(items, bgDownloader);
        }

        @Override
        protected long getItemId(UsersList.User user, int position)
        {
            return user.position - 1;
        }

        @Nullable
        @Override
        protected String getSlowParam(UsersList.User user, int position)
        {
            return user.previewUrl;
        }

        @Override
        protected void destroyQuickItem(@Nonnull UsersList.User user)
        {

        }

        @Override
        protected void destroySlowItem(@Nonnull Bitmap slowItem)
        {
            slowItem.recycle();
        }
    }

}
