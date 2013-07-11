package com.ltst.prizeword.navigation;

import org.omich.velo.handlers.IListenerInt;
import java.util.List;
import android.content.Context;

import javax.annotation.Nonnull;

/**
 * Created by naghtarr on 7/11/13.
 */
public interface INavigationDrawerActivity<T>
{
    public @Nonnull Context getContext();
    public int getDrawerItemResourceId();
    public int getDrawerItemTextViewResourceId();
    public @Nonnull IListenerInt getDrawerItemClickHandler();
    public @Nonnull List<T> getNavigationDrawerItems();
    public void selectNavigationFragment(int position);
}
