package com.ltst.prizeword.login.model;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 26.07.13.
 */
public interface IUserDataModel {

    void loadUserPic(@Nonnull final String url, @Nonnull IListenerVoid handler);
    void loadUserData(@Nonnull IListenerVoid handler);
    void resetUserPic(final byte[] userPic, @Nonnull IListenerVoid handler);
    void resetUserName(final String userName, @Nonnull IListenerVoid handler);
}
