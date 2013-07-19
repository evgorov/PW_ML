package com.ltst.prizeword.login.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationBackPress;
import com.ltst.prizeword.rest.RestParams;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import java.net.URI;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResetPassFragment extends SherlockFragment implements INavigationBackPress
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.login.ResetPassFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ResetPassFragment.class.getName();

    private static @Nullable String mPassedUrl;
    private static @Nullable String mPasswordToken;

    private static @Nullable String mNewPassword;

    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull EditText mNewPasswordInput;
    private @Nonnull EditText mNewPasswordConfirmInput;
    private @Nonnull Button mResetPasswordButton;
    private @Nonnull Button mNavBackButton;
    private @Nonnull IFragmentsHolderActivity mFragmentHolder;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mBcConnector = ((IBcConnectorOwner)activity).getBcConnector();
        mFragmentHolder = (IFragmentsHolderActivity) getActivity();
    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.resetpass_fragment_layout, container, false);
        mNewPasswordInput = (EditText) v.findViewById(R.id.login_change_pass_newpass_text);
        mNewPasswordConfirmInput = (EditText) v.findViewById(R.id.login_change_pass_confirm_text);
        mResetPasswordButton = (Button) v.findViewById(R.id.login_change_pass_btn);
        mNavBackButton = (Button) v.findViewById(R.id.resetpass_nav_back_btn);
        mNavBackButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackKeyPress();
            }
        });

        mResetPasswordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String pass = mNewPasswordInput.getText().toString();
                String passConfirm = mNewPasswordConfirmInput.getText().toString();
                final String token = mPasswordToken;
                if(pass.equals(passConfirm) && !pass.equals(Strings.EMPTY))
                {
                    ForgetPassUpdater updater = new ForgetPassUpdater()
                    {
                        @Nonnull
                        @Override
                        protected Intent createIntent()
                        {
                            return ForgetPassCycleTask.createResetPasswordIntent(token, pass);
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
            }
        });
        return v;
    }

    public void setUrl(@Nonnull String url)
    {
        mPassedUrl = url;
        parseToken();
    }

    private void parseToken()
    {
        if (mPassedUrl == null)
        {
            return;
        }

        URI uri = URI.create(mPassedUrl);
        List<NameValuePair> values = URLEncodedUtils.parse(uri, "UTF-8");
        for (NameValuePair value : values)
        {
            if(value.getName().equals(RestParams.PARAM_PARSE_TOKEN))
            {
                mPasswordToken = value.getValue();
                break;
            }
        }
    }

    private abstract class ForgetPassUpdater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return ForgetPassCycleTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }

            int statusCode = result.getInt(ForgetPassCycleTask.BF_HTTP_STATUS);

        }
    }

    @Override
    public void onBackKeyPress()
    {
        mFragmentHolder.selectNavigationFragmentByClassname(RegisterFragment.FRAGMENT_CLASSNAME);
    }
}
