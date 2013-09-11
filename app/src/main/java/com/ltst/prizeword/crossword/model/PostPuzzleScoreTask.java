package com.ltst.prizeword.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestParams;
import com.ltst.prizeword.score.ScoreQueue;


import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostPuzzleScoreTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_SESSION_KEY = "PostPuzzleScoreTask.sessionKey";
    public static final @Nonnull String BF_PUZZLE_ID = "PostPuzzleScoreTask.puzzleId";
    public static final @Nonnull String BF_SCORE = "PostPuzzleScoreTask.score";
    public static final @Nonnull String BF_STATUS = "PostPuzzleScoreTask.status";

    public static final Intent createIntent(@Nonnull String sessionKey, @Nonnull String puzzleId, int score)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_PUZZLE_ID, puzzleId);
        intent.putExtra(BF_SCORE, score);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if (extras == null)
        {
            return null;
        }
        @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nullable String puzzleId = extras.getString(BF_PUZZLE_ID);
        int scoreValue = extras.getInt(BF_SCORE);

        if (!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
            if (puzzleId != null && scoreValue >= 0)
            {
                ScoreQueue.Score score = new ScoreQueue.Score(0, scoreValue, puzzleId);
                env.dbw.putScoreToQueue(score);
            }
        }
        else
        {
            if (sessionKey != null && puzzleId != null && scoreValue >= 0)
            {
                HttpStatus status = postScore(env.context, sessionKey, puzzleId, scoreValue);
                if (status != null && status.value() == RestParams.SC_ERROR)
                {
                    ScoreQueue.Score score = new ScoreQueue.Score(0, scoreValue, puzzleId);
                    env.dbw.putScoreToQueue(score);
                }
                return packToBundle(status);
            }
        }
        return null;
    }

    private @Nullable Bundle packToBundle(@Nonnull HttpStatus status)
    {
        Bundle b = new Bundle();
        b.putInt(BF_STATUS, status.value());
        return b;
    }

    private @Nullable HttpStatus postScore(@Nonnull Context context, @Nonnull String sessionKey, @Nonnull String puzzleId, int score)
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
