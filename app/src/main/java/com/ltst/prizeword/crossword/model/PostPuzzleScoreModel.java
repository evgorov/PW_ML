package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PostPuzzleScoreModel
{
    @Nonnull IBcConnector mBcConnector;
    @Nonnull String mSessionKey;
    private boolean mIsDestroyed;
    private @Nonnull Uploader mUploader;

    public PostPuzzleScoreModel(@Nonnull String sessionKey, @Nonnull IBcConnector bcConnector)
    {
        mSessionKey = sessionKey;
        mBcConnector = bcConnector;
        mUploader = new Uploader();
    }

    public void post(final @Nonnull String puzzleId, final int score)
    {
        if(mIsDestroyed)
            return;
        mUploader.setIntent(PostPuzzleScoreTask.createIntent(mSessionKey, puzzleId, score));
        mUploader.update(null);
    }

    public void close()
    {
        Log.i("PostPuzzleScoreModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("PostPuzzleScoreModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mUploader.close();

        mIsDestroyed = true;
        Log.i("PostPuzzleScoreModel.destroy() end"); //$NON-NLS-1$
    }


    private class Uploader extends ModelUpdater<DbService.DbTaskEnv>
    {
        private @Nullable Intent mIntent;

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

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
