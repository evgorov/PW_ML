package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostPuzzleScoreModel
{
    @Nonnull IBcConnector mBcConnector;
    @Nonnull String mSessionKey;

    public PostPuzzleScoreModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
    }

    public void post(final @Nonnull String puzzleId, final int score)
    {
        Uploader uploader = new Uploader()
        {
            @Nullable
            @Override
            protected Intent createIntent()
            {
                return PostPuzzleScoreTask.createIntent(mSessionKey, puzzleId, score);
            }
        };
        uploader.update(null);
    }

    private abstract class Uploader extends ModelUpdater<DbService.DbTaskEnv>
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
            return PostPuzzleScoreTask.class;
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
            // никак не нужно обрабатывать
        }
    }
}
