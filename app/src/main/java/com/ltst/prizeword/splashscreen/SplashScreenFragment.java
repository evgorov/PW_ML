package com.ltst.prizeword.splashscreen;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 09.09.13.
 */
public class SplashScreenFragment extends SherlockFragment {

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.splashscreen.SplashScreenFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = SplashScreenFragment.class.getName();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.splashscreen_fragment_layout, container, false);
        return v;
    }
}
