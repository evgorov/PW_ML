package com.ltst.przwrd.score;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.rest.IRestClient;
import com.ltst.przwrd.rest.RestClient;
import com.ltst.przwrd.rest.RestParams;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UploadScoreQueryToInternetTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "UploadScoreQueryToInternetTask.sessionKey";
    public static final @Nonnull String BF_STATUS = "UploadScoreQueryToInternetTask.status";

    public static final Intent createIntent(@Nonnull String sessionKey)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        if (!BcTaskHelper.isNetworkAvailable(env.context))
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
                return getSuccessBundle(false);
            }
            @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
            @Nullable ScoreQueue queue = env.dbw.getScoreQueue();
            List<HttpStatus> statuses = new ArrayList<HttpStatus>();
            if (sessionKey != null && queue != null)
            {
                if(queue.scoreQueue.isEmpty())
                    return getSuccessBundle(true);

                for (ScoreQueue.Score score : queue.scoreQueue)
                {
                    HttpStatus status = postScore(env.context, sessionKey, score.puzzleId, score.score);
                    statuses.add(status);
                }
                boolean success = true;
                for (HttpStatus status : statuses)
                {
                    if (status.value() == RestParams.SC_ERROR)
                    {
                        success = false;
                        break;
                    }
                }
                if(success)
                {
                    env.dbw.clearScoreQueue();
                    return getSuccessBundle(true);
                }
            }
        }
        return getSuccessBundle(false);
    }

    private @Nonnull Bundle getSuccessBundle(boolean success)
    {
        Bundle b = new Bundle();
        b.putInt(BF_STATUS, (success) ? RestParams.SC_SUCCESS : RestParams.SC_ERROR);
        return b;
    }


    private @Nullable
    HttpStatus postScore(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull String puzzleId, int score)
    {
        try
        {
            IRestClient client = RestClient.create(context);
            return client.postPuzzleScore(sessionKey, puzzleId, score);
        }
        catch (Throwable e)
        {
            Log.e("Can't post score to internet");
        }
        return null;
    }
}
