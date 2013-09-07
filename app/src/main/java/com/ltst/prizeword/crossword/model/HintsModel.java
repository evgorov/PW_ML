package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HintsModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private boolean mIsDestroyed;
    private HintsUpdater mHintsUpdater;

    public HintsModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mHintsUpdater = new HintsUpdater();
    }

    public void close()
    {
        Log.i("HintsModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("HintsModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mHintsUpdater.close();

        mIsDestroyed = true;
        Log.i("HintsModel.destroy() end"); //$NON-NLS-1$
    }

    public void changeHints(final int num, @Nullable IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mHintsUpdater.setIntent(AddOrRemoveHintsTask.createIntent(mSessionKey, num));
        mHintsUpdater.update(handler);
    }


    private class HintsUpdater extends ModelUpdater<DbService.DbTaskEnv>
    {
        private @Nullable Intent mIntent;

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return AddOrRemoveHintsTask.class;
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

        }
    }
}
