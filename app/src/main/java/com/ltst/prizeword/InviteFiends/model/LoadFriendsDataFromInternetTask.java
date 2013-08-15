package com.ltst.prizeword.InviteFiends.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestInviteFriend;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadFriendsDataFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadFriendsDataFromInternetTask.sessionKey";
    public static final @Nonnull String BF_FRIEND_VK_DATA = "LoadFriendsDataFromInternetTask.friendData";

    public static @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }


    public static  @Nullable List<ISlowSource.Item<InviteFriendsData,Bitmap>> extractFriendsFromBundle(@Nullable Bundle taskResult)
    {
        if(taskResult == null)
            return null;

        List<InviteFriendsData> VkFriendsItems = taskResult.<InviteFriendsData>getParcelableArrayList(BF_FRIEND_VK_DATA);

        if(VkFriendsItems == null)
            return null;

        List<ISlowSource.Item<InviteFriendsData,Bitmap>> resultItems = new ArrayList<ISlowSource.Item<InviteFriendsData, Bitmap>>();

        for(InviteFriendsData item : VkFriendsItems)
        {
            if(item != null)
            {
                byte[] image = item.pngImage;
                Bitmap bitmap = (image == null)
                        ? null
                        : BitmapFactory.decodeByteArray(image, 0, image.length);
                resultItems.add(new ISlowSource.Item<InviteFriendsData, Bitmap>(item, bitmap));
            }
        }

        return resultItems;
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
        @Nonnull String friendVkData = extras.getString(BF_FRIEND_VK_DATA);

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            RestInviteFriend.RestInviteFriendHolder holder = loadRestFriendDataFromInternet(sessionKey,RestParams.VK_PROVIDER);
            if (holder != null)
            {
                ArrayList<InviteFriendsData> friends = parseFriendData(holder);
                if (friends != null)
                {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(BF_FRIEND_VK_DATA, friends);
                    return bundle;
                }
            }
        }
        return null;
    }




    private @Nullable
    RestInviteFriend.RestInviteFriendHolder loadRestFriendDataFromInternet(@Nonnull String sessionKey, @Nonnull String provider)
    {
        try
        {
            IRestClient client = RestClient.create();
            return client.getFriendsData(sessionKey, provider);
        } catch (Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }


    public static @Nullable
    ArrayList<InviteFriendsData> parseFriendData(@Nonnull RestInviteFriend.RestInviteFriendHolder holder)
    {
        ArrayList<InviteFriendsData> listFriends = new ArrayList<InviteFriendsData>(holder.getFriends().size());
        for (RestInviteFriend restF : holder.getFriends())
        {
            InviteFriendsData set = new InviteFriendsData(restF.getFirstName(),restF.getLastName(), restF.getDeactivated(),restF.getOnline(),restF.getUserId(),restF.getLists(),restF.getId(),restF.getUserpic(),restF.getStatus(),null);
            listFriends.add(set);
        }
        return listFriends;
    }

}
