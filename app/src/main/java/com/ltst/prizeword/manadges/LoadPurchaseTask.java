package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 29.08.13.
 */
public class LoadPurchaseTask implements DbService.IDbTask {

    public static final @Nonnull String BF_PURCHASES = "LoadPurchaseTask.purchase";
    public static final @Nonnull String BF_ONE_PURCHASE = "LoadPurchaseTask.one.purchase";
    private static final @Nonnull String BF_LOCAL_TASK = "LoadPurchaseTask.localtask";

    private static final @Nonnull String LT_RELOAD = "LoadPurchaseTask.Lt.reload";
    private static final @Nonnull String LT_UPDATE = "LoadPurchaseTask.Lt.reload";

    final static public @Nonnull Intent createReloadIntent(){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_RELOAD);
        return intent;
    }

    final static public @Nonnull Intent createUpdateIntent(){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_UPDATE);
        return intent;
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
            @Nullable String task = env.extras.getString(BF_LOCAL_TASK);
            if(task.equals(LT_RELOAD))
            {
                return getFromDatabase(env);
            }
            else if(task.equals(LT_UPDATE))
            {
                @Nullable Purchase purchase = env.extras.getParcelable(BF_ONE_PURCHASE);
                env.dbw.putPurchase(purchase);
                return getFromDatabase(env);
            }
        }
        return null;
    }

    static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        @Nonnull ArrayList<Purchase> purchase = env.dbw.getPurchases();
        return packToBundle(purchase);
    }

    static @Nonnull Bundle packToBundle(@Nonnull ArrayList<Purchase> purchases)
    {
        @Nonnull Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PURCHASES, purchases);
        return bundle;
    }

    final static public @Nullable List<Purchase> extractFromBundle(@Nullable Bundle bundle)
    {
        if (bundle == null)
        {
            return null;
        }
        return bundle.getParcelableArrayList(BF_PURCHASES);
    }
}
