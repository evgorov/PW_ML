package com.ltst.prizeword.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.login.model.LoadUserDataFromInternetTask;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AddOrRemoveHintsTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "AddOrRemoveHintsTask.sessionKey";
    public static final @Nonnull String BF_HINTS_TO_CHANGE = "AddOrRemoveHintsTask.hintsToChange";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey, int hintsToChange)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_HINTS_TO_CHANGE, hintsToChange);
        return  intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }
        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
            int hintsToChange = extras.getInt(BF_HINTS_TO_CHANGE);

            if (sessionKey != null)
            {
                RestUserData.RestUserDataHolder holder = changeHints(env.context, sessionKey, hintsToChange);
                if (holder != null)
                {
                    @Nonnull UserData userData = LoadUserDataFromInternetTask.parseUserData(holder.getUserData());
                    @Nullable List<UserProvider> providerData = LoadUserDataFromInternetTask.parseProviderData(holder.getUserData());
                    env.dbw.putUser(userData, providerData);
                }
            }
        }
        return null;
    }

    private RestUserData.RestUserDataHolder changeHints (@Nonnull Context context, @Nonnull String sessionKey, int hints)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.addOrRemoveHints(sessionKey, hints);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }
}
