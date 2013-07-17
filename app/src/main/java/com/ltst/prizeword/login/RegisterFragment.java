package com.ltst.prizeword.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class RegisterFragment extends SherlockFragment implements View.OnClickListener
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();
    public View OnCreateVeiw(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.register_fragment_layout, container, false);
        return v;
    }
    @Override public void onClick(View view)
    {

    }
}
