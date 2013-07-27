package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.login.model.ForgetPassCycleTask;
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


public class ForgetPassFragment extends SherlockFragment
        implements View.OnClickListener, View.OnKeyListener, INavigationBackPress
{
    private final @Nonnull String LOG_TAG = "forgetpass";

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ForgetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ForgetPassFragment.class.getName();

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull Context mContext;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;

    private @Nonnull Button mSendEmailButton;
    private @Nonnull Button mBackPressButton;
    private @Nonnull EditText mEmailEditText;
    private @Nonnull View mSuccessAlert;
    private @Nonnull View mSuccessButton;
    private @Nonnull View mSuccessAlertBg;
    private @Nonnull Animation mAnimationSlideInTop;
    private @Nonnull Animation mAnimationSlideOutTop;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mDrawerHolder = (INavigationDrawerHolder)activity;
        mDrawerHolder.lockDrawerClosed();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mSendEmailButton.setOnClickListener(this);
        mBackPressButton.setOnClickListener(this);
        mSuccessButton.setOnClickListener(this);
        mEmailEditText.setOnKeyListener(this);
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
        mSuccessAlert = (View) v.findViewById(R.id.forgetpass_success_send_alert);
        mSuccessButton = (View) v.findViewById(R.id.forgetpass_success_send_ok_btn);
        mAnimationSlideInTop = AnimationUtils.loadAnimation(mContext,R.anim.forget_slide_in_succes_view);
        mAnimationSlideOutTop = AnimationUtils.loadAnimation(mContext,R.anim.forget_slide_out_succes_view);
        mSuccessAlertBg = (View) v.findViewById(R.id.forget_succes_alert);
        return v;
    }

    void sendEmail(){
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
            }
        });
    }

    // ======= LISTENERS =========================

    @Override
    public void onClick(View view)
    {
        switch (view.getId()){
            case R.id.forgetpass_back_btn:
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                onBackKeyPress();
                break;
            case R.id.forgetpass_send_btn:
                sendEmail();
                break;
            case R.id.forgetpass_success_send_ok_btn:
                // Скрываем окно успешной отправки email;
                mAnimationSlideOutTop.reset();
                mSuccessAlertBg.clearAnimation();
                mSuccessAlertBg.startAnimation(mAnimationSlideOutTop);
                mSuccessAlert.setVisibility(View.GONE);
                //  переходим на другой фрагмент;
                onBackKeyPress();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackKeyPress() {
        mFragmentHolder.selectNavigationFragmentByClassname(AuthorizationFragment.FRAGMENT_CLASSNAME);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            sendEmail();
            return true;
        }
        return false;
    }

    // ========= SessionForgotPassword ================

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
            if (result == null)
                return;

            @Nonnull Integer statusCode = result.getInt(ForgetPassCycleTask.BF_HTTP_STATUS);
            Log.d(LOG_TAG, "STATUS_CODE = "+statusCode);
            if (statusCode == RestParams.SC_SUCCESS)
            {
                // Показываем окно успешной отправки email;
                mSuccessAlert.setVisibility(View.VISIBLE);
                mAnimationSlideInTop.reset();
                mSuccessAlertBg.clearAnimation();
                mSuccessAlertBg.startAnimation(mAnimationSlideInTop);
                // скрываем клавиатуру;
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                // Очищаем поле email;
                mEmailEditText.setText(Strings.EMPTY);
            }
            else
            {
                ErrorAlertDialog alertDialogBuilder = new ErrorAlertDialog(mContext);
                alertDialogBuilder.setMessage(R.string.msg_forget_password_error_email);
                alertDialogBuilder.create().show();
           }
        }
    }
}