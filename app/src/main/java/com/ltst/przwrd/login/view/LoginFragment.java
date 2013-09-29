package com.ltst.przwrd.login.view;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ltst.przwrd.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.crossword.view.CrosswordsFragment;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.sounds.SoundsWork;

import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class LoginFragment extends SherlockFragment implements OnClickListener
{
    public static final int REQUEST_LOGIN_VK = 1;
    public static final int REQUEST_LOGIN_FB = 2;
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.LoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = LoginFragment.class.getName();

    private @Nonnull Context mContext;
    private @Nonnull Button mAuthorizationVkButton;
    private @Nonnull Button mAuthorizationFbButton;
    private @Nonnull Button mAuthorizationButton;
    private @Nonnull Button mRegistrationButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull IAutorization mAuthorization;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;
    private @Nonnull IBcConnector mBcConnector;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mAuthorization = (IAutorization) activity;
        mDrawerHolder = (INavigationDrawerHolder) activity;
        mBcConnector = ((IBcConnectorOwner) activity).getBcConnector();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.login_fragment_layout, container, false);

        mAuthorizationVkButton = (Button) v.findViewById(R.id.enter_with_vk_btn);
        mAuthorizationFbButton = (Button) v.findViewById(R.id.enter_with_fb_btn);
        mAuthorizationButton = (Button) v.findViewById(R.id.enter_to_authorization_btn);
        mRegistrationButton = (Button) v.findViewById(R.id.enter_to_registration_btn);

        mAuthorizationButton.setOnClickListener(this);
        mAuthorizationVkButton.setOnClickListener(this);
        mAuthorizationFbButton.setOnClickListener(this);
        mRegistrationButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume()
    {
        mDrawerHolder.lockDrawerClosed();
        super.onResume();
    }

    @Override
    public void onClick(View v)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        @Nonnull Intent intent;
        switch (v.getId())
        {
            case R.id.enter_with_fb_btn:
                //??? facebook
                intent = new Intent(mContext, SocialLoginActivity.class);
                intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.FB_PROVIDER);
                startActivityForResult(intent, REQUEST_LOGIN_FB);
                break;
            case R.id.enter_with_vk_btn:
                //??? ?????????
                intent = new Intent(mContext, SocialLoginActivity.class);
                intent.putExtra(SocialLoginActivity.BF_PROVEDER_ID, RestParams.VK_PROVIDER);
                startActivityForResult(intent, REQUEST_LOGIN_VK);
                break;
            case R.id.enter_to_authorization_btn:
                mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.enter_to_registration_btn:
                mFragmentHolder.selectNavigationFragmentByClassname(RegisterFragment.FRAGMENT_CLASSNAME);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SherlockFragmentActivity.RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_LOGIN_FB:
                {
                    //успешно авторизовались в facebook
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    mAuthorization.onAuthotized();
                }
                break;
                case REQUEST_LOGIN_VK:
                {
                    //успешно авторизовались в vkontakte
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    mAuthorization.onAuthotized();
                }
                break;
                default:
                    break;
            }
        }
    }
}