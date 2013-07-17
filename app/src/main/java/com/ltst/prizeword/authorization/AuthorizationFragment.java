package com.ltst.prizeword.authorization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class AuthorizationFragment extends SherlockFragment
{
    public static final
    @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.authorization.AuthorizationFragment";
    public static final
    @Nonnull
    String FRAGMENT_CLASSNAME = AuthorizationFragment.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.authorization_fragment_layout, container, false);
        return v;
    }
}
