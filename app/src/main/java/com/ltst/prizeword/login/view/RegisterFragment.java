package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.view.CrosswordsFragment;
import com.ltst.prizeword.login.model.LoadSessionKeyTask;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserDataModel;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.navigation.IReloadUserData;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.tools.ChoiceImageSourceHolder;
import com.ltst.prizeword.tools.ErrorAlertDialog;
import com.ltst.prizeword.tools.IBitmapAsyncTask;
import com.ltst.prizeword.tools.BitmapAsyncTask;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.app.DatePickerDialog.OnDateSetListener;

public class RegisterFragment extends SherlockFragment
        implements INavigationBackPress,
        View.OnClickListener,
        IBitmapAsyncTask

{
    private int RESULT_LOAD_IMAGE = 1;
    private int REQUEST_MAKE_PHOTO = 2;

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

    private @Nonnull ChoiceImageSourceHolder mDrawerChoiceDialog;
    private @Nonnull ImageView mIconImg;
    private @Nonnull Button mRegisterFinishButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    private @Nonnull IReloadUserData mIReloadUserData;
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

    private @Nonnull UserDataModel mUserDataModel;
    private @Nonnull BitmapAsyncTask mBitmapAsyncTask;


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) activity;
        mIReloadUserData = (IReloadUserData) activity;
        mBcConnector = ((IBcConnectorOwner) activity).getBcConnector();
        mDrawerHolder = (INavigationDrawerHolder)activity;
        mDrawerHolder.lockDrawerClosed();
        mUserDataModel = new UserDataModel(mContext,mBcConnector);
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
        mIconImg = (ImageView) v.findViewById(R.id.registration_nav_icon_img);
        mIconImg.setClickable(true);
        mIconImg.setFocusable(true);


        mDrawerChoiceDialog = new ChoiceImageSourceHolder(mContext);
        mDrawerChoiceDialog.mGalleryButton.setOnClickListener(this);
        mDrawerChoiceDialog.mCameraButton.setOnClickListener(this);
        mIconImg.setOnClickListener(this);
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

        Locale ruLocale = new Locale("ru","RU");
        Locale.setDefault(ruLocale);
        cal = Calendar.getInstance();
        curDay = cal.get(Calendar.DAY_OF_MONTH);
        curMonth = cal.get(Calendar.MONTH);
        curYear = (cal.get(Calendar.YEAR)-23);
        cal.set(curYear,curMonth,curDay);
        dateFormat = new SimpleDateFormat(fr);
        mRegisterDateButton.setText(dateFormat.format(cal.getTime()));
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
//            // Получаем картинку из галереи;
            Uri chosenImageUri = data.getData();
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), chosenImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Меняем аватарку на панеле;
            setImage(photo);
        }
        if(requestCode == REQUEST_MAKE_PHOTO && resultCode == Activity.RESULT_OK){
            // получаем фото с камеры;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Меняем аватарку на панеле;
            setImage(photo);
        }
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
            ErrorAlertDialog.showDialog(mContext, R.string.register_screen_error_msg);
        }

    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
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
                mNameInput.setText(Strings.EMPTY);
                mSurnameInput.setText(Strings.EMPTY);
                mEmailInput.setText(Strings.EMPTY);
                mPasswordInput.setText(Strings.EMPTY);
                mPasswordConfirmInput.setText(Strings.EMPTY);
                mCityInput.setText(Strings.EMPTY);
                break;
            case R.id.register_date_born_btn:
                hideKeyboard();
                showDatePickerDialog();
                break;
            case R.id.registration_nav_icon_img:
                mDrawerChoiceDialog.show();
                break;
            case R.id.choice_photo_dialog_camera_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем камеру;
                Intent cameraIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_MAKE_PHOTO);
                break;
            case R.id.choice_photo_dialog_gallery_btn:
                mDrawerChoiceDialog.cancel();
                // Вызываем галерею;
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
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

    private abstract class SignUpExecutor extends ModelUpdater<IBcTask.BcTaskEnv>
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

                        BitmapDrawable drawable = (BitmapDrawable) mIconImg.getDrawable();
                        Bitmap bitmap = drawable == null ? null : drawable.getBitmap();
                        mBitmapAsyncTask = new BitmapAsyncTask(RegisterFragment.this);
                        mBitmapAsyncTask.execute(bitmap);
                    }
                    mFragmentHolder.selectNavigationFragmentByClassname(CrosswordsFragment.FRAGMENT_CLASSNAME);
                    break;
                case RestParams.SC_FORBIDDEN:
                {
                    ErrorAlertDialog.showDialog(mContext, R.string.msg_register_email_exists);
                    break;
                }
                default:
                {
                    ErrorAlertDialog.showDialog(mContext, R.string.msg_unknown_error);
                    break;
                }
            }
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

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }
    }

    public void setImage(@Nullable Bitmap bitmap){
        if(bitmap != null){
            int size = (int) getResources().getDimension(R.dimen.size_avatar);
//            if(bitmap.hasAlpha())
//                bitmap.setHasAlpha(false);
            mIconImg.setImageBitmap(Bitmap.createScaledBitmap(bitmap, size, size, false));
        } else {
            mIconImg.setImageResource(R.drawable.login_register_ava_btn);
        }

    }

    private void resetUserData(byte[] userPic){
        // изменить аватарку;
        mUserDataModel.resetUserImage(userPic, mTaskHandlerResetUserPic);

    }

    private IListenerVoid mTaskHandlerResetUserPic = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mIReloadUserData.reloadUserData();
            UserData data = mUserDataModel.getUserData();
            if( data != null ){
//                loadAvatar(data.previewUrl);
            } else {
//                mDrawerHeader.setImage(null);
            }
//            loadUserDataFromInternet();
        }
    };

    @Override
    public void bitmapConvertToByte(@Nullable byte[] buffer) {
        // Отправляем новую аватарку насервер;
        resetUserData(buffer);
    }

}
