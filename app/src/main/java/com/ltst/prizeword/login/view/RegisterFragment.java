package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;

import javax.annotation.Nonnull;

public class RegisterFragment extends SherlockFragment implements INavigationBackPress
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();

    private @Nonnull Context mContext;

    private @Nonnull Button mNavBackButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.register_fragment_layout, container, false);
        mNavBackButton = (Button) v.findViewById(R.id.registration_nav_back_button);
        mNavBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackKeyPress();
            }
        });
        return v;
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }
}
