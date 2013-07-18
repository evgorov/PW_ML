package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;

import javax.annotation.Nonnull;

public class AuthorizationFragment extends SherlockFragment
        implements View.OnClickListener, INavigationBackPress
{
    private @Nonnull String LOG_TAG = "autorization";

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.view.AuthorizationFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = AuthorizationFragment.class.getName();

    private @Nonnull Context mContext;
    private @Nonnull Button mBackButton;
    private @Nonnull Button mEnterLoginButton;
    private @Nonnull ImageButton mForgetLoginButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context) activity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.authorization_fragment_layout, container, false);
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mBackButton = (Button) v.findViewById(R.id.login_back_button);
        mEnterLoginButton = (Button) v.findViewById(R.id.login_enter_enter_btn);
        mForgetLoginButton = (ImageButton) v.findViewById(R.id.login_forget_btn);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mBackButton.setOnClickListener(this);
        mEnterLoginButton.setOnClickListener(this);
        mForgetLoginButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_enter_enter_btn:
                break;
            case R.id.login_forget_btn:
                break;
            case R.id.login_back_button:
                mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackKeyPress() {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }
}
