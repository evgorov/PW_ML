package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadUserDataFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_TOKEN = "LoadUserDataFromInternetTask.sessionToken";
    public static final @Nonnull String BF_PROVIDER_NAME = "LoadUserDataFromInternetTask.providerName";

    public static @Nonnull
    Intent createIntent(@Nonnull String sessionToken, @Nonnull String providerName)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_TOKEN, sessionToken);
        intent.putExtra(BF_PROVIDER_NAME, providerName);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;
        String sessionToken = extras.getString(BF_SESSION_TOKEN);
        String providerName = extras.getString(BF_PROVIDER_NAME);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {

//            RestSurveysOnePollItem response = loadFromInternet(surveyServerId);
//
//            if(response != null)
//            {
//                OnePoll poll = parseResponse(pollId, response);
//                env.dbw.putOnePoll(poll);
//            }
        }
        return null;
    }
}
