package com.ltst.przwrd.invitefriends.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestInviteFriend;
import com.ltst.przwrd.rest.RestParams;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.constants.Strings;
import org.omich.velo.lists.ISlowSource;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadFriendsDataFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadFriendsDataFromInternetTask.sessionKey";
    public static final @Nonnull String BF_FRIEND_VK_DATA = "LoadFriendsDataFromInternetTask.VkFriendData";
    public static final @Nonnull String BF_FRIEND_FB_DATA = "LoadFriendsDataFromInternetTask.FbFriendData";

    public static @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }


    public static @Nullable
    List<ISlowSource.Item<InviteFriendsData, Bitmap>> extractFriendsFromBundle(@Nullable Bundle taskResult)
    {
        List<ISlowSource.Item<InviteFriendsData, Bitmap>> resultItems = new ArrayList<ISlowSource.Item<InviteFriendsData, Bitmap>>();
        if (taskResult != null)
        {
            List<InviteFriendsData> VkFriendsItems = taskResult.<InviteFriendsData>getParcelableArrayList(BF_FRIEND_VK_DATA);
            List<InviteFriendsData> FbFriendsItems = taskResult.<InviteFriendsData>getParcelableArrayList(BF_FRIEND_FB_DATA);

            if (VkFriendsItems != null || FbFriendsItems != null)
            {
                if (VkFriendsItems != null)
                {
                    for (InviteFriendsData item : VkFriendsItems)
                    {
                        if (item != null)
                        {
                            byte[] image = item.pngImage;
                            Bitmap bitmap = (image == null)
                                    ? null
                                    : BitmapFactory.decodeByteArray(image, 0, image.length);
                            resultItems.add(new ISlowSource.Item<InviteFriendsData, Bitmap>(item, bitmap));
                        }
                    }
                }

                if(FbFriendsItems != null){
                    if(!FbFriendsItems.isEmpty())
                    {
                        InviteFriendsData midData = new InviteFriendsData(Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, 0, 0, new int[1],
                                Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, null, InviteFriendsData.NO_PROVIDER);
                        resultItems.add(new ISlowSource.Item<InviteFriendsData, Bitmap>(midData, null));
                    }

                    for (InviteFriendsData item : FbFriendsItems)
                    {
                        if (item != null)
                        {
                            byte[] image = item.pngImage;
                            Bitmap bitmap = (image == null)
                                    ? null
                                    : BitmapFactory.decodeByteArray(image, 0, image.length);
                            resultItems.add(new ISlowSource.Item<InviteFriendsData, Bitmap>(item, bitmap));
                        }
                    }
                }
            }
        }

        if(resultItems.size() == 0)
        {
            InviteFriendsData midData = new InviteFriendsData(Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, 0, 0, new int[1],
                    Strings.EMPTY, Strings.EMPTY, Strings.EMPTY, null, InviteFriendsData.NO_PROVIDER);
            resultItems.add(new ISlowSource.Item<InviteFriendsData, Bitmap>(midData, null));
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
        @Nonnull String VkFriendData = extras.getString(BF_FRIEND_VK_DATA);

        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        } else
        {
            Bundle bundle = new Bundle();
            RestInviteFriend.RestInviteFriendHolder holder = loadRestFriendDataFromInternet(env.context, sessionKey, RestParams.VK_PROVIDER);
            if (holder != null)
            {
                ArrayList<InviteFriendsData> friends = parseFriendData(holder, RestParams.VK_PROVIDER);
                if (friends != null)
                {
                    bundle.putParcelableArrayList(BF_FRIEND_VK_DATA, friends);
                }
            }
            holder = loadRestFriendDataFromInternet(env.context, sessionKey, RestParams.FB_PROVIDER);
            if (holder != null)
            {
                ArrayList<InviteFriendsData> friends = parseFriendData(holder, RestParams.FB_PROVIDER);
                if (friends != null)
                {
                    bundle.putParcelableArrayList(BF_FRIEND_FB_DATA, friends);
                    return bundle;
                }
            }
            else
            return  bundle;
        }
        return null;
    }


    private @Nullable
    RestInviteFriend.RestInviteFriendHolder loadRestFriendDataFromInternet(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull String provider)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getFriendsData(sessionKey, provider);
        } catch (Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }


    public static @Nullable
    ArrayList<InviteFriendsData> parseFriendData(@Nonnull RestInviteFriend.RestInviteFriendHolder holder, @Nonnull String providerName)
    {
        ArrayList<InviteFriendsData> listFriends = new ArrayList<InviteFriendsData>(holder.getFriends().size());
        for (RestInviteFriend restF : holder.getFriends())
        {
            InviteFriendsData set = new InviteFriendsData(restF.getFirstName(), restF.getLastName(), restF.getDeactivated(), restF.getOnline(), restF.getUserId(), restF.getLists(), restF.getId(), restF.getUserpic(), restF.getStatus(), null, providerName);
            listFriends.add(set);
        }
        return listFriends;
    }

}
