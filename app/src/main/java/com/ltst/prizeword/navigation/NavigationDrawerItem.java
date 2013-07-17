package com.ltst.prizeword.navigation;

import javax.annotation.Nonnull;

public class NavigationDrawerItem
{
    private @Nonnull String mTitle;
    private @Nonnull String mFragmentClassName;
    private boolean mIsHidden;

    public NavigationDrawerItem(@Nonnull String title, @Nonnull String fragmentClassName, boolean isHidden)
    {
        mTitle = title;
        mFragmentClassName = fragmentClassName;
        mIsHidden = isHidden;
    }

    @Nonnull
    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(@Nonnull String title)
    {
        mTitle = title;
    }

    @Nonnull
    public String getFragmentClassName()
    {
        return mFragmentClassName;
    }

    public void setFragmentClassName(@Nonnull String fragmentClassName)
    {
        mFragmentClassName = fragmentClassName;
    }

    public boolean isHidden()
    {
        return mIsHidden;
    }

    public void setHidden(boolean mIsHidden)
    {
        this.mIsHidden = mIsHidden;
    }

    @Override
    public boolean equals(Object o)
    {
        return this.equals((NavigationDrawerItem) o);
    }

    private boolean equals(NavigationDrawerItem item)
    {
        return this.mTitle.equals(item.mTitle);
    }
}
