package com.ltst.prizeword.login;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class RulesFragment extends SherlockFragment implements OnClickListener{

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.RulesFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RulesFragment.class.getName();

    RulesFragmentAdapter mAdapter;
    private ViewPager mPager;

    public static final String TAG = "detailsFragment";

    public View OnCreateVeiw(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.rules_fragment_layout, container, false);
        mAdapter = new RulesFragmentAdapter(getActivity().getSupportFragmentManager());

        mPager = (ViewPager)v.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

        });


        return v;
    }
    @Override public void onClick(View view)
    {

    }
}
