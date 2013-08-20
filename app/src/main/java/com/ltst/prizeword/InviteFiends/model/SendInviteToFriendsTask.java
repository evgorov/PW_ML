package com.ltst.prizeword.InviteFiends.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestInviteFriend;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SendInviteToFriendsTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "SendInviteToFriendsTask.sessionKey";
    public static final @Nonnull String BF_INVITE_IDS = "SendInviteToFriendsTask.inviteIds";
    public static final @Nonnull String BF_PROVIDER_NAME = "SendInviteToFriendsTask.providerName";


    public static @Nonnull Intent createIntent(@Nonnull String sessionKey, @Nonnull String ids,@Nonnull String providerName)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_INVITE_IDS, ids);
        intent.putExtra(BF_PROVIDER_NAME, providerName);
        return intent;
    }

    @Nullable
    @Override public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }
        String sessionKey = extras.getString(BF_SESSION_KEY);
        String ids = extras.getString(BF_INVITE_IDS);
        String providerName = extras.getString(BF_PROVIDER_NAME);

        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        } else
        {

            IRestClient client = RestClient.create();
            RestInviteFriend.RestInviteFriendHolder response = null;

            if(!sessionKey.equals(Strings.EMPTY) && !ids.equals(Strings.EMPTY)&& !providerName.equals(Strings.EMPTY)){
                try
                {
                    response = client.sendInviteToFriends(sessionKey,providerName,ids);
                }
                catch(Throwable e)
                {
                    Log.i("Can't load data from internet"); //$NON-NLS-1$
                    return null;
                }
            }

        }
        return null;
    }


}
