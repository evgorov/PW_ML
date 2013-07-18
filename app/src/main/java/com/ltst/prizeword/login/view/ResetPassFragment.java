package com.ltst.prizeword.login.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class ResetPassFragment extends SherlockFragment
{
    public static final @Nonnull String BF_PASSED_URL = "ResetPassFragment.url";

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ResetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ResetPassFragment.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.resetpass_fragment_layout, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
}
