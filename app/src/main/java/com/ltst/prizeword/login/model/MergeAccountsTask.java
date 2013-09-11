package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 05.08.13.
 */
public class MergeAccountsTask implements DbService.IDbTask {

    public static final @Nonnull String BF_MERGE_SESSION_KEY1 = "MergeAccountsTask.sessionKey1";
    public static final @Nonnull String BF_MERGE_SESSION_KEY2 = "MergeAccountsTask.sessionKey2";
    public static final @Nonnull String BF_STATUS_CODE = "MergeAccountsTask.statusCode";
    public static final @Nonnull String BF_STATUS_MESSAGE = "MergeAccountsTask.statusMessage";


    public static @Nonnull
    Intent createIntentMergeAccounts(@Nonnull String sessionKey1, @Nonnull String sessionKey2)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_MERGE_SESSION_KEY1, sessionKey1);
        intent.putExtra(BF_MERGE_SESSION_KEY2, sessionKey2);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {
        Bundle extras = env.extras;
        if(extras == null)
            return null;

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            String sessionKey1 = extras.getString(BF_MERGE_SESSION_KEY1);
            String sessionKey2 = extras.getString(BF_MERGE_SESSION_KEY2);

            if(sessionKey1 != Strings.EMPTY && sessionKey2 != Strings.EMPTY){
                IRestClient client = RestClient.create(env.context);
                RestUserData.RestAnswerMessageHolder response = client.mergeAccounts(sessionKey1, sessionKey2);
                return getAnswerMessage(response);
            }
        }
        return null;
    }

    public static @Nullable Bundle getAnswerMessage(@Nonnull RestUserData.RestAnswerMessageHolder answer)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(BF_STATUS_CODE, answer.getStatusCode().value());
        bundle.putString(BF_STATUS_MESSAGE, answer.getMessage());
        return bundle;
    }
}
