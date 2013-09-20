package com.ltst.prizeword.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleSetModel implements IPuzzleSetModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nullable List<PuzzleSet> mPuzzleSetList;
    private @Nonnull HashMap<String,List<Puzzle>> mPuzzlesSet;
    private @Nonnull PuzzleUserDataSynchronizer mSynchronizer;
    private int hintsCount;
    private boolean mIsDestroyed;
    private boolean answerState;

    public PuzzleSetModel(@Nonnull Context mContext, @Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        mSynchronizer = new PuzzleUserDataSynchronizer(mContext);
        hintsCount = 0;
    }

    @Override
    public boolean isAnswerState() {
        return answerState;
    }

    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleSetsDbUpdater.update(handler);
    }

    @Override
    public void updateTotalDataByDb(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleSetsDbUpdater.update(handler);
    }

    @Override
    public void updateCurrentSets(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleCurrentSetsUpdater.update(handler);
    }

    @Override
    public void updateOneSet(@Nonnull String puzzleSetServerId, @Nonnull IListenerVoid handler) {
        if(mIsDestroyed)
            return;
        Intent intent = LoadPuzzleSetsFromInternet.createOneSetIntent(mSessionKey, puzzleSetServerId);
        mPuzzleOneSetUpdater.setIntent(intent);
        mPuzzleOneSetUpdater.update(handler);
    }

    @Override
    public void buyCrosswordSet(@Nonnull String setServerId, @Nonnull String receiptData, @Nonnull String signature, @Nullable IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        @Nonnull Intent intent = LoadPuzzleSetsFromInternet.createBuyCrosswordSetIntent(mSessionKey, setServerId, receiptData, signature);
        mBuyPuzzleTotalSetUpdater.setIntent(intent);
        mBuyPuzzleTotalSetUpdater.update(handler);
    }

    public void synchronizePuzzleUserData()
    {
        List<String> ids = mSynchronizer.getNotUpdatedPuzzlesIds();
        if (ids == null)
        {
            return;
        }
        ArrayList<Puzzle> puzzlesToUpdate = new ArrayList<Puzzle>(ids.size());
        outer_loop: for (List<Puzzle> puzzleList : mPuzzlesSet.values())
        {
            for (Puzzle puzzle : puzzleList)
            {
                if(ids.contains(puzzle.serverId))
                {
                    puzzlesToUpdate.add(puzzle);
                    if(puzzlesToUpdate.size() == ids.size())
                        break outer_loop;
                }
            }
        }
        mSynchronizer.sync(mBcConnector, mSessionKey, puzzlesToUpdate);
    }

    @Override
    public void close()
    {
        Log.i("PuzzleSetModel.destroy() begin"); //$NON-NLS-1$
        if(mIsDestroyed)
        {
            Log.w("PuzzleSetModel.destroy() called more than once"); //$NON-NLS-1$
        }

        mPuzzleSetsDbUpdater.close();
        mPuzzleSetsInternetUpdater.close();
        Log.i("Closing total set updater");
        mPuzzleTotalSetsInternetUpdater.close();
        mPuzzleCurrentSetsUpdater.close();
        mBuyPuzzleTotalSetUpdater.close();
        mPuzzleOneSetUpdater.close();
        mHintsUpdater.close();

        mSynchronizer.close();

        mIsDestroyed = true;
        Log.i("PuzzleSetModel.destroy() end"); //$NON-NLS-1$

    }

    @Override
    public @Nonnull List<PuzzleSet> getPuzzleSets()
    {
        if (mPuzzleSetList != null)
        {
            return mPuzzleSetList;
        }
        return new ArrayList<PuzzleSet>();
    }

    @Override
    public int getHintsCount()
    {
        return hintsCount;
    }

    @Nonnull
    @Override
    public HashMap<String, List<Puzzle>> getPuzzlesSet() {
        return mPuzzlesSet;
    }

    @Override
    public void updateDataByInternet(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleSetsInternetUpdater.update(handler);
    }

    @Override
    public void updateTotalDataByInternet(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mPuzzleTotalSetsInternetUpdater.update(handler);
    }

    @Override
    public void updateHints(@Nonnull IListenerVoid handler)
    {
        if(mIsDestroyed)
            return;
        mHintsUpdater.update(handler);
    }

    // ==== updaters =====================================

    private Updater mHintsUpdater = new UpdaterHints()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadHintsTask.createIntent(mSessionKey);
        }
    };

    private Updater mPuzzleSetsInternetUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createShortIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mBuyPuzzleTotalSetUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleTotalSetsInternetUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createLongIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleCurrentSetsUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createCurrentSetsIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleOneSetUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }

    };

    private Updater mPuzzleSetsDbUpdater = new UpdaterSets()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromDatabase.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromDatabase.class;
        }
    };

    private abstract class UpdaterHints extends Updater
    {

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadHintsTask.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            hintsCount = result.getInt(LoadHintsTask.BF_HINTS_COUNT);
        }
    };

    private abstract class UpdaterSets extends Updater
    {
        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                answerState = false;
                return;
            }
            answerState = true;
            mPuzzleSetList = LoadPuzzleSetsFromInternet.extractFromBundle(result);
            mPuzzlesSet = (HashMap<String,List<Puzzle> >) result.getSerializable(LoadPuzzleSetsFromInternet.BF_PUZZLES_AT_SET);
        }
    };



    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        public @Nonnull Intent mIntent;

        protected Updater(){}

        public void setIntent(@Nonnull Intent mIntent) {
            this.mIntent = mIntent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

    }


    public enum PuzzleSetType
    {
        BRILLIANT,
        GOLD,
        SILVER,
        SILVER2,
        FREE
    }

    public static final @Nonnull String BRILLIANT = "brilliant";
    public static final @Nonnull String GOLD = "gold";
    public static final @Nonnull String SILVER = "silver";
    public static final @Nonnull String SILVER2 = "silver2";
    public static final @Nonnull String FREE = "free";

    public static @Nullable PuzzleSetType getPuzzleTypeByString(@Nonnull String type)
    {
        if (type.equals(FREE))
            return PuzzleSetType.FREE;
        if (type.equals(GOLD))
            return PuzzleSetType.GOLD;
        if (type.equals(SILVER))
            return PuzzleSetType.SILVER;
        if (type.equals(SILVER2))
            return PuzzleSetType.SILVER2;
        if (type.equals(BRILLIANT))
            return PuzzleSetType.BRILLIANT;
        return null;
    }
}
