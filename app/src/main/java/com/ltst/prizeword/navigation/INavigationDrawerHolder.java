package com.ltst.prizeword.navigation;

public interface INavigationDrawerHolder
{
    void lockDrawerClosed();
    void lockDrawerOpened();
    boolean isLockDrawerOpen();
    void unlockDrawer();
}
