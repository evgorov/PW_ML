package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nonnull;

import static android.view.View.VISIBLE;

public class RegisterFragment extends SherlockFragment implements View.OnClickListener, INavigationBackPress
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.login.view.RegisterFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RegisterFragment.class.getName();

    private @Nonnull Context mContext;

    private @Nonnull Button mRegisterDateButton;
    private @Nonnull Button mNavBackButton;
    private @Nonnull Button mRegisterSetDateButton;
    private @Nonnull FrameLayout mDatePickerFrame;
    private @Nonnull DatePicker mDatePicker;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;
    SimpleDateFormat format;
    Date date;
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = (Context) activity;
        mFragmentHolder = (IFragmentsHolderActivity) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.register_fragment_layout, container, false);
        mNavBackButton = (Button) v.findViewById(R.id.registration_nav_back_button);
        mRegisterDateButton = (Button) v.findViewById(R.id.register_date_born_btn);
        mRegisterSetDateButton = (Button) v.findViewById(R.id.register_set_date_btn);
        mDatePickerFrame = (FrameLayout) v.findViewById(R.id.date_picker_frame);
        mDatePicker = (DatePicker)  v.findViewById(R.id.datePicker1);
        mNavBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackKeyPress();
            }
        });
        mRegisterDateButton.setOnClickListener(this);
        mRegisterSetDateButton.setOnClickListener(this);
        String pattern = "yyyy-MM-dd";
        format = new SimpleDateFormat(pattern);


        return v;
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(LoginFragment.FRAGMENT_CLASSNAME);
    }

    @Override public void onClick(View v)
    {
        switch (v.getId()){
            case R.id.register_date_born_btn:
                mDatePickerFrame.setVisibility(VISIBLE);
                break;
            case R.id.register_set_date_btn:
                mDatePickerFrame.setVisibility(View.GONE);
                try
                {
                    date=format.parse(mDatePicker.getYear()+"-"+mDatePicker.getMonth()+"-"+ mDatePicker.getDayOfMonth());
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
                mRegisterDateButton.setText(format.format(date));
                break;
        }
    }
}
