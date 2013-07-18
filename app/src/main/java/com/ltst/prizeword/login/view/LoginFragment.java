package com.ltst.prizeword.login.view;

import com.ltst.prizeword.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.authorization.AuthorizationFragment;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.rest.RestParams;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class LoginFragment extends SherlockFragment implements OnClickListener
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.LoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = LoginFragment.class.getName();

    private @Nonnull Context mContext;
    private @Nonnull Button mAuthorizationVkButton;
    private @Nonnull Button mAuthorizationFbButton;
    private @Nonnull Button mAuthorizationButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;

    @Override
    public void onAttach(Activity activity) {
        mContext = (Context) activity;
        super.onAttach(activity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.login_fragment_layout, container, false);
        mAuthorizationButton = (Button) v.findViewById(R.id.enter);
        mAuthorizationVkButton = (Button) v.findViewById(R.id.button1);
        mAuthorizationFbButton = (Button) v.findViewById(R.id.button2);
        return v;
    }

    public void onActivityCreated(Bundle saveInstanceState)
    {
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mAuthorizationButton.setOnClickListener(this);
        mAuthorizationVkButton.setOnClickListener(this);
        mAuthorizationFbButton.setOnClickListener(this);
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onClick(View v)
    {
        @Nonnull Intent intent;
        switch (v.getId())
        {
            case R.id.enter:
                mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.button1:
                intent = new Intent(mContext, SocialLoginActivity.class);
                intent.putExtra(SocialLoginActivity.PROVEDER_ID, RestParams.FB_PROVIDER);
//        startActivityForResult(intent, REQUEST_LOGIN);
                startActivity(intent);
                break;
            case R.id.button2:
                intent = new Intent(mContext, SocialLoginActivity.class);
                intent.putExtra(SocialLoginActivity.PROVEDER_ID, RestParams.VK_PROVIDER);
//        startActivityForResult(intent, REQUEST_LOGIN);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

}
