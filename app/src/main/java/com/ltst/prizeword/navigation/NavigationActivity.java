package com.ltst.prizeword.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.view.CrosswordsFragment;
import com.ltst.prizeword.login.LoginFragment;
import com.ltst.prizeword.vk.VkLoginFragment;

import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerInt;

import javax.annotation.Nonnull;

public class NavigationActivity extends SherlockFragmentActivity implements INavigationDrawerActivity<NavigationDrawerItem>
{
    private @Nonnull DrawerLayout mDrawerLayout;
    private @Nonnull ListView mDrawerList;
    private @Nonnull NavigationDrawerListAdapter mDrawerAdapter;
    private @Nonnull List<NavigationDrawerItem> mDrawerItems;

    private @Nonnull FragmentManager mFragmentManager;
    private @Nonnull SparseArrayCompat<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.nagivation_drawer_list);
        mDrawerAdapter = new NavigationDrawerListAdapter(this);
        mDrawerList.setAdapter(mDrawerAdapter);
        mFragmentManager = getSupportFragmentManager();
        mFragments = new SparseArrayCompat<Fragment>();

        selectNavigationFragment(0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    // ==== INavigationDrawerActivity =================================

    @Nonnull
    @Override
    public Context getContext()
    {
        return this;
    }

    @Override
    public int getDrawerItemResourceId()
    {
        return R.layout.navigation_drawer_item;
    }

    @Override
    public int getDrawerItemTextViewResourceId()
    {
        return R.id.navigation_drawer_textview;
    }

    @Nonnull
    @Override
    public IListenerInt getDrawerItemClickHandler()
    {
        return new IListenerInt()
        {
            @Override
            public void handle(int i)
            {
                selectNavigationFragment(i);
            }
        };
    }

    @Nonnull
    @Override
    public List<NavigationDrawerItem>  getNavigationDrawerItems()
    {
        if(mDrawerItems == null)
        {
            mDrawerItems = new ArrayList<NavigationDrawerItem>();
            initFragmentToList(LoginFragment.FRAGMENT_ID,  LoginFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(CrosswordsFragment.FRAGMENT_ID,  CrosswordsFragment.FRAGMENT_CLASSNAME);
            initFragmentToList(VkLoginFragment.FRAGMENT_ID,  VkLoginFragment.FRAGMENT_CLASSNAME);
        }
        return mDrawerItems;
    }

    @Override
    public void selectNavigationFragment(int position)
    {
        if(!isFragmentInitialized(position))
        {
            Fragment fr = Fragment.instantiate(this, mDrawerItems.get(position).getFragmentClassName());
            mFragments.append(position, fr);
        }

        Fragment fr = mFragments.get(position);
        mFragmentManager.beginTransaction()
                        .replace(R.id.navigation_content_frame, fr)
                        .commit();

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mDrawerItems.get(position).getTitle());
    }

    // ==================================================

    private void initFragmentToList(@Nonnull String id, @Nonnull String classname)
    {
        String title = Strings.EMPTY;
        Resources res = getResources();
        if(id.equals(LoginFragment.FRAGMENT_ID))
            title = res.getString(R.string.login_fragment_title);
        else if(id.equals(CrosswordsFragment.FRAGMENT_ID))
            title = res.getString(R.string.crosswords_fragment_title);
        else if(id.equals(VkLoginFragment.FRAGMENT_ID))
            title = res.getString(R.string.vk_login_fragment_title);

        if(!title.equals(Strings.EMPTY))
        {
            NavigationDrawerItem item = new NavigationDrawerItem(title, classname);
            mDrawerItems.add(item);
        }
    }

    private boolean isFragmentInitialized(int position)
    {
        return mFragments.get(position) != null;
    }

}
