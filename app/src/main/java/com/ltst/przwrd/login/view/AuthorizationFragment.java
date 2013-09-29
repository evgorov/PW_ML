package com.ltst.przwrd.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.view.CrosswordsFragment;
import com.ltst.przwrd.login.model.LoadSessionKeyTask;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationBackPress;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.rest.RestParams;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.tools.ErrorAlertDialog;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthorizationFragment extends SherlockFragment
        implements View.OnClickListener, View.OnKeyListener, INavigationBackPress
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
    private @Nonnull IAutorization mAuthorization;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        mAuthorization = (IAutorization) activity;
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
//        mEmailEditText.setOnKeyListener(this);
//        mPasswdlEditText.setOnKeyListener(this);

        mEmailEditText.setText("vlad@ltst.ru");
        mPasswdlEditText.setText("vlad");

        return v;
    }

    private  void authorizing(){
        hideKeyboard();
        final @Nonnull String email = mEmailEditText.getText().toString();
        final @Nonnull String password = mPasswdlEditText.getText().toString();

        SessionEnterLogin loader = new SessionEnterLogin()
        {
            @Nonnull
            @Override
            protected Intent createIntent()
            {
                return LoadSessionKeyTask.createSignInIntent(email, password);
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
                    hideKeyboard();
                    // Переключемся на фрагмент сканвордов;
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    // Информируем наследников интерфейса IAutorization, что авторизация прошла успешно;
                    mAuthorization.onAuthotized();
                }
            }
        });

//                mEmailEditText.setText(Strings.EMPTY);
//                mPasswdlEditText.setText(Strings.EMPTY);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        switch (view.getId()){
            case R.id.login_email_etext:{
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    if(mPasswdlEditText.requestFocus()) {
                        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        mEmailEditText.clearFocus();
                    }
                }
            }
            break;
            case R.id.login_passwd_etext:{
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    authorizing();
                }
            }
            break;
            default:
                return view.onKeyUp(keyCode, keyEvent);
        }
        return true;
    }

    @Override
    public void onClick(View view)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        switch (view.getId())
        {
            case R.id.login_enter_enter_btn:
                authorizing();
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
        @Nullable FragmentActivity fragment = getActivity();
        if(fragment == null) return;
        InputMethodManager imm = (InputMethodManager) fragment.getSystemService(Context.INPUT_METHOD_SERVICE);
        @Nullable View focus = fragment.getCurrentFocus();
        if (focus == null)
        {
            return;
        }
        @Nullable IBinder binder = focus.getWindowToken();
        if (binder == null)
        {
            return;
        }
        imm.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    private abstract class SessionEnterLogin extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return LoadSessionKeyTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
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