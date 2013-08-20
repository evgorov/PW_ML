package com.ltst.prizeword.coefficients;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoefficientsModel implements ICoefficientsModel
{
    private @Nonnull String mSessionKey;
    private @Nonnull IBcConnector mBcConnector;
    private @Nullable Coefficients mCoefficients;

    public CoefficientsModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
    }

    @Override
    public void updateFromDatabase(@Nonnull IListenerVoid handler)
    {
        mDbUpdater.update(handler);
    }

    @Override
    public void updateFromInternet(@Nonnull IListenerVoid handler)
    {
        mInternetUpdater.update(handler);
    }

    @Nullable
    @Override
    public Coefficients getCoefficients()
    {
        return mCoefficients;
    }

    private Updater mDbUpdater = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadCoefficientsFromDbTask.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadCoefficientsFromDbTask.class;
        }
    };

    private Updater mInternetUpdater = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadCoefficientsFromInternetTask.createIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadCoefficientsFromInternetTask.class;
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

            mCoefficients = result.getParcelable(LoadCoefficientsFromInternetTask.BF_COEFFICIENTS);
        }
    }
}
