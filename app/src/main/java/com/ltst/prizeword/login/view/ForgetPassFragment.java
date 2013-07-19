package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;


import javax.annotation.Nonnull;

/**
 * Created by Kostrof on 16.07.13.
 */

public class ForgetPassFragment extends SherlockFragment
        implements View.OnClickListener, INavigationBackPress
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ForgetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ForgetPassFragment.class.getName();

    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull Context mContext;
    private @Nonnull Button mSendEmailButton;
    private @Nonnull Button mBackPressButton;
    private @Nonnull EditText mEmailEditText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mSendEmailButton.setOnClickListener(this);
        mBackPressButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        View v = inflater.inflate(R.layout.forgetpass_fragment_layout, container, false);
        mEmailEditText = (EditText) v.findViewById(R.id.forgetpass_email_etext);
        mBackPressButton = (Button) v.findViewById(R.id.forgetpass_back_btn);
        mSendEmailButton = (Button) v.findViewById(R.id.forgetpass_send_btn);
        return v;
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId()){
            case R.id.forgetpass_back_btn:
                onBackKeyPress();
                break;
            case R.id.forgetpass_send_btn:
                String email = mEmailEditText.getText().toString();

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackKeyPress() {
        mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
    }
}
