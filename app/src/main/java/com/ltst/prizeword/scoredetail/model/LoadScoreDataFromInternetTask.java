package com.ltst.prizeword.scoredetail.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestInviteFriend;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadScoreDataFromInternetTask implements IBcTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadUsersFromInternetTask.sessionKey";
    public static final @Nonnull String BF_FRIEND_VK_DATA = "LoadFriendsDataFromInternetTask.VkFriendData";
    public static final @Nonnull String BF_FRIEND_FB_DATA = "LoadFriendsDataFromInternetTask.FbFriendData";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    public static final @Nullable
    List<ISlowSource.Item<ScoreFriendsData, Bitmap>> extractFriendsList(@Nonnull Bundle bundle)
    {
        if (bundle == null)
            return null;

        List<ScoreFriendsData> VkFriendsItems = bundle.<ScoreFriendsData>getParcelableArrayList(BF_FRIEND_VK_DATA);
        List<ScoreFriendsData> FbFriendsItems = bundle.<ScoreFriendsData>getParcelableArrayList(BF_FRIEND_FB_DATA);

        if (VkFriendsItems == null && FbFriendsItems == null)
            return null;

        List<ISlowSource.Item<ScoreFriendsData, Bitmap>> resultItems = new ArrayList<ISlowSource.Item<ScoreFriendsData, Bitmap>>();

        if (VkFriendsItems != null)
        {
            for (ScoreFriendsData item : VkFriendsItems)
            {
                if (item != null)
                {
                    byte[] image = item.pngImage;
                    Bitmap bitmap = (image == null)
                            ? null
                            : BitmapFactory.decodeByteArray(image, 0, image.length);
                    resultItems.add(new ISlowSource.Item<ScoreFriendsData, Bitmap>(item, bitmap));
                }
            }
        }

        if (FbFriendsItems != null)
        {
            for (ScoreFriendsData item : FbFriendsItems)
            {
                if (item != null)
                {
                    byte[] image = item.pngImage;
                    Bitmap bitmap = (image == null)
                            ? null
                            : BitmapFactory.decodeByteArray(image, 0, image.length);
                    resultItems.add(new ISlowSource.Item<ScoreFriendsData, Bitmap>(item, bitmap));
                }
            }
        }

        return resultItems;
    }

    @Override public Bundle execute(BcTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }
        @Nonnull String sessionKey = extras.getString(BF_SESSION_KEY);

        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        } else
        {
            Bundle bundle = new Bundle();
            RestInviteFriend.RestInviteFriendHolder holder = loadScoreData(sessionKey, RestParams.VK_PROVIDER);
            if (holder != null)
            {
                ArrayList<ScoreFriendsData> friends = parseInvitedFriends(holder, RestParams.VK_PROVIDER);
                if (friends != null && friends.size()>0)
                {
                    bundle.putParcelableArrayList(BF_FRIEND_VK_DATA, friends);
                }
            }
            holder = loadScoreData(sessionKey, RestParams.FB_PROVIDER);
            if (holder != null)
            {
                ArrayList<ScoreFriendsData> friends = parseInvitedFriends(holder, RestParams.FB_PROVIDER);

                if (friends != null && friends.size()>0)
                {
                    bundle.putParcelableArrayList(BF_FRIEND_FB_DATA, friends);
                    return bundle;
                }
            }

            ArrayList<ScoreFriendsData> friends = new ArrayList<ScoreFriendsData>();
            ScoreFriendsData midData = new ScoreFriendsData("Павел", "Сон", Strings.EMPTY, 0, 0, new int[1],
                    Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, null, RestParams.FB_PROVIDER, RestParams.FRIEND_TYPE_DATA);
            friends.add(midData);
            midData = new ScoreFriendsData("Pavel", "Son", Strings.EMPTY, 0, 0, new int[1],
                    Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, null, RestParams.FB_PROVIDER, RestParams.FRIEND_TYPE_DATA);
            friends.add(midData);
            bundle.putParcelableArrayList(BF_FRIEND_FB_DATA, friends);
            return bundle;

        }


        return null;
    }

    private @Nullable
    RestInviteFriend.RestInviteFriendHolder loadScoreData(@Nonnull String sessionKey, @Nonnull String provider)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getFriendsScoreData(sessionKey, provider);

        } catch (Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    public static @Nullable
    ArrayList<ScoreFriendsData> parseInvitedFriends(@Nonnull RestInviteFriend.RestInviteFriendHolder holder, @Nonnull String providerName)
    {
        ArrayList<ScoreFriendsData> listFriends = new ArrayList<ScoreFriendsData>(holder.getFriends().size());
        for (RestInviteFriend restF : holder.getFriends())
        {
            ScoreFriendsData set = new ScoreFriendsData(restF.getFirstName(), restF.getLastName(),
                    restF.getDeactivated(), restF.getOnline(), restF.getUserId(), restF.getLists(),
                    restF.getId(), restF.getUserpic(), restF.getStatus(), null, providerName, RestParams.FRIEND_TYPE_DATA);
            listFriends.add(set);
        }
        return listFriends;
    }

}
