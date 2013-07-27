package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.widget.TextView;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.app.DatePickerDialog.OnDateSetListener;
import static android.view.View.VISIBLE;


public class RegisterFragment extends SherlockFragment implements INavigationBackPress, View.OnClickListener

{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();

    private @Nonnull Context mContext;
    private IBcConnector mBcConnector;

    private @Nonnull Button mRegisterDateButton;
    private @Nonnull Button mNavBackButton;

    private @Nonnull FrameLayout mDatePickerFrame;
    private @Nonnull DatePicker mDatePicker;
    SimpleDateFormat dateFormat;
    Date date;

    private @Nonnull Button mRegisterFinishButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull INavigationDrawerHolder mDrawerHolder;

    private @Nonnull EditText mNameInput;
    private @Nonnull EditText mSurnameInput;
    private @Nonnull EditText mEmailInput;
    private @Nonnull EditText mPasswordInput;
    private @Nonnull EditText mPasswordConfirmInput;
    private @Nonnull EditText mCityInput;

    private @Nonnull TextView mEmailLabel;
    private @Nonnull TextView mPassLabel;
    private @Nonnull TextView mRetryPassLabel;

    private @Nonnull Calendar cal;
    private @Nonnull int curYear;
    private @Nonnull int curMonth;
    private @Nonnull int curDay;
    private @Nonnull String pattern;
    private @Nonnull String fr;

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

        mRegisterDateButton = (Button) v.findViewById(R.id.register_date_born_btn);

        mRegisterFinishButton = (Button) v.findViewById(R.id.registration_finish_button);
        mNavBackButton.setOnClickListener(this);
        mRegisterFinishButton.setOnClickListener(this);
        mRegisterDateButton.setOnClickListener(this);

        pattern = "yyyy-MM-dd"; //iso 8061
        fr = "dd MMMM yyyy";


        mNameInput = (EditText) v.findViewById(R.id.register_name_input);
        mSurnameInput = (EditText) v.findViewById(R.id.register_surname_input);
        mEmailInput = (EditText) v.findViewById(R.id.register_email_input);
        mPasswordInput = (EditText) v.findViewById(R.id.register_password_input);
        mPasswordConfirmInput = (EditText) v.findViewById(R.id.register_password_confirm_input);
        mCityInput = (EditText) v.findViewById(R.id.register_city_input);

        mEmailLabel = (TextView) v.findViewById(R.id.register_label_email);
        mPassLabel = (TextView) v.findViewById(R.id.register_label_pass);
        mRetryPassLabel= (TextView) v.findViewById(R.id.register_label_retry_pass);

        cal = Calendar.getInstance();
        curDay = cal.get(Calendar.DAY_OF_MONTH);
        curMonth = cal.get(Calendar.MONTH);
        curYear = (cal.get(Calendar.YEAR)-23);
        cal.set(curYear,curMonth,curDay);
        dateFormat = new SimpleDateFormat(fr);
        mRegisterDateButton.setText(dateFormat.format(cal.getTime()));
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
            validateRegData(Color.WHITE);
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
        else{
            validateRegData(Color.RED);
            showErrorAlertDalog(R.string.register_screen_error_msg);
        }

    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    protected void showErrorAlertDalog(int ResID)
    {
        ErrorAlertDialog alertDialogBuilder = new ErrorAlertDialog(mContext);
        alertDialogBuilder.setMessage(ResID);
        alertDialogBuilder.create().show();
    }
    protected void showDatePickerDialog(){
        DatePickerDialog dp = new DatePickerDialog(mContext,myCallBack,curYear,curMonth,curDay);

        dp.setTitle(R.string.register_screen_date_pick_dialog_title);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.HONEYCOMB){
            cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,cal.get(Calendar.YEAR)-10);
            dp.getDatePicker().setMaxDate(cal.getTimeInMillis());
            dp.getDatePicker().setCalendarViewShown(false);
        }
        dp.show();
    }
    OnDateSetListener myCallBack = new OnDateSetListener()
    {
        @Override public void onDateSet(DatePicker datePicker, int Year, int Month, int Day)
        {
            curDay = Day;
            curMonth=Month;
            curYear= Year;
            Calendar cl = Calendar.getInstance();
            cl.set(Year,Month,Day);
            mRegisterDateButton.setText(dateFormat.format(cl.getTime()));
        }
    };



    protected void validateRegData(int color){
        mNameInput.setHintTextColor(color);
        mSurnameInput.setHintTextColor(color);
        mEmailLabel.setTextColor(color);
        mPassLabel.setTextColor(color);
        mRetryPassLabel.setTextColor(color);
        if(color==Color.RED){
            mNameInput.setBackgroundResource(R.drawable.login_register_edittext_top_error);
            mSurnameInput.setBackgroundResource(R.drawable.login_register_edittext_bottom_error);
            mEmailInput.setBackgroundResource(R.drawable.login_register_edittext_top_error);
            mPasswordInput.setBackgroundResource(R.drawable.login_register_textedit_2_2_error);
            mPasswordConfirmInput.setBackgroundResource(R.drawable.login_register_edittext_bottom_error);
        }
        else{
            mNameInput.setBackgroundResource(R.drawable.login_register_edittext_top);
            mSurnameInput.setBackgroundResource(R.drawable.login_register_edittext_bottom);
            mEmailInput.setBackgroundResource(R.drawable.login_register_edittext_top);
            mPasswordInput.setBackgroundResource(R.drawable.login_register_textedit_2_2);
            mPasswordConfirmInput.setBackgroundResource(R.drawable.login_register_edittext_bottom);
        }
    }
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.registration_nav_back_button:
                hideKeyboard();
                onBackKeyPress();
                break;
            case R.id.registration_finish_button:
                hideKeyboard();
                performRegistration();
                break;
            case R.id.register_date_born_btn:
                hideKeyboard();
                showDatePickerDialog();
                break;
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
                    showErrorAlertDalog(R.string.msg_register_email_exists);
                    /*int msg_id = R.string.msg_register_email_exists;
                    ErrorAlertDialog.showDialog(mContext,msg_id);*/
                    break;
                }
                default:
                {
                    showErrorAlertDalog(R.string.msg_unknown_error);
                    /*int msg_id = R.string.msg_unknown_error;
                    ErrorAlertDialog.showDialog(mContext, msg_id);*/
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
