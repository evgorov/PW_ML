package com.ltst.prizeword.news;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NewsModel implements INewsModel
{
    private @Nonnull String mSessionKey;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull News mNews;

    public NewsModel(@Nonnull String mSessionKey, @Nonnull IBcConnector mBcConnector)
    {
        this.mSessionKey = mSessionKey;
        this.mBcConnector = mBcConnector;
    }

    @Override public void updateFromInternet(@Nonnull IListenerVoid handler)
    {
        mInternetUpdater.update(handler);
    }

    @Nullable @Override public News getNews()
    {
        return mNews;
    }


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
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return LoadNewsFromInternetTask.class;
        }
    };

    private abstract class Updater extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Override
        protected @Nonnull Class<BcService> getServiceClass()
        {
            return BcService.class;
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