package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.login.model.ForgetPassCycleTask;
import com.ltst.prizeword.login.model.LoadSessionKeyTask;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class ForgetPassFragment extends SherlockFragment
        implements View.OnClickListener, INavigationBackPress
{
    private final @Nonnull String LOG_TAG = "forgetpass";

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ForgetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ForgetPassFragment.class.getName();

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull Context mContext;
    private @Nonnull Button mSendEmailButton;
    private @Nonnull Button mBackPressButton;
    private @Nonnull EditText mEmailEditText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
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
        Log.d(LOG_TAG, "onCreateView");
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
        Log.d(LOG_TAG, "onClick");
        switch (view.getId()){
            case R.id.forgetpass_back_btn:
                onBackKeyPress();
                break;
            case R.id.forgetpass_send_btn:
                Log.d(LOG_TAG, "send");

                final @Nonnull String email = mEmailEditText.getText().toString();
                SessionForgotPassword session = new SessionForgotPassword() {
                    @Nonnull
                    @Override
                    protected Intent createIntent() {
                        return ForgetPassCycleTask.createForgotPasswordIntent(email);
                    }
                };

                session.update(new IListenerVoid(){
                    @Override
                    public void handle() {
                        Log.d(LOG_TAG, "handle");
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackKeyPress() {
        mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
    }

    private abstract class SessionForgotPassword extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector() {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return ForgetPassCycleTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass() {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result) {
            Log.d(LOG_TAG, "come");
            if (result == null)
                return;

            @Nonnull Integer statusCode = result.getInt(ForgetPassCycleTask.BF_HTTP_STATUS);
            Log.d(LOG_TAG, "statusCode = " + statusCode);
//            if (statusCode != RestParams.SC_AURORIZE_ERROR)
//            {
//            }
        }
    }
}