package com.ltst.prizeword.login.model;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

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
}
