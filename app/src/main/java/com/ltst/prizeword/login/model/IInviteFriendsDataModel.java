package com.ltst.prizeword.login.model;

import android.graphics.Bitmap;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import javax.annotation.Nonnull;

public interface IInviteFriendsDataModel
{
    void loadFriendImageFromServer(@Nonnull final String url, @Nonnull IListenerVoid handler);
    void loadFriendDataFromInternet(@Nonnull IListenerVoid handler);
}
