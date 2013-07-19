package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.view.CrosswordsFragment;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.login.model.LoadSessionKeyTask;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.tools.ErrorAlertDialog;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RegisterFragment extends SherlockFragment implements INavigationBackPress, View.OnClickListener
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();

    private @Nonnull Context mContext;
    private IBcConnector mBcConnector;

    private @Nonnull Button mNavBackButton;
    private @Nonnull Button mRegisterFinishButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;

    private @Nonnull EditText mNameInput;
    private @Nonnull EditText mSurnameInput;
    private @Nonnull EditText mEmailInput;
    private @Nonnull EditText mPasswordInput;
    private @Nonnull EditText mPasswordConfirmInput;
    private @Nonnull EditText mCityInput;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) activity;
        mBcConnector = ((IBcConnectorOwner) activity).getBcConnector();
        mDrawerHolder = (INavigationDrawerHolder)activity;
        mDrawerHolder.lockDrawerClosed();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.register_fragment_layout, container, false);
        mNavBackButton = (Button) v.findViewById(R.id.registration_nav_back_button);
        mRegisterFinishButton = (Button) v.findViewById(R.id.registration_finish_button);
        mNavBackButton.setOnClickListener(this);
        mRegisterFinishButton.setOnClickListener(this);

        mNameInput = (EditText) v.findViewById(R.id.register_name_input);
        mSurnameInput = (EditText) v.findViewById(R.id.register_surname_input);
        mEmailInput = (EditText) v.findViewById(R.id.register_email_input);
        mPasswordInput = (EditText) v.findViewById(R.id.register_password_input);
        mPasswordConfirmInput = (EditText) v.findViewById(R.id.register_password_confirm_input);
        mCityInput = (EditText) v.findViewById(R.id.register_city_input);

        return v;
    }

    private void performRegistration()
    {
        @Nonnull final String name = mNameInput.getText().toString();
        @Nonnull final String surname = mSurnameInput.getText().toString();
        @Nonnull final String email = mEmailInput.getText().toString();
        @Nonnull final String password = mPasswordInput.getText().toString();
        @Nonnull final String passwordConfirm = mPasswordConfirmInput.getText().toString();

        @Nonnull final String city = mCityInput.getText().toString();

        if(!name.equals(Strings.EMPTY) && !surname.equals(Strings.EMPTY) && !email.equals(Strings.EMPTY)
                && password.equals(passwordConfirm) && !password.equals(Strings.EMPTY))
        {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            SignUpExecutor updater = new SignUpExecutor()
            {
                @Nonnull
                @Override
                protected Intent createIntent()
                {
                    return LoadSessionKeyTask.createSignUpIntent(email, name, surname, password, null, null, null);
                }
            };

            updater.update(new IListenerVoid()
            {
                @Override
                public void handle()
                {

                }
            });
        }

    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.registration_nav_back_button:
                onBackKeyPress();
                break;
            case R.id.registration_finish_button:
                performRegistration();
                break;
        }
    }

    private abstract class SignUpExecutor extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            int statusCode = result.getInt(LoadSessionKeyTask.BF_STATUS_CODE);

            switch (statusCode)
            {
                case RestParams.SC_SUCCESS:
                    @Nullable String sessionKey = result.getString(LoadSessionKeyTask.BF_SESSION_KEY);
                    if (result != null)
                    {
                        SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(mContext);
                        spref.putString(SharedPreferencesValues.SP_SESSION_KEY, sessionKey);
                        spref.commit();
                    }
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    break;
                case RestParams.SC_FORBIDDEN:
                {
                    int msg_id = R.string.msg_register_email_exists;
                    ErrorAlertDialog.showDialog(mContext, msg_id);
                    break;
                }
                default:
                {
                    int msg_id = R.string.msg_unknown_error;
                    ErrorAlertDialog.showDialog(mContext, msg_id);
                    break;
                }
            }
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadSessionKeyTask.class;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }
    }
}
