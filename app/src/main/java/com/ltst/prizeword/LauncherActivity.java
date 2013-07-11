package com.ltst.prizeword;

import android.os.Bundle;


import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;

import javax.annotation.Nonnull;

public class LauncherActivity extends SherlockActivity
{
    private @Nonnull ListView mDrawerList;
    private @Nonnull ListAdapter mDrawerAdapter;
    private @Nonnull String[] mDrawerItems = new String[]{"Crosswords", "Invite Friends"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
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
