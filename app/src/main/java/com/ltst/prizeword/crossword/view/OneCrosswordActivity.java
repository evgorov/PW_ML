package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IOnePuzzleModel;
import com.ltst.prizeword.crossword.model.OnePuzzleModel;
import com.ltst.prizeword.crossword.model.PuzzleSet;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

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

    private @Nonnull CrosswordBackgroundView mCrosswordBgImage;
    private @Nonnull IOnePuzzleModel mPuzzleModel;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;
    private @Nonnull String mCurrentPuzzleServerId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_crossword);
        mCrosswordBgImage = (CrosswordBackgroundView) findViewById(R.id.one_crossword_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mPuzzleSet = extras.getParcelable(BF_PUZZLE_SET);
        }
        mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(0);
        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mBcConnector = new BcConnector(this);
    }

    @Override
    protected void onResume()
    {
        mPuzzleModel = new OnePuzzleModel(mBcConnector, mSessionKey, mCurrentPuzzleServerId, mPuzzleSet.id);
        mPuzzleModel.updateDataByDb(updateHandler);
        mPuzzleModel.updateDataByInternet(updateHandler);
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private @Nonnull IListenerVoid updateHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {

        }
    };
}
