package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.08.13.
 */
public class PurchaseSetModel implements IPurchaseSetModel
{

    private @Nonnull IBcConnector mBcConnector;
    private @Nullable List<Purchase> mPurchases;

    private boolean mIsDestroyed;

    public PurchaseSetModel(@Nonnull IBcConnector mBcConnector)
    {
        this.mBcConnector = mBcConnector;
    }

    public
    @Nullable
    Purchase getPurchase(@Nullable String googleId)
    {
        if (googleId == null || mPurchases == null)
        {
            return null;
        }
        for (Purchase purchase : mPurchases)
        {
            if (purchase.googleId.equals(googleId))
            {
                return purchase;
            }
        }
        return new Purchase();
    }

    @Override
    public void close()
    {
        Log.i("OnePuzzleModel.destroy() begin"); //$NON-NLS-1$
        if (mIsDestroyed)
        {
            Log.w("OnePuzzleModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mPurchaseReloadSession.close();
        mPurchaseUpdateSession.close();

        mIsDestroyed = true;
        Log.i("OnePuzzleModel.destroy() end"); //$NON-NLS-1$
    }

    @Override
    public void reloadPurchases(@Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mPurchaseReloadSession.update(handler);
    }

    @Override
    public void putPurchase(@Nonnull Purchase purchase, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mPurchaseUpdateSession.update(handler);
    }

    private Updater mPurchaseReloadSession = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadPurchaseTask.createReloadIntent();
        }
    };

    private Updater mPurchaseUpdateSession = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadPurchaseTask.createReloadIntent();
        }
    };


    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        protected Updater()
        {

        }

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

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPurchaseTask.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result != null)
            {
                mPurchases = LoadPurchaseTask.extractFromBundle(result);
            }
        }
    }
}
