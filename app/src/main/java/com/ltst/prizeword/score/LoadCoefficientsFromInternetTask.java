package com.ltst.prizeword.score;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestCoefficients;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoadCoefficientsFromInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "LoadCoefficientsFromInternetTask.sessionKey";
    public static final @Nonnull String BF_COEFFICIENTS = "LoadCoefficientsFromInternetTask.coefficients";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
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
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
            if (sessionKey != null)
            {
                @Nullable RestCoefficients restCoefficients = loadCoefficients(env.context, sessionKey);
                if (restCoefficients != null)
                {
                    @Nonnull Coefficients coefficients = parseCoefficients(restCoefficients);
                    env.dbw.putCoefficients(coefficients);
                    return getFromDatabase(env);
                }
            }
        }
        return null;
    }

    private @Nullable RestCoefficients loadCoefficients(@Nonnull Context context, @Nonnull String sessionKey)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.getCoefficients(sessionKey);
        }
        catch (Throwable e)
        {
            Log.i("Can't load coefficients from internet");
        }
        return null;
    }

    private @Nonnull Coefficients parseCoefficients(@Nonnull RestCoefficients rest)
    {
        return new Coefficients(0, rest.getTimeBonus(), rest.getFriendBonus(), rest.getFreeBaseScore(),
                rest.getGoldBaseScore(), rest.getBrilliantBaseScore(), rest.getSilver1BaseScore(), rest.getSilver2BaseScore());
    }

    private static @Nonnull Bundle packToBundle(@Nonnull Coefficients coefficients)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_COEFFICIENTS, coefficients);
        return bundle;
    }

    public static @Nullable Bundle getFromDatabase(@Nonnull DbService.DbTaskEnv env)
    {
        @Nullable Coefficients coefficients = env.dbw.getCoefficients();
        if (coefficients != null)
        {
            return packToBundle(coefficients);
        }
        return null;
    }
}
