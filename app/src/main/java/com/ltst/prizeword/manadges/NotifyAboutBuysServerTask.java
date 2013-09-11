package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestPuzzleSet;
import com.ltst.prizeword.rest.RestPuzzleTotalSet;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 02.09.13.
 */
public class NotifyAboutBuysServerTask implements DbService.IDbTask {

    public static final @Nonnull String BF_SET_SERVER_ID = "NotifyAboutBuysServerTask.set.server.id";
    public static final @Nonnull String BF_RECEIPT_DATA = "NotifyAboutBuysServerTask.receipt.data";

    final static public @Nonnull Intent createReloadIntent(@Nonnull String setServerId, @Nonnull String receiptData){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_SET_SERVER_ID, setServerId);
        intent.putExtra(BF_RECEIPT_DATA, receiptData);
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
            @Nullable String setServerId = env.extras.getString(BF_SET_SERVER_ID);
            @Nullable String receiptData = env.extras.getString(BF_RECEIPT_DATA);
            if (setServerId == null || receiptData == null)
            {
                return null;
            }

            RestPuzzleSet.RestPuzzleSetsHolder holder = loadRestPuzzleSetFromInternet(setServerId, receiptData);
        }
        return null;
    }

    private @Nullable
    RestPuzzleSet.RestPuzzleSetsHolder loadRestPuzzleSetFromInternet(@Nonnull String setServerId, @Nonnull String receiptData)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.postBuySet(setServerId, receiptData);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

}
