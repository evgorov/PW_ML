package com.ltst.prizeword.manadges;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.LoadPuzzleSetsFromInternet;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.rest.RestPuzzleSet;
import com.ltst.prizeword.rest.RestPuzzleTotalSet;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import java.security.Signature;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 02.09.13.
 */
public class BuyCrosswordSetOnServerTask implements DbService.IDbTask {

    private static final @Nonnull String BF_SESSION_KEY = "BuyCrosswordSetOnServerTask.sessionKey";
    private static final @Nonnull String BF_SET_SERVER_ID = "BuyCrosswordSetOnServerTask.setServerId";
    private static final @Nonnull String BF_RECEIPT_DATA = "BuyCrosswordSetOnServerTask.receiptData";
    private static final @Nonnull String BF_SIGNATURE = "BuyCrosswordSetOnServerTask.signature";

    final static public @Nonnull Intent createBuyCrosswordSetIntent(@Nonnull String sessionKey,
                                                                    @Nonnull String setServerId,
                                                                    @Nonnull String receiptData,
                                                                    @Nonnull String signature){
        @Nonnull Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_SET_SERVER_ID, setServerId);
        intent.putExtra(BF_RECEIPT_DATA, receiptData);
        intent.putExtra(BF_SIGNATURE, signature);
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
            @Nullable String sessionKey =   env.extras.getString(BF_SESSION_KEY);
            @Nullable String setServerId =  env.extras.getString(BF_SET_SERVER_ID);
            @Nullable String receiptData =  env.extras.getString(BF_RECEIPT_DATA);
            @Nullable String signature =    env.extras.getString(BF_SIGNATURE);
            if (setServerId == Strings.EMPTY || receiptData == Strings.EMPTY || signature == Strings.EMPTY)
            {
                return null;
            }

            RestPuzzleSet.RestPuzzleSetsHolder data = loadRestPuzzleSetFromInternet(env.context, setServerId, receiptData, signature);
            if(data != null)
            {
                if(data.getHttpStatus() == HttpStatus.valueOf(RestParams.SC_SUCCESS))
                {
                    return null;
                }
//            if (data != null)
//            {
//                ArrayList<PuzzleSet> sets = LoadPuzzleSetsFromInternet.extractFromRest(data);
//                env.dbw.putPuzzleSetList(sets);
//                return LoadPuzzleSetsFromInternet.getFromDatabase(env);
//            }
            }
        }
        return null;
    }

    private @Nullable
    RestPuzzleSet.RestPuzzleSetsHolder loadRestPuzzleSetFromInternet(@Nonnull Context context, @Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.postBuySet(setServerId, receiptData, signature);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

}
