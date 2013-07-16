package com.ltst.prizeword.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;


import javax.annotation.Nonnull;

/**
 * Created by Kostrof on 16.07.13.
 */
public class ForgetPassFragment extends SherlockFragment implements View.OnClickListener
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.LoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = LoginFragment.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.login_fragment_layout, container, false);
        return v;
    }


    @Override
    public void onClick(View view)
    {

    }
}
