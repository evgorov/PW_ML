package com.ltst.prizeword.login.view;

import com.ltst.prizeword.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.authorization.AuthorizationFragment;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;

import javax.annotation.Nonnull;

import static android.view.View.OnClickListener;

public class LoginFragment extends SherlockFragment implements OnClickListener
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.LoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = LoginFragment.class.getName();

    private @Nonnull Button mAuthorizationButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.login_fragment_layout, container, false);
        mAuthorizationButton = (Button) v.findViewById(R.id.enter);
        return v;
    }

    public void onActivityCreated(Bundle saveInstanceState)
    {
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mAuthorizationButton.setOnClickListener(this);
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.enter:
                mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
                break;
            default:
                break;
        }
    }
}
