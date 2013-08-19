package com.ltst.prizeword.rating.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestPuzzleUsers;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadUsersFromInternetTask implements IBcTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadUsersFromInternetTask.sessionKey";
    public static final @Nonnull String BF_USERS = "LoadUsersFromInternetTask.users";

    public static final @Nonnull
    Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }
    @Nullable
    @Override
    public Bundle execute(@Nonnull BcTaskEnv env)
    {
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
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);

            if (sessionKey != null)
            {
                @Nullable RestPuzzleUsers restUsers = loadUsers(sessionKey);
                if (restUsers != null)
                {
                    @Nonnull UsersList users = parseUsers(restUsers);
                    return packToBundle(users);
                }
            }
        }
        return null;
    }

    private @Nullable RestPuzzleUsers loadUsers(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getUsers(sessionKey);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private @Nonnull UsersList parseUsers (@Nonnull RestPuzzleUsers restUsers)
    {
        RestUserData meRest = restUsers.getMe();
        UsersList.User me = new UsersList.User(meRest.getId(), meRest.getName(), meRest.getSurname(), meRest.getCity(),
                meRest.getSolved(), meRest.getPosition(), meRest.getHighScore(), meRest.getHighScore(), meRest.getDynamics(), meRest.getUserpicUrl(), null);
        List<UsersList.User> users = new ArrayList<UsersList.User>();
        for (RestUserData rest : restUsers.getUsers())
        {
            UsersList.User user = new UsersList.User(rest.getId(), rest.getName(), rest.getSurname(), rest.getCity(),
                    rest.getSolved(), rest.getPosition(), rest.getHighScore(), rest.getHighScore(), rest.getDynamics(), rest.getUserpicUrl(), null);
            users.add(user);
        }
        return new UsersList(me, users);
    }

    private @Nonnull Bundle packToBundle(@Nonnull UsersList users)
    {
        Bundle b = new Bundle();
        b.putParcelable(BF_USERS, users);
        return b;
    }
}
