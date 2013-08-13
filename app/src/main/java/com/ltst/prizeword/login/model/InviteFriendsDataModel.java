package com.ltst.prizeword.login.model;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.navigation.NavigationActivity;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InviteFriendsDataModel implements IInviteFriendsDataModel
{
    private @Nonnull Context mContext;
    private @Nonnull BcConnector mBcConnector;
    private @Nonnull InviteFriendsData mfriendsData;
    private @Nonnull byte[] mFriendPic;
    private int mStatusCodeAnswer;
    private @Nonnull String mStatusMessageAnswer;
    private @Nonnull String mProvider;

    public InviteFriendsDataModel(@Nonnull Context mContext, @Nonnull BcConnector mBcConnector)
    {
        this.mContext = mContext;
        this.mBcConnector = mBcConnector;
    }

    @Nonnull public InviteFriendsData getMfriendsData()
    {
        return mfriendsData;
    }

    @Nonnull public byte[] getmFriendPic()
    {
        return mFriendPic;
    }

    @Nonnull public String getmStatusMessageAnswer()
    {
        return mStatusMessageAnswer;
    }

    public int getmStatusCodeAnswer()
    {
        return mStatusCodeAnswer;
    }

    @Nonnull public String getmProvider()
    {
        return mProvider;
    }

    public void setmProvider(@Nonnull String mProvider)
    {
        this.mProvider = mProvider;
    }

    @Override public void loadFriendDataFromInternet(@Nonnull IListenerVoid handler)
    {
        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Log.d(NavigationActivity.LOG_TAG, "RELOAD USERDATA SessionKey = " + sessionKey);
        Updater session = new Updater() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return LoadFriendDataFromInternet.createIntent(sessionKey);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return LoadFriendDataFromInternet.class;
            }

            @Override
            protected void handleData(@Nullable Bundle result)
            {
                if (result == null){
                    mfriendsData = null;
                    return;
                }
                @Nonnull InviteFriendsData friendData = result.getParcelable(LoadFriendDataFromInternet.BF_FRIEND_DATA);
                mfriendsData = result.getParcelable(LoadFriendDataFromInternet.BF_FRIEND_DATA);
            }
        };
        session.update(handler);
    }
    @Override
    public void loadFriendImageFromServer(@Nonnull String url, @Nonnull IListenerVoid handler)
    {

    }

    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

    }
}
