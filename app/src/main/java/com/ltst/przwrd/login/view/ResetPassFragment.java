package com.ltst.przwrd.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.login.model.ForgetPassCycleTask;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationBackPress;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.sounds.SoundsWork;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResetPassFragment extends SherlockFragment implements INavigationBackPress
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ResetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ResetPassFragment.class.getName();

    private static @Nullable String mPasswordToken;

    private static @Nullable String mNewPassword;

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull Context mContext;

    private @Nonnull EditText mNewPasswordInput;
    private @Nonnull EditText mNewPasswordConfirmInput;
    private @Nonnull Button mResetPasswordButton;
    private @Nonnull Button mNavBackButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner)activity).getBcConnector();
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mDrawerHolder = (INavigationDrawerHolder)activity;
        mDrawerHolder.lockDrawerClosed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.resetpass_fragment_layout, container, false);
        mNewPasswordInput = (EditText) v.findViewById(R.id.login_change_pass_newpass_text);
        mNewPasswordConfirmInput = (EditText) v.findViewById(R.id.login_change_pass_confirm_text);
        mResetPasswordButton = (Button) v.findViewById(R.id.login_change_pass_btn);
        mNavBackButton = (Button) v.findViewById(R.id.resetpass_nav_back_btn);
        mNavBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackKeyPress();
            }
        });

        mResetPasswordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SoundsWork.interfaceBtnMusic(mContext);
                hideKeyboard();

                mNewPassword = mNewPasswordInput.getText().toString();
                final String pass = mNewPassword;
                String passConfirm = mNewPasswordConfirmInput.getText().toString();
                final String token = mPasswordToken;
                if(pass.equals(passConfirm) && !pass.equals(Strings.EMPTY))
                {
                    ForgetPassUpdater updater = new ForgetPassUpdater()
                    {
                        @Nonnull
                        @Override
                        protected Intent createIntent()
                        {
                            return ForgetPassCycleTask.createResetPasswordIntent(token, pass);
                        }
                    };

                    updater.update(new IListenerVoid()
                    {
                        @Override
                        public void handle()
                        {
                            mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
                        }
                    });
                }
            }
        });
        return v;
    }

    public void setPasswordToken(@Nonnull String token)
    {
        mPasswordToken = token;
    }


    private abstract class ForgetPassUpdater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return ForgetPassCycleTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                Toast.makeText(getActivity(), R.string.msg_unknown_error, Toast.LENGTH_LONG).show();
                return;
            }

            String msg = Strings.EMPTY;
            Resources res = getResources();
            int statusCode = result.getInt(ForgetPassCycleTask.BF_HTTP_STATUS);
            switch (statusCode)
            {
                case RestParams.SC_SUCCESS:
                    msg = res.getString(R.string.msg_password_changed);
                    break;
                case RestParams.SC_FORBIDDEN:
                    msg = res.getString(R.string.msg_password_empty);
                    break;
                case RestParams.SC_ERROR:
                    msg = res.getString(R.string.msg_password_token_not_found);
                    break;
                default:
                    msg = res.getString(R.string.msg_unknown_error);
                    break;
            }
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
        hideKeyboard();
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
