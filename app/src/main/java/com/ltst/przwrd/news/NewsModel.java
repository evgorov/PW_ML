package com.ltst.przwrd.news;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NewsModel implements INewsModel
{
    private @Nonnull String mSessionKey;
    private @Nonnull IBcConnector mBcConnector;
    private @Nullable News mNews;
    private boolean mIsDestroyed;

    public NewsModel(@Nonnull String mSessionKey, @Nonnull IBcConnector mBcConnector)
    {
        this.mSessionKey = mSessionKey;
        this.mBcConnector = mBcConnector;
    }

    @Override
    public void updateFromInternet(@Nonnull IListenerVoid handler)
    {
        mInternetUpdater.update(handler);
    }

    @Nullable
    @Override
    public News getNews()
    {
        return mNews;
    }

    @Override
    public void closeHolder()
    {
        mCloseNewsUpdater.update(null);
    }

    @Override
    public void close()
    {
        Log.i("NewsModel.destroy() begin"); //$NON-NLS-1$
        if (mIsDestroyed)
        {
            Log.w("NewsModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mInternetUpdater.close();
        mCloseNewsUpdater.close();

        mIsDestroyed = true;
        Log.i("NewsModel.destroy() end"); //$NON-NLS-1$

    }

    private Updater mCloseNewsUpdater=  new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return CloseNewsTask.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return CloseNewsTask.class;
        }
    };

    private Updater mInternetUpdater = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadNewsFromInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadNewsFromInternetTask.class;
        }
    };

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
            if (result == null)
            {
                return;
            }

            mNews = result.getParcelable(LoadNewsFromInternetTask.BF_NEWS);
        }
    }

}
