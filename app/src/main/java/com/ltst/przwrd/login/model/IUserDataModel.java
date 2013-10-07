package com.ltst.przwrd.login.model;

import org.omich.velo.handlers.IListenerVoid;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public interface IUserDataModel {

    void loadUserImageFromServer(@Nonnull final String url, @Nonnull IListenerVoid handler);
    void loadUserImageFromDB(long user_id, @Nonnull IListenerVoid handler);
    void loadUserDataFromInternet(@Nonnull IListenerVoid handler);
    void resetUserImage(final byte[] userPic, @Nonnull IListenerVoid handler);
    void resetUserName(final String userName, @Nonnull IListenerVoid handler);
    void loadProvidersFromDB(long user_id, @Nonnull IListenerVoid handler);
    void mergeAccounts(@Nonnull IListenerVoid handler);
    void clearDataBase(@Nonnull IListenerVoid handler);
    void setProvider(@Nonnull String mProvider);
    UserData getUserData();
    byte[] getUserPic();
    @Nullable ArrayList<UserProvider> getProviders();
    int getStatusCodeAnswer();
    @Nonnull String getStatusMessageAnswer();
    @Nonnull String getProvider();
    void close();
}
