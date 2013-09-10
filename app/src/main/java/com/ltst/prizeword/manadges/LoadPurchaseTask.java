package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.db.SQLiteHelper;

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
    public static final @Nonnull String BF_MANY_PURCHASES = "LoadPurchaseTask.many.purchases";
    public static final @Nonnull String BF_LOCAL_TASK = "LoadPurchaseTask.localtask";

    private static final @Nonnull String LT_LOAD_FROM_DATABASE = "LoadPurchaseTask.Lt.load.database";
    public static final @Nonnull String LT_LOAD_FROM_GOOGLEPLAY = "LoadPurchaseTask.Lt.load.googleplay";
    public static final @Nonnull String LT_SAVE_FROM_GOOGLEPLAY = "LoadPurchaseTask.Lt.save.googleplay";

    final static public @Nonnull Intent createLoadFromDataBaseIntent(){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_LOAD_FROM_DATABASE);
        return intent;
    }

    final static public @Nonnull Intent createLoadFromGooglePlayIntent(@Nonnull Purchase purchase){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_LOAD_FROM_GOOGLEPLAY);
        intent.putExtra(BF_ONE_PURCHASE, purchase);
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
            if(task.equals(LT_LOAD_FROM_DATABASE))
            {
                return getFromDatabase(env);
            }
            else if(task.equals(LT_LOAD_FROM_GOOGLEPLAY))
            {
                @Nullable Purchase purchase = env.extras.getParcelable(BF_ONE_PURCHASE);
                env.dbw.putPurchase(purchase);
                return getFromDatabase(env);
            }
            else if(task.equals(LT_SAVE_FROM_GOOGLEPLAY))
            {
                @Nullable List<Purchase> purchases = env.extras.getParcelableArrayList(BF_ONE_PURCHASE);
                env.dbw.putPurchases(purchases);
                return getFromDatabase(env);
            }
        }
        return null;
    }

    static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        @Nullable ArrayList<Purchase> purchase = env.dbw.getPurchases();
        return packToBundle(purchase);
    }

    static @Nonnull Bundle packToBundle(@Nullable ArrayList<Purchase> purchases)
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
