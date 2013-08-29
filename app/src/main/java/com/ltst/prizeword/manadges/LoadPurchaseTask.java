package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.08.13.
 */
public class LoadPurchaseTask implements DbService.IDbTask {

    public static final @Nonnull String BF_PURCHASE = "LoadPurchaseTask.purchase";

    final static public @Nonnull Intent createIntent(){
        return null;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {
        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {

        }
        return null;
    }

    static @Nullable Bundle getFromDatabase(@Nonnull String googleId, @Nonnull DbService.DbTaskEnv env)
    {
        @Nonnull Purchase purchase = env.dbw.getPurchaseByGoogleId(googleId);
        return packToBundle(purchase);
    }

    static @Nonnull Bundle packToBundle(@Nonnull Purchase parPurchase)
    {
        @Nonnull Bundle bundle = new Bundle();
        bundle.putParcelable(BF_PURCHASE, parPurchase);
        return bundle;
    }

}
