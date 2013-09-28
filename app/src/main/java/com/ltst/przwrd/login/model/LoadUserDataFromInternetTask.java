package com.ltst.przwrd.login.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.db.SQLiteHelper;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadUserDataFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadUserDataFromInternetTask.sessionKey";
    public static final @Nonnull String BF_USER_DATA = "LoadUserDataFromInternetTask.userData";

    public static @Nonnull
    Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;
        @Nonnull String sessionKey = extras.getString(BF_SESSION_KEY);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            RestUserData response = loadRestUserDataFromInternet(env.context, sessionKey);

            if(response != null)
            {
                @Nonnull UserData userData = parseUserData(response);
                @Nullable List<UserProvider> providerData = parseProviderData(response);
                env.dbw.putUser(userData, providerData);
            }
        }
        return getUserDataFromDatabase(env);
    }

    public static @Nullable Bundle getSessionKeyFromInternet(RestUserData.RestUserDataHolder holder)
    {
        Bundle bundle = new Bundle();
        bundle.putString(BF_SESSION_KEY, holder.getSessionKey());
        return bundle;
    }

    private @Nullable RestUserData loadRestUserDataFromInternet(@Nonnull Context context, @Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getUserData(sessionKey);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    public static @Nullable
    List<UserProvider> parseProviderData(RestUserData response)
    {
        List<RestUserData.RestUserProvider> providers = response.getProviders();
        if(providers == null)
            return null;
        List<UserProvider> data = new ArrayList<UserProvider>(providers.size());
        for (Iterator<RestUserData.RestUserProvider> iterator = providers.iterator(); iterator.hasNext(); )
        {
            RestUserData.RestUserProvider providerRest =  iterator.next();
            UserProvider provider = new UserProvider(0, providerRest.getName(), providerRest.getId(), providerRest.getToken(), 0);
            data.add(provider);
        }
        return data;
    }


    public static @Nonnull UserData parseUserData(@Nonnull RestUserData response)
    {
        return new UserData(0, response.getName(), response.getSurname(),
                response.getEmail(), response.getBirthDate(), response.getCity(),
                response.getSolved(), response.getPosition(), response.getMonthScore(),
                response.getHighScore(), response.getDynamics(), response.getHints(),
                response.getUserpicUrl(),null);
    }

    public static @Nullable Bundle getUserDataFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle bundle = new Bundle();
        UserData data = env.dbw.getUserById(SQLiteHelper.ID_USER);
        bundle.putParcelable(BF_USER_DATA, data);
        return bundle;
    }
}
