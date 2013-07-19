package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthorizationFragment extends SherlockFragment
        implements View.OnClickListener, INavigationBackPress
{
    private @Nonnull String LOG_TAG = "autorization";

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.view.AuthorizationFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = AuthorizationFragment.class.getName();

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull Context mContext;
    private @Nonnull EditText mEmailEditText;
    private @Nonnull EditText mPasswdlEditText;
    private @Nonnull Button mBackPressButton;
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
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
        View v = inflater.inflate(R.layout.authorization_fragment_layout, container, false);
        mEmailEditText = (EditText) v.findViewById(R.id.login_email_etext);
        mPasswdlEditText = (EditText) v.findViewById(R.id.login_passwd_etext);
        mBackPressButton = (Button) v.findViewById(R.id.login_back_button);
        mEnterLoginButton = (Button) v.findViewById(R.id.login_enter_enter_btn);
        mForgetLoginButton = (ImageButton) v.findViewById(R.id.login_forget_btn);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mBackPressButton.setOnClickListener(this);
        mEnterLoginButton.setOnClickListener(this);
        mForgetLoginButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_enter_enter_btn:
                String email = mEmailEditText.getText().toString();
                String passwordf = mPasswdlEditText.getText().toString();
                enterLogin(email, passwordf);
                break;
            case R.id.login_forget_btn:
                mFragmentHolder.selectNavigationFragmentByClassname(ForgetPassFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.login_back_button:
                onBackKeyPress();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackKeyPress() {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    protected void showErrorAlertDalog(){
        Resources res = mContext.getResources();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(res.getString(R.string.error));
        alertDialogBuilder.setMessage(res.getString(R.string.login_enter_error_msg));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setNegativeButton(res.getString(R.string.ok_bnt_title), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    protected void enterLogin(@Nonnull String email, @Nonnull String password){

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
                String sessionKey = spref.getString(SharedPreferencesValues.SP_SESSION_KEY, "");
                Log.i(LOG_TAG, "SESSIONKEY = "+sessionKey);
                if(sessionKey.isEmpty()){

                        showErrorAlertDalog();

//                    Toast.makeText(mContext, "ERROR!", Toast.LENGTH_LONG).show();
                }
                else {
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                }
            }
        });

    }

    private abstract class SessionEnterLogin extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector() {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass() {
            return DbService.class;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadSessionKeyTask.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result) {

            if(result == null)
                return;

            @Nonnull String statusCode = result.getString(LoadSessionKeyTask.BF_STATUS_CODE);
            if(statusCode != RestParams.SC_AURORIZE_ERROR){
                @Nullable String sessionKey = result.getString(LoadSessionKeyTask.BF_SESSION_KEY);
                if(result != null){
                    SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(mContext);
                    spref.putString(SharedPreferencesValues.SP_SESSION_KEY, sessionKey);
                    spref.commit();
                    Log.i(LOG_TAG, "SESSION KEY = " + sessionKey);
                }
            }
        }

    }
}