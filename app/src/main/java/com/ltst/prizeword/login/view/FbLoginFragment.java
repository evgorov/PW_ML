package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Context;
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
import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.login.facebook.FbAccount;
import com.ltst.prizeword.login.vk.VkAccount;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 17.07.13.
 */
public class FbLoginFragment extends SherlockFragment {

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.view.FbLoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = FbLoginFragment.class.getName();

    private final int REQUEST_LOGIN = 1;

    private @Nonnull Button mButtonLogin;
    private @Nonnull Button mButtonExit;
    private @Nonnull TextView mTextInfo;
    private @Nonnull ImageView mImage;
    private @Nonnull Context mContext;

    //    private @Nullable Api api;
    private @Nonnull
    com.ltst.prizeword.login.facebook.FbAccount account = new FbAccount();


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
                showButtons();
            }
        }
    }

    void showButtons(){
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

}
