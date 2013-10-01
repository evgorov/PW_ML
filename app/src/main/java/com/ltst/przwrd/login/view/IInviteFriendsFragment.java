package com.ltst.przwrd.login.view;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IInviteFriendsFragment {

    void getToken();
    void invite(@Nullable IListenerVoid handler);
    void setIdsFriends(@Nonnull String id);
}
