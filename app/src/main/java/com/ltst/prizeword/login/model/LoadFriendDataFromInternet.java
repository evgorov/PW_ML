package com.ltst.prizeword.login.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.db.SQLiteHelper;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestInvite;
import com.ltst.prizeword.rest.RestInviteFriend;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.rest.RestPuzzle;
import com.ltst.prizeword.rest.RestUserData;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadFriendDataFromInternet implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadFriendDataFromInternet.sessionKey";
    public static final @Nonnull String BF_FRIEND_DATA = "LoadFriendDataFromInternet.friendData";

    public static @Nonnull Intent createIntent(@Nonnull String sessionKey)
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
        if (extras == null)
        {
            return null;
        }
        @Nonnull String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nonnull String friendData = extras.getString(BF_FRIEND_DATA);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            RestInvite.RestInviteHolder holder = loadRestFriendDataFromInternet(sessionKey);
            if (holder != null)
            {
                ArrayList<InviteFriendsData> friends = parseFriendData(holder);
                if (friends != null)
                {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(BF_FRIEND_DATA, friends);
                    return bundle;
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

    private @Nullable
    RestInvite.RestInviteHolder loadRestFriendDataFromInternet(@Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getFriendsData(sessionKey, RestParams.VK_PROVIDER);
        } catch (Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }


    public static @Nullable
    ArrayList<InviteFriendsData> parseFriendData(@Nonnull RestInvite.RestInviteHolder holder)
    {
        RestInvite invite = holder.getFriends();
        if (invite != null)
        {
            List<RestInviteFriend> friendsList = invite.getFriends();
            ArrayList<InviteFriendsData> friends = new ArrayList<InviteFriendsData>(friendsList.size());
            for (RestInviteFriend restF : friendsList)
            {
                InviteFriendsData f = new InviteFriendsData(restF.getProvider(), restF.getFriendsId(), restF.getName(), restF.getEmail(), restF.isInviteSend(), restF.isInviteUsed(), restF.getInvitedAt(), null);
                friends.add(f);
            }
            return friends;
        }
        return null;
    }

}
