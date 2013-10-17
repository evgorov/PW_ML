package com.ltst.przwrd.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.db.DbService;

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
    private @Nullable List<PurchasePrizeWord> mPurchases;
//    private @Nonnull PurchasePrizeWord mPurchaseDone;

    private boolean mIsDestroyed;

    public PurchaseSetModel(@Nonnull IBcConnector mBcConnector)
    {
        this.mBcConnector = mBcConnector;
    }

    public
    @Nonnull
    PurchasePrizeWord getPurchase(@Nullable String googleId)
    {
        if (googleId != null && mPurchases != null)
        {
            for (PurchasePrizeWord purchase : mPurchases)
            {
                if (purchase.googleId.equals(googleId))
                {
                    return purchase;
                }
            }
        }
        PurchasePrizeWord purchase = new PurchasePrizeWord();
        purchase.googleId = googleId;
        return purchase;
    }

    @Override
    public void close()
    {
        Log.i("OnePuzzleModel.destroy() begin"); //$NON-NLS-1$
        if (mIsDestroyed)
        {
            Log.w("OnePuzzleModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mReloadFromDataBaseSession.close();
        mReloadFromGooglePlaySession.close();

        mIsDestroyed = true;
        Log.i("OnePuzzleModel.destroy() end"); //$NON-NLS-1$
    }

    @Override
    public void reloadPurchases(@Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;
        mReloadFromDataBaseSession.update(handler);
        mReloadFromGooglePlaySession.update(handler);
    }

    @Override
    public void putOnePurchase(@Nonnull PurchasePrizeWord purchase, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;

        for(int i=0; i<mPurchases.size(); i++)
        {
            PurchasePrizeWord product = mPurchases.get(i);
            if (product.googleId.equals(purchase.googleId))
            {
                mPurchases.remove(i);
                mPurchases.add(purchase);
                break;
            }
        }

        @Nonnull Intent intent = LoadPurchaseTask.createUpdateOnePurchseToDataBase(purchase);
        mReloadFromGooglePlaySession.setIntent(intent);
        mReloadFromGooglePlaySession.update(handler);
    }

    @Override
    public void putPurchases(@Nonnull ArrayList<PurchasePrizeWord> purchases, @Nonnull IListenerVoid handler)
    {
        if (mIsDestroyed)
            return;

        @Nonnull Intent intent = LoadPurchaseTask.createUpdatePurchsesToDataBase(purchases);
        mReloadFromGooglePlaySession.setIntent(intent);
        mReloadFromGooglePlaySession.update(handler);
    }

    private Updater mReloadFromDataBaseSession = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return LoadPurchaseTask.createLoadFromDataBaseIntent();
        }
    };

    private Updater mReloadFromGooglePlaySession = new Updater()
    {
        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }
    };


    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        protected  @Nonnull Intent mIntent;

        protected Updater()
        {

        }

        public void setIntent(@Nonnull Intent intent)
        {
            mIntent = intent;
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
