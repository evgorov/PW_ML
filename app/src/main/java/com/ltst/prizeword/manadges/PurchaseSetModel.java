package com.ltst.prizeword.manadges;

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

/**
 * Created by cosic on 29.08.13.
 */
public class PurchaseSetModel implements IPurchaseSetModel {

    private @Nonnull IBcConnector mBcConnector;

    public PurchaseSetModel(@Nonnull IBcConnector mBcConnector) {
        this.mBcConnector = mBcConnector;
    }

    @Override
    public void reloadPurchases(@Nonnull IListenerVoid handler) {

    }

    @Override
    public void updatePurchase(ManadgeHolder.ManadgeProduct product, @Nonnull IListenerVoid handler) {
        mPurchaseDbUpdater.update(handler);
    }

    private Updater mPurchaseDbUpdater = new Updater() {
        @Nullable
        @Override
        protected Intent createIntent() {
            return LoadPurchaseTask.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPurchaseTask.class;
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

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
        }
    }
}
