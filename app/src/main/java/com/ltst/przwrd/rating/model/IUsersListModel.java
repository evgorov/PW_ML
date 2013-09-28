package com.ltst.przwrd.rating.model;

import android.graphics.Bitmap;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import javax.annotation.Nonnull;

public interface IUsersListModel
{
    @Nonnull ISlowSource<UsersList.User, Bitmap> getSource();
    void updateDataByInternet(@Nonnull IListenerVoid handler);
}
