package com.ltst.prizeword.authorization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by Kostrof on 15.07.13.
 */
public class AuthoFragment extends SherlockFragment{
    public static final
    @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.authorization.AuthoFragment";
    public static final
    @Nonnull
    String FRAGMENT_CLASSNAME = AuthoFragment.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.autho_fragment_layout, container, false);
        return v;
    }


}
