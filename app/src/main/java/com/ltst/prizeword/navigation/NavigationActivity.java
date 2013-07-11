package com.ltst.prizeword.navigation;

import android.os.Bundle;


import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class NavigationActivity extends SherlockActivity
{
    private @Nonnull ListView mDrawerList;
    private @Nonnull ListAdapter mDrawerAdapter;
    private @Nonnull String[] mDrawerItems = new String[]{"Crosswords", "Invite Friends"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDrawerItems);
        mDrawerList.setAdapter(mDrawerAdapter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
