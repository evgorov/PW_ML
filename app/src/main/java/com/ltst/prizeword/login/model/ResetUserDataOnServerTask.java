package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 24.07.13.
 */
public class ResetUserDataOnServerTask implements DbService.IDbTask {

    public static final @Nonnull String BF_SESSION_KEY = "LoadUserDataFromInternetTask.sessionKey";
    public static final @Nonnull String BF_USER_DATA = "ResetUserDataOnServerTask.userData";
    public static final @Nonnull String BF_USER_SENDER = "ResetUserDataOnServerTask.userSender";

    public static @Nonnull
    android.content.Intent createIntent(@Nonnull String sessionKey, byte[] userPic)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_USER_SENDER, userPic);
        return intent;
    }

    @Override
    public Bundle execute(DbService.DbTaskEnv env) {
        Log.d(NavigationActivity.LOG_TAG, "COME!");
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
            String sessionKey = extras.getString(BF_SESSION_KEY);
            byte[] userPic = extras.getByteArray(BF_USER_SENDER);

            if(sessionKey != Strings.EMPTY && userPic != null){
                IRestClient client = RestClient.create();
                RestUserData response = client.resetUserPic(sessionKey, userPic);
                if(response != null)
                {
                    @Nonnull UserData userData = parseUserData(response);
                    @Nullable List<UserProvider> providerData = parseProviderData(response);
                    env.dbw.putUser(userData, providerData);
                    return getUserDataFromDatabase(env, userData.email);
                }
            }
        }
        return null;
    }

    public static @Nonnull UserData parseUserData(@Nonnull RestUserData response)
    {
        return new UserData(0, response.getName(), response.getSurname(),
                response.getEmail(), response.getBirthDate(), response.getCity(),
                response.getSolved(), response.getPosition(), response.getMonthScore(),
                response.getHighScore(), response.getDynamics(), response.getHints(),
                response.getUserpicUrl(), null);
    }

    private @Nullable
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

    public static @Nullable Bundle getUserDataFromDatabase(@Nonnull DbService.DbTaskEnv env, @Nonnull String email)
    {
        Bundle bundle = new Bundle();
        UserData data = env.dbw.getUserByEmail(email);
        bundle.putParcelable(BF_USER_DATA, data);
        return bundle;
    }

}
