package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.RestParams;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OnePuzzleModel implements IOnePuzzleModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull String mPuzzleServerId;

    private @Nullable Puzzle mPuzzle;
    private long mSetId;

    public OnePuzzleModel(@Nonnull IBcConnector bcConnector,
                          @Nonnull String sessionKey,
                          @Nonnull String puzzleServerId,
                          long setId)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mPuzzleServerId = puzzleServerId;
        mSetId = setId;
    }

    @Nullable
    @Override
    public Puzzle getPuzzle()
    {
        return mPuzzle;
    }

    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {
        mPuzzleDbUpdater.update(handler);
    }

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        mPuzzleInternetUpdater.update(handler);
    }

    // ==== updaters ==================================================

    private Updater mPuzzleInternetUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadOnePuzzleFromInternet.createIntent(mSessionKey, mPuzzleServerId, mSetId);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadOnePuzzleFromInternet.class;
        }
    };

    private Updater mPuzzleDbUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadOnePuzzleFromDatabase.createIntent(mPuzzleServerId);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadOnePuzzleFromDatabase.class;
        }
    };

    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            int status = result.getInt(LoadOnePuzzleFromInternet.BF_STATUS_CODE);

            if(status == RestParams.SC_SUCCESS)
            {
                mPuzzle = result.getParcelable(LoadOnePuzzleFromInternet.BF_PUZZLE);
            }

        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }
    }

}
