package com.ltst.prizeword.login.view;

import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.login.vk.VkAccount;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class VkLoginFragment extends SherlockFragment {

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.view.VkLoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = VkLoginFragment.class.getName();

    private final int REQUEST_LOGIN = 1;

//    private @Nonnull String FORMAT_BIRTHDAY = "dd.MM.yyyy";

    private @Nonnull Button mButtonLogin;
    private @Nonnull Button mButtonExit;
    private @Nonnull TextView mTextInfo;
    private @Nonnull ImageView mImage;
    private @Nonnull Context mContext;

//    private @Nullable Api api;
    private @Nonnull
com.ltst.prizeword.login.vk.VkAccount account = new VkAccount();


    @Override
    public void onAttach(Activity activity) {
        mContext = (Context) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_vk_login, container, false);
        mButtonLogin = (Button) v.findViewById(R.id.vk_login_button);
        mButtonExit = (Button) v.findViewById(R.id.vk_exit_button);
        mTextInfo = (TextView) v.findViewById(R.id.vk_info);
        mImage = (ImageView) v.findViewById(R.id.vk_photo);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mButtonLogin.setOnClickListener(onLick);
        mButtonExit.setOnClickListener(onLick);

        Resources res = this.getResources();
        //Восстановление сохранённой сессии
        account.restore(mContext);

//        //Если сессия есть создаём API для обращения к серверу
//        if(account.access_token!=null)
//            api=new Api(account.access_token, res.getString(R.string.VK_API_ID));

        showButtons();

        super.onActivityCreated(savedInstanceState);
    }

    private View.OnClickListener onLick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.vk_login_button:{
                    launchLoginActivity();
                }
                case R.id.vk_exit_button:{
                    closeLoginActivity();
                }
                default: break;
                }
        }
    };

    private void launchLoginActivity()
    {
        @Nonnull Intent intent = new Intent(mContext, SocialLoginActivity.class);
        intent.putExtra(SocialLoginActivity.PROVEDER_ID, FRAGMENT_ID);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void closeLoginActivity()
    {
//        api=null;
        account.access_token=null;
        account.user_id=0;
        account.save(mContext);
        showButtons();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(VkAccount.LOG_TAG, "COME ANSWER!");
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == SherlockActivity.RESULT_OK) {
                Log.d(VkAccount.LOG_TAG, "SUSSECCFULLY AUTORITHED!");
                //авторизовались успешно
                Resources res = this.getResources();
                account.access_token=data.getStringExtra(VkAccount.ACCOUNT_ACCESS_TOKEN);
                account.user_id=data.getLongExtra(VkAccount.ACCOUNT_USER_ID, 0);
                account.save(mContext);
//                api=new Api(account.access_token, res.getString(R.string.VK_API_ID));
                showButtons();
            }
        }
    }

    void showButtons(){
//        if(api!=null){
        if(account.access_token!=null){
            mButtonLogin.setVisibility(View.GONE);
            mButtonExit.setVisibility(View.VISIBLE);
            mTextInfo.setVisibility(View.VISIBLE);
            mImage.setVisibility(View.VISIBLE);
//            mTextInfo.setText(getInfo());
        }else{
            mButtonLogin.setVisibility(View.VISIBLE);
            mButtonExit.setVisibility(View.GONE);
            mTextInfo.setVisibility(View.GONE);
            mImage.setVisibility(View.GONE);
        }
    }

//    private int getAge (int _year, int _month, int _day) {
//
//        GregorianCalendar cal = new GregorianCalendar();
//        int y, m, d, a;
//
//        y = cal.get(Calendar.YEAR);
//        m = cal.get(Calendar.MONTH) + 1;
//        d = cal.get(Calendar.DAY_OF_MONTH);
//        cal.set(_year, _month, _day);
//        a = y - cal.get(Calendar.YEAR);
//        if ((m < cal.get(Calendar.MONTH))
//                || ((m == cal.get(Calendar.MONTH)) && (d < cal
//                .get(Calendar.DAY_OF_MONTH)))) {
//            --a;
//        }
//        if(a < 0)
//            throw new IllegalArgumentException("Age < 0");
//        return a;
//    }

//    private String getInfo(){
//        String text = null;
//        Collection<Long> uids = new ArrayList<Long>();
//        uids.add(account.user_id);
//        try {
//            if(api == null)
//                Log.d(LOG_TAG, "API IS NULL!");
//            ArrayList<User> uss = api.getProfiles(uids, null, null, null, null, null);
//            User us = uss.get(0);
//
//            Collection<Long> cits = new ArrayList<Long>();
//            cits.add((long) us.city);
//            ArrayList<City> cities = api.getCities(cits);
//            City city = cities.get(0);
//
//            Calendar cal = Calendar.getInstance();
//            if(us.birthdate != null){
//
//                SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_BIRTHDAY, Locale.US);
//                try {
//                    cal.setTime(sdf.parse(us.birthdate));
//                } catch (ParseException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//            int age = getAge(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
//
//
//            text = "Last name: "+us.last_name +"\n"
//                    +"First name: "+us.first_name + "\n"
//                    +"Nikname: "+us.nickname+ "\n"
//                    +"Photo: "+us.photo_medium_rec+"\n"
//                    +"City: "+city.name+"\n"
//                    +"Birthday: "+us.birthdate+"\n"
//                    +"Age: "+age;
//        } catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            text = e.toString();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            text = e.toString();
//        } catch (JSONException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            text = e.toString();
//        } catch (KException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            text = e.toString();
//        }
//        return text;
//        return "some text";
//    }
}
