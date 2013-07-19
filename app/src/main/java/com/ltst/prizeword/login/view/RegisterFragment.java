package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class RegisterFragment extends SherlockFragment implements View.OnClickListener
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.view.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();

    private @Nonnull Context mContext;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context) activity;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public View OnCreateVeiw(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.register_fragment_layout, container, false);
        return v;
    }

    @Override public void onClick(View view)
    {

    }
}
