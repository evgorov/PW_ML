package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadSessionKeyTask implements IBcTask
{
    public static final @Nonnull String BF_STATUS_CODE = "LoadSessionKeyTask.statusCode";
    public static final @Nonnull String BF_SESSION_KEY = "LoadSessionKeyTask.sessionToken";
    public static final @Nonnull String BF_PROVIDER_NAME = "LoadSessionKeyTask.providerName";
    public static final @Nonnull String BF_ACCESS_TOKEN = "LoadSessionKeyTask.accessToken";
    public static final @Nonnull String BF_EMAIL = "LoadSessionKeyTask.email";
    public static final @Nonnull String BF_PASSWORD = "LoadSessionKeyTask.password";

    public static final @Nonnull String BF_NAME = "LoadSessionKeyTask.name";
    public static final @Nonnull String BF_SURNAME = "LoadSessionKeyTask.surname";
    public static final @Nonnull String BF_BIRTHDATE = "LoadSessionKeyTask.birthdate";
    public static final @Nonnull String BF_CITY = "LoadSessionKeyTask.city";
    public static final @Nonnull String BF_USERPIC = "LoadSessionKeyTask.userpic";

    private static final @Nonnull String BF_SIGNUP_FLAG = "LoadSessionKeyTask.signUp";

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

    public static @Nonnull
    Intent createSignUpIntent(@Nonnull String email,
                              @Nonnull String name,
                              @Nonnull String surname,
                              @Nonnull String password,
                              @Nullable String birthdate,
                              @Nullable String city,
                              @Nullable byte[] userpic)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_EMAIL, email);
        intent.putExtra(BF_NAME, name);
        intent.putExtra(BF_SURNAME, surname);
        intent.putExtra(BF_PASSWORD, password);
        if (birthdate != null)
        {
            intent.putExtra(BF_BIRTHDATE, birthdate);
        }
        if (city != null)
        {
            intent.putExtra(BF_CITY, city);
        }
        if (userpic != null)
        {
            intent.putExtra(BF_USERPIC, userpic);
        }
        intent.putExtra(BF_SIGNUP_FLAG, true);
        return intent;
    }


        @Nullable
    @Override
    public Bundle execute(@Nonnull BcTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;
        @Nullable String providerName = extras.getString(BF_PROVIDER_NAME);
        @Nullable String accessToken = extras.getString(BF_ACCESS_TOKEN);

        @Nullable String email = extras.getString(BF_EMAIL);
        @Nullable String password = extras.getString(BF_PASSWORD);

        boolean signUpFlag = extras.getBoolean(BF_SIGNUP_FLAG);
        @Nullable String name = extras.getString(BF_NAME);
        @Nullable String surname = extras.getString(BF_SURNAME);
        @Nullable String birthdate = extras.getString(BF_BIRTHDATE);
        @Nullable String city = extras.getString(BF_CITY);
        @Nullable byte[] userpic = extras.getByteArray(BF_USERPIC);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            if(signUpFlag && email != null && password != null && name != null && surname != null)
            {
                RestUserData.RestUserDataHolder holder = loadRestUserDataHolderBySignUp(email, name, surname,
                                                                    password, birthdate, city, userpic);
                if (holder != null)
                {
                    return getSessionKeyFromInternet(holder);
                }
            }

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

    private @Nullable RestUserData.RestUserDataHolder loadRestUserDataHolderBySignUp(@Nonnull String email,
                                                                                     @Nonnull String name,
                                                                                     @Nonnull String surname,
                                                                                     @Nonnull String password,
                                                                                     @Nullable String birthdate,
                                                                                     @Nullable String city,
                                                                                     @Nullable byte[] userpic)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getSessionKeyBySignUp(email, name, surname, password, birthdate, city, userpic);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
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
            Log.i("Can't load data from internet"); //$NON-NLS-1$
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
            Log.i("Can't load data from internet"); //$NON-NLS-1$
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
