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

public class LoadSessionKeyTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_STATUS_CODE = "LoadSessionKeyTask.statusCode";
    public static final @Nonnull String BF_SESSION_KEY = "LoadSessionKeyTask.sessionToken";
    public static final @Nonnull String BF_PROVIDER_NAME = "LoadSessionKeyTask.providerName";
    public static final @Nonnull String BF_ACCESS_TOKEN = "LoadSessionKeyTask.accessToken";
    public static final @Nonnull String BF_EMAIL = "LoadSessionKeyTask.email";
    public static final @Nonnull String BF_PASSWORD = "LoadSessionKeyTask.password";

    public static @Nonnull
    Intent createProviderIntent(@Nonnull String providerName, @Nonnull String accessToken)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_PROVIDER_NAME, providerName);
        intent.putExtra(BF_ACCESS_TOKEN, accessToken);
        return intent;
    }

    public static @Nonnull
    Intent createSignInIntent(@Nonnull String email, @Nonnull String password)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_EMAIL, email);
        intent.putExtra(BF_PASSWORD, password);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;
        @Nullable String providerName = extras.getString(BF_PROVIDER_NAME);
        @Nullable String accessToken = extras.getString(BF_ACCESS_TOKEN);

        @Nullable String email = extras.getString(BF_EMAIL);
        @Nullable String password = extras.getString(BF_PASSWORD);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            if (email != null && password != null)
            {
                RestUserData.RestUserDataHolder holder = loadRestUserDataHolderBySignIn(email, password);
                if (holder != null)
                {
                    return getSessionKeyFromInternet(holder);
                }
            }

            if (accessToken != null && providerName != null)
            {
                RestUserData.RestUserDataHolder holder = loadRestUserDataHolderByProvider(providerName, accessToken);
                if (holder != null)
                {
                    return getSessionKeyFromInternet(holder);
                }
            }
        }
        return null;
    }

    private @Nullable RestUserData.RestUserDataHolder loadRestUserDataHolderByProvider(@Nonnull String provider, @Nonnull String accessToken)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getSessionKeyByProvider(provider, accessToken);
        }
        catch(Throwable e)
        {
            Log.i("Can't load survey from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nullable RestUserData.RestUserDataHolder loadRestUserDataHolderBySignIn(@Nonnull String email, @Nonnull String password)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getSessionKeyByLogin(email, password);
        }
        catch(Throwable e)
        {
            Log.i("Can't load survey from internet"); //$NON-NLS-1$
            return null;
        }
    }

    public static @Nullable Bundle getSessionKeyFromInternet(RestUserData.RestUserDataHolder holder)
    {
        Bundle bundle = new Bundle();
        bundle.putString(BF_SESSION_KEY, holder.getSessionKey());
        bundle.putInt(BF_STATUS_CODE, holder.getStatusCode().value());
        return bundle;
    }
}
