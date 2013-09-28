package com.ltst.przwrd.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SendRegistrationIdTask implements IBcTask
{
    public static final @Nonnull String BF_REGISTRATION_ID = "SendRegistrationIdTask.registrationId";
    public static final @Nonnull String BF_SESSION_KEY = "SendRegistrationIdTask.sessionKey";

    public static final @Nonnull
    Intent createIntent(@Nonnull String registrationId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_REGISTRATION_ID, registrationId);
        return intent;
    }

    public static final @Nonnull
    Intent createIntent(@Nonnull String sessionKey, @Nonnull String registrationId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_REGISTRATION_ID, registrationId);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull BcTaskEnv env)
    {
        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            Bundle extras = env.extras;
            if (extras == null)
            {
                return null;
            }
            @Nullable String registrationId = extras.getString(BF_REGISTRATION_ID);
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
            if (sessionKey != null)
            {
                if (registrationId != null)
                {
                    sendId(env.context, sessionKey, registrationId);
                }
            }
            else
            {
                if (registrationId != null)
                {
                    sendId(env.context, registrationId);
                }
            }
        }
        return null;
    }

    private void sendId(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull String registrationId)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            client.sendRegistrationId(sessionKey, registrationId);
        }
        catch (Throwable e)
        {
            Log.i("Can't load coefficients from internet");
        }
    }

    private void sendId(@Nonnull Context context, @Nonnull String registrationId)
    {
        try
        {
            IGcmRestClient client = GcmRestClient.create(context);
            client.sendRegistrationId(registrationId);
        }
        catch (Throwable e)
        {
            Log.i("Can't load coefficients from internet");
        }
    }
}
