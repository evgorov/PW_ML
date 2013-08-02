package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;
import com.ltst.prizeword.crossword.model.IOnePuzzleModel;
import com.ltst.prizeword.crossword.model.OnePuzzleModel;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OneCrosswordActivity extends SherlockActivity
{
    public static final @Nonnull String BF_PUZZLE_SET = "OneCrosswordActivity.puzzleSet";

    public static @Nonnull
    Intent createIntent(@Nonnull Context context, @Nonnull PuzzleSet set)
    {
        Intent intent = new Intent(context, OneCrosswordActivity.class);
        intent.putExtra(BF_PUZZLE_SET, set);
        return intent;
    }

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;

    private @Nonnull PuzzleView mPuzzleView;
    private @Nonnull PuzzleResourcesAdapter mPuzzleAdapter;
    private @Nonnull String mCurrentPuzzleServerId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_crossword);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mPuzzleSet = extras.getParcelable(BF_PUZZLE_SET);
        }
        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mBcConnector = new BcConnector(this);
        mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(0);
    }

    @Override
    protected void onStart()
    {
        mPuzzleView = (PuzzleView) findViewById(R.id.puzzle_view);
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        mPuzzleAdapter = new PuzzleResourcesAdapter(mBcConnector, mSessionKey, mPuzzleSet);
        mPuzzleView.setAdapter(mPuzzleAdapter);
        mPuzzleAdapter.updatePuzzle(mCurrentPuzzleServerId);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        mPuzzleView.recycle();
        super.onStop();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
