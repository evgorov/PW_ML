package com.ltst.prizeword.score;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UploadScoreQueueModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull Updater mUpdater;
    private boolean mIsDestroyed;

    public UploadScoreQueueModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mUpdater = new Updater();
    }

    public void upload()
    {
        if(mIsDestroyed)
            return;
        mUpdater.update(null);
    }

    public void close()
    {
        if(mIsDestroyed)
            return;
        mIsDestroyed = true;
        mUpdater.close();
    }

    private class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return UploadScoreQueryToInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return UploadScoreQueryToInternetTask.class;
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
            // ничего не нужно обрабатывать
        }
    }

}
