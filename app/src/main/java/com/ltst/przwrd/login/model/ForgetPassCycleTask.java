package com.ltst.przwrd.login.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;

import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ForgetPassCycleTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_PASSWORD_TOKEN = "ForgetPassCycleTask.passwordToken";
    public static final @Nonnull String BF_NEW_PASSWORD = "ForgetPassCycleTask.newPassword";
    public static final @Nonnull String BF_CURRENT_EMAIL = "ForgetPassCycleTask.currentEmail";
    public static final @Nonnull String BF_HTTP_STATUS = "ForgetPassCycleTask.httpStatus";

    public static @Nonnull Intent createResetPasswordIntent(@Nonnull String passwordToken, @Nonnull String newPassword)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_PASSWORD_TOKEN, passwordToken);
        intent.putExtra(BF_NEW_PASSWORD, newPassword);
        return intent;
    }

    public static @Nonnull Intent createForgotPasswordIntent(@Nonnull String email)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_CURRENT_EMAIL, email);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv dbTaskEnv)
    {
            @Nullable Bundle bundle = dbTaskEnv.extras;
            if(bundle == null)
                return null;
        @Nullable String newPassword = bundle.getString(BF_NEW_PASSWORD);
        @Nullable String passwordToken = bundle.getString(BF_PASSWORD_TOKEN);
        @Nullable String currentEmail = bundle.getString(BF_CURRENT_EMAIL);

        if (passwordToken != null && newPassword != null)
        {
            @Nullable HttpStatus status = resetPassword(dbTaskEnv.context, passwordToken, newPassword);
            if (status != null)
            {
                return packStatusToBundle(status);
            }
        }

        if(currentEmail != null)
        {
            @Nullable HttpStatus status = forgotPassword(dbTaskEnv.context, currentEmail);
            if(status != null)
            {
                return packStatusToBundle(status);
            }
        }
        return null;
    }

    private HttpStatus resetPassword(@Nonnull Context context, @Nonnull String passwordToken, @Nonnull String newPassword)
    {
        IRestClient client = RestClient.create(context);
        return client.resetPassword(passwordToken, newPassword);
    }

    private HttpStatus forgotPassword(@Nonnull Context context, @Nonnull String email)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.forgotPassword(email);
        }
        catch(Throwable e)
        {
            Log.i("Can't load data from internet"); //$NON-NLS-1$
            return null;
        }
    }

    private Bundle packStatusToBundle(@Nonnull HttpStatus status)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(BF_HTTP_STATUS, status.value());
        return bundle;
    }
}