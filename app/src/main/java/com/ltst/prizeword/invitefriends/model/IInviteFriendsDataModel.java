package com.ltst.prizeword.invitefriends.model;

import android.graphics.Bitmap;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.ISlowSource;

import javax.annotation.Nonnull;

public interface IInviteFriendsDataModel
{
    @Nonnull ISlowSource<InviteFriendsData,Bitmap> getSource();
    void updateDataByInternet(@Nonnull IListenerVoid handler);
    void sendInviteFriends(String ids, String providerName, @Nonnull IListenerVoid handler);
}
