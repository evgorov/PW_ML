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
    public static final @Nonnull String BF_ACCESS_TOKEN = "LoadUserDataFromInternetTask.accessToken";

    public static @Nonnull
    Intent createIntent(@Nullable String sessionKey, @Nullable String providerName, @Nullable String accessToken)
    {
        Intent intent = new Intent();
        if(sessionKey != null)
            intent.putExtra(BF_SESSION_KEY, sessionKey);
        if(providerName != null)
            intent.putExtra(BF_PROVIDER_NAME, providerName);
        if (accessToken != null)
            intent.putExtra(BF_ACCESS_TOKEN, accessToken);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;
        @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nullable String providerName = extras.getString(BF_PROVIDER_NAME);
        @Nullable String accessToken = extras.getString(BF_ACCESS_TOKEN);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            if(sessionKey != null)
            {
                RestUserData response = loadRestUserDataFromInternet(sessionKey);

                if(response != null)
                {
                    UserData userData = parseUserData(providerName, response);
                    env.dbw.putUser(userData);
                    //@TODO
                    //return env.dbw.getUserFromDatabase()
                }
            }
            if (accessToken != null && providerName != null)
            {
                RestUserData.RestUserDataHolder holder = loadRestUserDataHolderFromInternet(providerName, accessToken);
                if (holder != null)
                {
//                    UserData userData = parseUserData(providerName, holder.getUserData());
//                    env.dbw.putUser(userData);
                   return getSessionKeyFromInternet(holder);
                }
            }
        }
        return null;
    }

    public static @Nullable Bundle getSessionKeyFromInternet(RestUserData.RestUserDataHolder holder)
    {
        Bundle bundle = new Bundle();
        bundle.putString(BF_SESSION_KEY, holder.getSessionKey());
        return bundle;
    }

    private @Nullable RestUserData loadRestUserDataFromInternet(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getUserData(sessionKey);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nullable RestUserData.RestUserDataHolder loadRestUserDataHolderFromInternet(@Nonnull String provider, @Nonnull String accessToken)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getSessionKey(provider, accessToken);
        }
        catch(Throwable e)
        {
            Log.i("Can't load survey from internet"); //$NON-NLS-1$
            return null;
        }
    }

    public static @Nonnull UserData parseUserData(@Nonnull String provider, @Nonnull RestUserData response)
    {
        return new UserData(0, response.getName(), response.getSurname(),
                response.getEmail(), provider, response.getBirthDate(), response.getCity(),
                response.getSolved(), response.getPosition(), response.getMonthScore(),
                response.getHighScore(), response.getDynamics(), response.getHints(),
                response.getUserpicUrl(), null);
    }
}
