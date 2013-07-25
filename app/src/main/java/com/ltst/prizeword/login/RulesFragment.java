package com.ltst.prizeword.login;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class RulesFragment extends FragmentActivity implements OnClickListener{

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.RulesFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RulesFragment.class.getName();

    RulesFragmentAdapter mAdapter;
    private ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.rules_fragment_layout, container, false);
        /*mAdapter = new RulesFragmentAdapter();

        mPager = (ViewPager)v.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

        });*/

        List<View> pages = new ArrayList<View>();

        LayoutInflater inflater1 = LayoutInflater.from(getActivity());
        View page = inflater1.inflate(R.layout.page, null);
        TextView textView = (TextView) page.findViewById(R.id.text_view);
        textView.setText("Страница 1");
        pages.add(page);

        page = inflater1.inflate(R.layout.page, null);
        textView = (TextView) page.findViewById(R.id.text_view);
        textView.setText("Страница 2");
        pages.add(page);

        page = inflater1.inflate(R.layout.page, null);
        textView = (TextView) page.findViewById(R.id.text_view);
        textView.setText("Страница 3");
        pages.add(page);

        RulesFragmentAdapter pagerAdapter = new RulesFragmentAdapter(pages);
        ViewPager viewPager = (ViewPager)v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        return v;
    }
    @Override public void onClick(View view)
    {

    }
}
