package com.ltst.prizeword.InviteFiends.model;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.dowloading.BgImageDownloader;
import com.ltst.prizeword.dowloading.LoadImageTask;
import com.ltst.prizeword.login.model.LoadFriendsImageTask;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ArraySlowSource;
import org.omich.velo.lists.ISlowSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsDataModel implements IInviteFriendsDataModel
{
    private @Nonnull Context mContext;
    private boolean mIsDestroyed;

    private @Nullable Source mSource;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull BgImageDownloader mDownloader;

    public InviteFriendsDataModel(@Nonnull Context context,@Nonnull IBcConnector mBcConnector)
    {
        this.mContext = context;
        this.mBcConnector = mBcConnector;

        BgImageDownloader downloader = new FriendsImageDownloader(mBcConnector, LoadFriendsImageTask.class);
        mDownloader = downloader;
        mSource = new Source(new ArrayList<ISlowSource.Item<InviteFriendsData, Bitmap>>(), downloader);
        mIsDestroyed = false;
    }

    /**
     * Класс ответственнен за то, чтобы сделать recycle() картинкам,
     * которые внутри него.
     *
     * Поэтому мы должны реализовать метод destroy();
     */
    public void close()
    {
        //Log.i(getClass(), "NewsModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            //Log.w(getClass(), "NewsModel.destroy() called more than once"); //$NON-NLS-1$

        }

        session.close();

        mSource.close();
        mDownloader.destroy();

        mIsDestroyed = true;
        //Log.i(getClass(), "NewsModel.destroy() end")
    }

    public void pauseLoading()
    {
        mSource.pauseResource(false);
    }

    public void resumeLoading()
    {

        if (mSource != null)
        {
            mSource.resumeResource();
        }
    }

    //==== INewsModel ========================================================



    @Nonnull @Override public ISlowSource<InviteFriendsData, Bitmap> getSource()
    {
        return mSource;
    }

    public void updateDataByInternet(@Nonnull IListenerVoid updateHandler)
    {
        if(mIsDestroyed)
            return;

        session.update(updateHandler);
    }


    //==========================================================================
    private final Updater session = new Updater()
    {
        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
                return LoadFriendsDataFromInternetTask.class;
        }

        @Nonnull
        @Override
        protected Intent createIntent()
        {
            final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
            Log.d(NavigationActivity.LOG_TAG, "RELOAD USERDATA SessionKey = " + sessionKey);
                return LoadFriendsDataFromInternetTask.createIntent(sessionKey);
        }
    };



    public void sendInviteFriends( final String ids, final String providerName, @Nonnull IListenerVoid handler) {

        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Inviter session = new Inviter() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return SendInviteToFriendsTask.createIntent(sessionKey,ids,providerName);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return SendInviteToFriendsTask.class;
            }

            @Override protected void handleData(@Nullable Bundle result)
            {

            }

        };
        session.update(handler);
    }



    //========================================================================


    /**
     * Загрузчик. Обновляет список новостей по требованию извне.
     *
     * Может быть двух видов: долгкий из интернета или быстрый из базы данных.
     */
    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            Source source = mSource;
            BgImageDownloader downloader = mDownloader;
            if(mIsDestroyed)
                return;
            List<ISlowSource.Item<InviteFriendsData,Bitmap>> list;
                list = LoadFriendsDataFromInternetTask.extractFriendsFromBundle(result);

            if(list != null)
            {
                source.close();
                mSource = new Source(list, downloader);
            }
        }

    }

    private abstract class Inviter extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }



    }

    /**
     * Передаётся в адаптер, отвечает за поочерёдную подгрузку картинок.
     */
    private static class Source extends ArraySlowSource<InviteFriendsData, Bitmap, String>
    {
        public Source(@Nonnull Collection<Item<InviteFriendsData, Bitmap>> items, @Nonnull BgImageDownloader downloader)
        {
            super(items, downloader);
        }

        @Override
        protected long getItemId(InviteFriendsData quick, int position)
        {
            return 0;
        }

        @Override
        protected @Nonnull String getSlowParam(InviteFriendsData quick, int position)
        {
            return quick.userpic;
        }

        @Override
        protected void destroyQuickItem(@Nonnull InviteFriendsData quick)
        {
            // NewsItem doesn't contain anything for destroying
        }

        @Override
        protected void destroySlowItem(@Nonnull Bitmap slowItem)
        {
            slowItem.recycle();
        }
    }

    private static class FriendsImageDownloader extends BgImageDownloader
    {

        public FriendsImageDownloader(@Nonnull IBcConnector mBcConnector, @Nonnull Class<? extends LoadImageTask> mLoadImageTask)
        {
            super(mBcConnector, mLoadImageTask);
        }

        @Nonnull @Override public Intent createIntentFromTask(@Nonnull String previewImageUrl)
        {
            return LoadFriendsImageTask.createIntent(previewImageUrl);
        }
    }
}
