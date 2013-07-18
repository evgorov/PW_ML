package com.ltst.prizeword.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.login.view.AuthorizationFragment;
import com.ltst.prizeword.crossword.view.CrosswordsFragment;
import com.ltst.prizeword.login.view.LoginFragment;
import com.ltst.prizeword.login.view.RegisterFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerInt;

import javax.annotation.Nonnull;

public class NavigationActivity extends SherlockFragmentActivity
        implements INavigationDrawerActivity<NavigationDrawerItem>,
        IFragmentsHolderActivity,
        IBcConnectorOwner
{

    public static final @Nonnull String LOG_TAG = "prizeword";

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull DrawerLayout mDrawerLayout;
    private @Nonnull ListView mDrawerList;
    private @Nonnull NavigationDrawerListAdapter mDrawerAdapter;
    private @Nonnull List<NavigationDrawerItem> mDrawerItems;

    private @Nonnull FragmentManager mFragmentManager;
    private @Nonnull SparseArrayCompat<Fragment> mFragments;

    private int mCurrentSelectedFragmentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mBcConnector = new BcConnector(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.nagivation_drawer_list);
        mDrawerAdapter = new NavigationDrawerListAdapter(this);
        mDrawerList.setAdapter(mDrawerAdapter);
        mFragmentManager = getSupportFragmentManager();
        mFragments = new SparseArrayCompat<Fragment>();

        selectNavigationFragmentByPosition(0);
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
                selectNavigationFragmentByPosition(i);
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
            initFragmentToList(LoginFragment.FRAGMENT_ID,  LoginFragment.FRAGMENT_CLASSNAME, false);
            initFragmentToList(CrosswordsFragment.FRAGMENT_ID, CrosswordsFragment.FRAGMENT_CLASSNAME, false);
            initFragmentToList(AuthorizationFragment.FRAGMENT_ID, AuthorizationFragment.FRAGMENT_CLASSNAME, true);
            initFragmentToList(RegisterFragment.FRAGMENT_ID, RegisterFragment.FRAGMENT_CLASSNAME, true);
        }
        return mDrawerItems;
    }

    // ==== IFragmentsHolderActivity =================================

    @Override
    public void selectNavigationFragmentByPosition(int position)
    {
        if(!isFragmentInitialized(position))
        {
            Fragment fr = Fragment.instantiate(this, mDrawerItems.get(position).getFragmentClassName());
            mFragments.append(position, fr);
        }

        mCurrentSelectedFragmentPosition = position;
        Fragment fr = mFragments.get(position);
        mFragmentManager.beginTransaction()
                        .replace(R.id.navigation_content_frame, fr)
                        .commit();

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mDrawerItems.get(position).getTitle());
    }

    @Override
    public void selectNavigationFragmentByClassname(@Nonnull String fragmentClassname)
    {
        int size = mDrawerItems.size();
        for (int i = 0; i < size; i++)
        {
            NavigationDrawerItem item = mDrawerItems.get(i);
            if(fragmentClassname.equals(item.getFragmentClassName()))
            {
                mCurrentSelectedFragmentPosition = i;
                selectNavigationFragmentByPosition(i);
                break;
            }
        }
    }

    // ==================================================

    private void initFragmentToList(@Nonnull String id, @Nonnull String classname, boolean hidden)
    {
        String title = Strings.EMPTY;
        Resources res = getResources();
        if(id.equals(LoginFragment.FRAGMENT_ID))
            title = res.getString(R.string.login_fragment_title);
        else if(id.equals(CrosswordsFragment.FRAGMENT_ID))
            title = res.getString(R.string.crosswords_fragment_title);
        else if(id.equals(AuthorizationFragment.FRAGMENT_ID))
            title = res.getString(R.string.authorization_fragment_title);
        else if(id.equals(RegisterFragment.FRAGMENT_ID))
            title = res.getString(R.string.registration_fragment_title);

        if(!title.equals(Strings.EMPTY))
        {
            NavigationDrawerItem item = new NavigationDrawerItem(title, classname, hidden);
            mDrawerItems.add(item);
        }
    }

    private boolean isFragmentInitialized(int position)
    {
        return mFragments.get(position) != null;
    }

    //==== IBcConnectorOwner ==============================================

    @Nonnull
    @Override
    public IBcConnector getBcConnector()
    {
        return mBcConnector;
    }

    // ==================== BACK_PRESS ==============================


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Fragment fr = mFragments.get(mCurrentSelectedFragmentPosition);
            if(fr instanceof INavigationBackPress){
                ((INavigationBackPress)fr).onBackKeyPress();
            }
            else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }
}
