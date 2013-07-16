package com.ltst.prizeword.vk;

import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.actionbarsherlock.app.SherlockFragment;
import com.perm.kate.api.Api;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 16.07.13.
 */
public class VkLoginFragment extends SherlockFragment {

    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.vk.VkLoginFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = VkLoginFragment.class.getName();

    private final int REQUEST_LOGIN = 1;

    private @Nonnull Button mButtonLogin;
    private @Nonnull Context mContext;

    private @Nonnull Api api;
    private @Nonnull VkAccount account = new VkAccount();


    @Override
    public void onAttach(Activity activity) {
        mContext = (Context) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_vk_login, container, false);
        mButtonLogin = (Button) v.findViewById(R.id.vk_login_button);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mButtonLogin.setOnClickListener(onLick);

        Resources res = this.getResources();
        //Восстановление сохранённой сессии
        account.restore(mContext);

        //Если сессия есть создаём API для обращения к серверу
        if(account.access_token!=null)
            api=new Api(account.access_token, res.getString(R.string.VK_API_ID));

        super.onActivityCreated(savedInstanceState);
    }

    private View.OnClickListener onLick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            launchCrosswordActivity();
        }
    };

    private void launchCrosswordActivity()
    {
        @Nonnull Intent intent = new Intent(mContext, VkLoginActivity.class);
        mContext.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == SherlockActivity.RESULT_OK) {
                //авторизовались успешно
                Resources res = this.getResources();
                account.access_token=data.getStringExtra("token");
                account.user_id=data.getLongExtra("user_id", 0);
                account.save(mContext);
                api=new Api(account.access_token, res.getString(R.string.VK_API_ID));
                Toast.makeText(mContext,"YES!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
