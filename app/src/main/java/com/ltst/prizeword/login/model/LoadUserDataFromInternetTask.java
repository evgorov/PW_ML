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
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadUserDataFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadUserDataFromInternetTask.sessionToken";
    public static final @Nonnull String BF_PROVIDER_NAME = "LoadUserDataFromInternetTask.providerName";

    public static @Nonnull
    Intent createIntent(@Nonnull String sessionToken, @Nonnull String providerName)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionToken);
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
        String sessionKey = extras.getString(BF_SESSION_KEY);
        String providerName = extras.getString(BF_PROVIDER_NAME);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {

            RestUserData response = loadFromInternet(sessionKey);

            if(response != null)
            {
                UserData userData = parseResponse(providerName, response);
                env.dbw.putUser(userData);
            }
        }
        return null;
    }

    private @Nullable RestUserData loadFromInternet(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getUserData(sessionKey);
        }
        catch(Throwable e)
        {
            Log.i("Can't load survey from internet"); //$NON-NLS-1$
            return null;
        }
    }

    public static @Nonnull UserData parseResponse(@Nonnull String provider, @Nonnull RestUserData response)
    {
        return new UserData(0, response.getName(), response.getSurname(),
                response.getEmail(), provider, response.getBirthDate(), response.getCity(),
                response.getSolved(), response.getPosition(), response.getMonthScore(),
                response.getHighScore(), response.getDynamics(), response.getHints(),
                response.getUserpicUrl(), null);
    }
}
