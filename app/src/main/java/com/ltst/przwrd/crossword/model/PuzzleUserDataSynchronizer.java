package com.ltst.przwrd.crossword.model;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.app.ModelUpdater;
import com.ltst.przwrd.app.SharedPreferencesHelper;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.constants.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleUserDataSynchronizer
{
    public static final @Nonnull String SP_NOT_UPDATED_PUZZLE_IDS = "PuzzleUserDataSynchronizer.notUpdatedPuzzleIds";
    protected static final String DELIMETER = "|";
    protected static final String REGEXP_SHIELD = "\\";

    private @Nonnull Context mContext;
    private @Nonnull SharedPreferencesHelper mPreferencesHelper;
    private boolean mNeedSync = false;
    private @Nullable Updater mUpdater;

    public PuzzleUserDataSynchronizer(@Nonnull Context context)
    {
        mContext = context;
        mPreferencesHelper = SharedPreferencesHelper.getInstance(mContext);
    }

    public void addPuzzleId(@Nonnull String puzzleId)
    {
        List<String> idsList = getNotUpdatedPuzzlesIds();
        if(idsList != null && idsList.contains(puzzleId))
            return;

        String currentIds = mPreferencesHelper.getString(SP_NOT_UPDATED_PUZZLE_IDS, Strings.EMPTY);
        StringBuffer sb = new StringBuffer();
        if (currentIds.equals(Strings.EMPTY))
        {
            sb.append(puzzleId);
        }
        else
        {
            sb.append(currentIds);
            sb.append(DELIMETER);
            sb.append(puzzleId);
        }
        mPreferencesHelper.putString(SP_NOT_UPDATED_PUZZLE_IDS, sb.toString()).commit();
    }

    public @Nullable List<String> getNotUpdatedPuzzlesIds()
    {
        String ids = mPreferencesHelper.getString(SP_NOT_UPDATED_PUZZLE_IDS, Strings.EMPTY);
        if(ids.equals(Strings.EMPTY))
            return null;

        mNeedSync = true;
        String[] idsArray = ids.split(REGEXP_SHIELD + DELIMETER);
        return Arrays.asList(idsArray);
    }

    public void sync(IBcConnector bcConnector, String sessionKey, ArrayList<Puzzle> puzzles)
    {
        if(!mNeedSync)
            return;
        mUpdater = new Updater(bcConnector);
        mUpdater.setIntent(SynchronizePuzzleUserDataTask.createIntent(sessionKey, puzzles));
        mUpdater.update(null);
    }

    public void close()
    {
        if(mNeedSync && mUpdater != null)
        {
            mUpdater.close();
            mNeedSync = false;
        }
    }

    private void addPuzzleIdsList(@Nonnull List<String> puzzleIds)
    {
        if(puzzleIds.isEmpty())
        {
            mPreferencesHelper.erase(SP_NOT_UPDATED_PUZZLE_IDS).commit();
            return;
        }
        StringBuffer sb = new StringBuffer();
        String id = puzzleIds.get(0);
        sb.append(id);
        puzzleIds.remove(0);
        for (String puzzleId : puzzleIds)
        {
            sb.append(DELIMETER);
            sb.append(puzzleId);
        }
        mPreferencesHelper.putString(SP_NOT_UPDATED_PUZZLE_IDS, sb.toString()).commit();
    }

    private class Updater extends ModelUpdater<IBcTask.BcTaskEnv>
    {
        private @Nonnull IBcConnector mBcConnector;
        private @Nullable Intent mIntent;

        private Updater(@Nonnull IBcConnector bcConnector)
        {
            mBcConnector = bcConnector;
        }

        public void setIntent(@Nullable Intent intent)
        {
            mIntent = intent;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nullable
        @Override
        protected Intent createIntent()
        {
            return mIntent;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return SynchronizePuzzleUserDataTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }

            ArrayList<String> notSyncedPuzzles = result.getStringArrayList(SynchronizePuzzleUserDataTask.BF_NOT_SYNCED_PUZZLE_IDS);
            if (notSyncedPuzzles == null)
            {
                return;
            }
            addPuzzleIdsList(notSyncedPuzzles);
        }
    }

}
