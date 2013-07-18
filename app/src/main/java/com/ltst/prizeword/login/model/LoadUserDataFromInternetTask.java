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
            RestUserData response = loadRestUserDataFromInternet(sessionKey);

            if(response != null)
            {
//                UserData userData = parseUserData(response);
//                env.dbw.putUser(userData);
                //@TODO
                //return env.dbw.getUserFromDatabase()
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


    public static @Nonnull UserData parseUserData(@Nonnull RestUserData response)
    {
        return new UserData(0, response.getName(), response.getSurname(),
                response.getEmail(), response.getBirthDate(), response.getCity(),
                response.getSolved(), response.getPosition(), response.getMonthScore(),
                response.getHighScore(), response.getDynamics(), response.getHints(),
                response.getUserpicUrl(), null);
    }
}
