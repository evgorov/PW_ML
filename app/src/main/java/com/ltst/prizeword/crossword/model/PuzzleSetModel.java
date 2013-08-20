package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

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
    private @Nonnull HashMap<String,List<Puzzle> > mPuzzleListAtSet;
    private int hintsCount;

    public PuzzleSetModel(@Nonnull IBcConnector bcConnector, @Nonnull String sessionKey)
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
    public HashMap<String, List<Puzzle>> getPuzzleListAtSet() {
        return mPuzzleListAtSet;
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
            return LoadPuzzleSetsFromInternet.createShortIntent(mSessionKey);
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
            mPuzzleListAtSet = (HashMap<String,List<Puzzle> >) result.getSerializable(LoadPuzzleSetsFromInternet.BF_PUZZLES_AT_SET);
        }
    }


    public enum PuzzleSetType
    {
        BRILLIANT,
        GOLD,
        SILVER,
        FREE
    }

    public static final @Nonnull String BRILLIANT = "brilliant";
    public static final @Nonnull String GOLD = "gold";
    public static final @Nonnull String SILVER = "silver";
    public static final @Nonnull String FREE = "free";

    public static @Nullable PuzzleSetType getPuzzleTypeByString(@Nonnull String type)
    {
        if (type.startsWith(FREE))
            return PuzzleSetType.FREE;
        if (type.startsWith(GOLD))
            return PuzzleSetType.GOLD;
        if (type.startsWith(SILVER))
            return PuzzleSetType.SILVER;
        if (type.startsWith(BRILLIANT))
            return PuzzleSetType.BRILLIANT;
        return null;
    }
}
