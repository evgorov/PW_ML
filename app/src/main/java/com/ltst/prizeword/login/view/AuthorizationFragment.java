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
import android.widget.ImageButton;

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

public class AuthorizationFragment extends SherlockFragment
        implements View.OnClickListener, INavigationBackPress
{
    private @Nonnull String LOG_TAG = "autorization";

    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.AuthorizationFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = AuthorizationFragment.class.getName();

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull Context mContext;
    private @Nonnull EditText mEmailEditText;
    private @Nonnull EditText mPasswdlEditText;
    private @Nonnull Button mBackPressButton;
    private @Nonnull Button mEnterLoginButton;
    private @Nonnull ImageButton mForgetLoginButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mDrawerHolder = (INavigationDrawerHolder)activity;
        mDrawerHolder.lockDrawerClosed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.authorization_fragment_layout, container, false);
        mEmailEditText = (EditText) v.findViewById(R.id.login_email_etext);
        mPasswdlEditText = (EditText) v.findViewById(R.id.login_passwd_etext);
        mBackPressButton = (Button) v.findViewById(R.id.login_back_button);
        mEnterLoginButton = (Button) v.findViewById(R.id.login_enter_enter_btn);
        mForgetLoginButton = (ImageButton) v.findViewById(R.id.login_forget_btn);
        mBackPressButton.setOnClickListener(this);
        mEnterLoginButton.setOnClickListener(this);
        mForgetLoginButton.setOnClickListener(this);

        mEmailEditText.setText("vlad@ltst.ru");
        mPasswdlEditText.setText("vlad");

        return v;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.login_enter_enter_btn:
                hideKeyboard();
                String email = mEmailEditText.getText().toString();
                String passwordf = mPasswdlEditText.getText().toString();
                enterLogin(email, passwordf);
                mEmailEditText.setText(Strings.EMPTY);
                mPasswdlEditText.setText(Strings.EMPTY);
                break;
            case R.id.login_forget_btn:
                hideKeyboard();
                mFragmentHolder.selectNavigationFragmentByClassname(ForgetPassFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.login_back_button:
                hideKeyboard();
                onBackKeyPress();
                break;
            default:
                break;
        }
    }
    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    protected void enterLogin(@Nonnull String email, @Nonnull String password)
    {

        final @Nonnull String emailf = email;
        final @Nonnull String passwordf = password;
        SessionEnterLogin loader = new SessionEnterLogin()
        {
            @Nonnull
            @Override
            protected Intent createIntent()
            {
                return LoadSessionKeyTask.createSignInIntent(emailf, passwordf);
            }
        };

        loader.update(new IListenerVoid()
        {
            @Override
            public void handle()
            {
                Log.i(LOG_TAG, "handling");
                SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(mContext);
                String sessionKey = spref.getString(SharedPreferencesValues.SP_SESSION_KEY, Strings.EMPTY);
                Log.i(LOG_TAG, "SESSIONKEY = " + sessionKey);
                if (sessionKey.equals(Strings.EMPTY))
                {
                    ErrorAlertDialog.showDialog(mContext, R.string.login_enter_error_msg);
                }
                else
                {
                    // скрываем клавиатуру;
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    // Переключемся на фрагмент сканвордов;
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    // Информируем наследников интерфейса IAutorization, что авторизация прошла успешно;
                    ((IAutorization) mContext).onAutotized();
                }
            }
        });

    }

    private abstract class SessionEnterLogin extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
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

        @Override
        protected void handleData(@Nullable Bundle result)
        {

            if (result == null)
                return;

            int statusCode = result.getInt(LoadSessionKeyTask.BF_STATUS_CODE);
            if (statusCode != RestParams.SC_UNAUTHORIZED)
            {
                @Nullable String sessionKey = result.getString(LoadSessionKeyTask.BF_SESSION_KEY);
                if (result != null)
                {
                    SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(mContext);
                    spref.putString(SharedPreferencesValues.SP_SESSION_KEY, sessionKey);
                    spref.commit();
                    Log.i(LOG_TAG, "SESSION KEY = " + sessionKey);
                }
            }
        }

    }
}