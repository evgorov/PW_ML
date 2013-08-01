package com.ltst.prizeword.login.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.dowloading.LoadImageTask;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public class UserDataModel implements IUserDataModel {

    private @Nonnull Context mContext;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull UserData mUserData;
    private @Nonnull byte[] mUserPic;

    public UserDataModel(@Nonnull Context context, @Nonnull IBcConnector bcConnector) {
        this.mContext = context;
        this.mBcConnector = bcConnector;
    }

    @Nonnull
    public UserData getUserData() {
        return mUserData;
    }

    @Nonnull
    public byte[] getUserPic() {
        return mUserPic;
    }

    @Override
    public void loadUserDataFromInternet(@Nonnull IListenerVoid handler) {

        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Updater session = new Updater() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return LoadUserDataFromInternetTask.createIntent(sessionKey);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return LoadUserDataFromInternetTask.class;
            }

            @Override
            protected void handleData(@Nullable Bundle result)
            {
                if (result == null){
                    mUserData = null;
                    return;
                }
                mUserData = result.getParcelable(LoadUserDataFromInternetTask.BF_USER_DATA);
            }
        };
        session.update(handler);
    }

    @Override
    public void loadUserPic(@Nonnull final String url, @Nonnull IListenerVoid handler) {

        Updater session = new Updater() {

            @Nonnull
            @Override
            protected Intent createIntent() {
                return LoadImageTask.createIntent(url);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return LoadImageTask.class;
            }

            @Override
            protected void handleData(@Nullable Bundle result)
            {
                if (result == null){
                    mUserPic = null;
                    return;
                }
                mUserPic = result.getByteArray(LoadImageTask.BF_BITMAP);
            }
        };
        session.update(handler);
    }

    @Override
    public void resetUserPic(final byte[] userPic, @Nonnull IListenerVoid handler) {

        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Updater session = new Updater() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return ResetUserDataOnServerTask.createUserPicIntent(sessionKey, userPic);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return ResetUserDataOnServerTask.class;
            }

            @Override
            protected void handleData(@Nullable Bundle result)
            {
                if (result == null){
                    mUserData = null;
                    return;
                }
                mUserData = result.getParcelable(ResetUserDataOnServerTask.BF_USER_DATA);
            }

        };
        session.update(handler);
    }

    @Override
    public void resetUserName(final String userName, @Nonnull IListenerVoid handler) {

        final String sessionKey = SharedPreferencesValues.getSessionKey(mContext);
        Updater session = new Updater() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return ResetUserDataOnServerTask.createUserName(sessionKey, userName);
            }

            @Nonnull
            @Override
            protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
                return ResetUserDataOnServerTask.class;
            }

            @Override
            protected void handleData(@Nullable Bundle result)
            {
                if (result == null){
                    mUserData = null;
                    return;
                }
                mUserData = result.getParcelable(ResetUserDataOnServerTask.BF_USER_DATA);
            }

        };
        session.update(handler);
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
