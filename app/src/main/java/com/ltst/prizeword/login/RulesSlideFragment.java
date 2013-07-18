package com.ltst.prizeword.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

import javax.annotation.Nonnull;

import static android.support.v4.view.ViewPager.*;


public class RulesSlideFragment extends Fragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.RulesSlideFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RulesSlideFragment.class.getName();
    int imageResourceId;

    public RulesSlideFragment(int i)
    {
        imageResourceId = i;
    }

    @Override public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ImageView image = new ImageView(getActivity());
        image.setImageResource(imageResourceId);
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new ViewPager.LayoutParams());
        layout.setGravity(Gravity.CENTER);
        layout.addView(image);
        return layout;
    }
}
