package com.ltst.prizeword.navigation;

import javax.annotation.Nonnull;

public class NavigationDrawerItem
{
    private @Nonnull String mTitle;
    private @Nonnull String mFragmentClassName;

    public NavigationDrawerItem(@Nonnull String title, @Nonnull String fragmentClassName)
    {
        mTitle = title;
        mFragmentClassName = fragmentClassName;
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
