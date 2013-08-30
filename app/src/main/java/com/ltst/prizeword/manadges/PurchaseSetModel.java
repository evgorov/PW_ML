package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.08.13.
 */
public class PurchaseSetModel implements IPurchaseSetModel {

    private @Nonnull IBcConnector mBcConnector;
    private List<Purchase> mPurchases;

    public PurchaseSetModel(@Nonnull IBcConnector mBcConnector) {
        this.mBcConnector = mBcConnector;
    }

    @Override
    public void reloadPurchases(@Nonnull IListenerVoid handler) {
        mPurchaseReloadSession.update(handler);
    }

    @Override
    public void updatePurchase(@Nonnull Purchase purchase, @Nonnull IListenerVoid handler) {
        mPurchaseUpdateSession.update(handler);
    }

    private Updater mPurchaseReloadSession = new Updater() {
        @Nullable
        @Override
        protected Intent createIntent() {
            return LoadPurchaseTask.createReloadIntent();
        }
    };

    private Updater mPurchaseUpdateSession = new Updater() {
        @Nullable
        @Override
        protected Intent createIntent() {
            return LoadPurchaseTask.createReloadIntent();
        }
    };


    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        protected Updater(){

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
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPurchaseTask.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                mPurchases = LoadPurchaseTask.extractFromBundle(result);
            }
        }
    }
}
