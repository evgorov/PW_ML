package com.ltst.przwrd.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;

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
    private static final @Nonnull String LT_SAVE_ONE_PRODUCT_TO_DATABASE = "LoadPurchaseTask.Lt.save.one.product.to.database";
    private static final @Nonnull String LT_SAVE_PRODUCTS_TO_DATABASE = "LoadPurchaseTask.Lt.save.products.to.database";

    final static public @Nonnull Intent createLoadFromDataBaseIntent(){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_LOAD_FROM_DATABASE);
        return intent;
    }

    final static public @Nonnull Intent createUpdateOnePurchseToDataBase(@Nonnull PurchasePrizeWord purchase){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_SAVE_ONE_PRODUCT_TO_DATABASE);
        intent.putExtra(BF_ONE_PURCHASE, purchase);
        return intent;
    }

    final static public @Nonnull Intent createUpdatePurchsesToDataBase(@Nonnull ArrayList<PurchasePrizeWord> purchases){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_LOCAL_TASK, LT_SAVE_PRODUCTS_TO_DATABASE);
        intent.putParcelableArrayListExtra(LoadPurchaseTask.BF_MANY_PURCHASES, purchases);
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
            else if(task.equals(LT_SAVE_ONE_PRODUCT_TO_DATABASE))
            {
                @Nullable PurchasePrizeWord purchase = env.extras.getParcelable(BF_ONE_PURCHASE);
                env.dbw.putPurchase(purchase);
                return getFromDatabase(env);
            }
            else if(task.equals(LT_SAVE_PRODUCTS_TO_DATABASE))
            {
                @Nullable ArrayList<PurchasePrizeWord> purchases = env.extras.getParcelableArrayList(BF_MANY_PURCHASES);
                env.dbw.putPurchases(purchases);
                return getFromDatabase(env);
            }
        }
        return getFromDatabase(env);
    }

    static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        @Nullable ArrayList<PurchasePrizeWord> purchase = env.dbw.getPurchases();
        return packToBundle(purchase);
    }

    static @Nonnull Bundle packToBundle(@Nullable ArrayList<PurchasePrizeWord> purchases)
    {
        @Nonnull Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_PURCHASES, purchases);
        return bundle;
    }

    final static public @Nullable List<PurchasePrizeWord> extractFromBundle(@Nullable Bundle bundle)
    {
        if (bundle == null)
        {
            return null;
        }
        return bundle.getParcelableArrayList(BF_PURCHASES);
    }
}
