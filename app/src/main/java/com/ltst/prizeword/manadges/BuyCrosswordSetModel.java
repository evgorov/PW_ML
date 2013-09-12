package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.crossword.model.LoadPuzzleSetsFromInternet;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleUserDataSynchronizer;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 12.09.13.
 */
public class BuyCrosswordSetModel {

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private boolean mIsDestroyed;
    @Nonnull BuyCrosswordSetUpdater mBuyCrosswordSetUpdater;

    private @Nullable List<PuzzleSet> mPuzzleSetList;
    private @Nonnull HashMap<String,List<Puzzle>> mPuzzlesSet;
    private int hintsCount;


    public BuyCrosswordSetModel(@Nonnull IBcConnector mBcConnector, @Nonnull String mSessionKey) {
        this.mBcConnector = mBcConnector;
        this.mSessionKey = mSessionKey;
        mBuyCrosswordSetUpdater = new BuyCrosswordSetUpdater();
    }

    public void buyCrosswordSet(@Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature, @Nullable IListenerVoid handler)
    {
        @Nonnull Intent intent = BuyCrosswordSetOnServerTask.createBuyCrosswordSetIntent(mSessionKey, setServerId, receiptData, signature);
        mBuyCrosswordSetUpdater.setIntent(intent);
        mBuyCrosswordSetUpdater.update(handler);
    }

    public void close()
    {
        Log.i("HintsModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("HintsModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mBuyCrosswordSetUpdater.close();

        mIsDestroyed = true;
        Log.i("HintsModel.destroy() end"); //$NON-NLS-1
    }

    private class BuyCrosswordSetUpdater extends ModelUpdater<DbService.DbTaskEnv>
    {
        private @Nullable
        Intent mIntent;

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
            return BuyCrosswordSetOnServerTask.class;
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
            mPuzzleSetList = LoadPuzzleSetsFromInternet.extractFromBundle(result);
            hintsCount = result.getInt(LoadPuzzleSetsFromInternet.BF_HINTS_COUNT);
            mPuzzlesSet = (HashMap<String,List<Puzzle> >) result.getSerializable(LoadPuzzleSetsFromInternet.BF_PUZZLES_AT_SET);

        }
    }
}
