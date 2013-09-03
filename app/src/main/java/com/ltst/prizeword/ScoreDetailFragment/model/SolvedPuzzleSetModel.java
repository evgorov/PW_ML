package com.ltst.prizeword.scoredetailfragment.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.LoadPuzzleSetsFromDatabase;
import com.ltst.prizeword.crossword.model.LoadPuzzleSetsFromInternet;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
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

public class SolvedPuzzleSetModel implements IPuzzleSetModel
{
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nullable List<PuzzleSet> mPuzzleSetList;
    private @Nonnull HashMap<String,List<Puzzle>> mPuzzlesSet;
    private int hintsCount;
    private boolean mIsDestroyed;

    public SolvedPuzzleSetModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
    {
        mBcConnector = bcConnector;
        mSessionKey = sessionKey;
        hintsCount = 0;
    }

    @Override
    public void updateDataByDb(@Nonnull IListenerVoid handler)
    {
        mPuzzleSetsDbUpdater.update(handler);
    }

    @Override
    public void updateTotalDataByDb(@Nonnull IListenerVoid handler)
    {
        mPuzzleSetsDbUpdater.update(handler);
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
        mPuzzleSetsInternetUpdater.update(handler);
    }

    @Override
    public void updateTotalDataByInternet(@Nonnull IListenerVoid handler)
    {
        mPuzzleTotalSetsInternetUpdater.update(handler);
    }

    // ==== updaters =====================================

    private Updater mPuzzleSetsInternetUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createSortIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleTotalSetsInternetUpdater = new Updater()
    {
        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromInternet.createLongIntent(mSessionKey);
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPuzzleSetsFromInternet.class;
        }
    };

    private Updater mPuzzleSetsDbUpdater = new Updater()
    {

        @Nonnull
        @Override
        protected Intent createIntent()
        {
            return LoadPuzzleSetsFromDatabase.createIntent();
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass()
        {
            return LoadPuzzleSetsFromDatabase.class;
        }
    };

    private abstract class Updater extends ModelUpdater<DbService.DbTaskEnv>
    {
        protected Updater(){}

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
