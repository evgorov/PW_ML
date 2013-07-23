package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IOnePuzzleModel;
import com.ltst.prizeword.crossword.model.OnePuzzleModel;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

public class OneCrosswordActivity extends SherlockActivity
{
    public static final @Nonnull String BF_PUZZLE_SERVER_ID = "OneCrosswordActivity.puzzleServerId";

    public static @Nonnull
    Intent createIntent(@Nonnull Context context, @Nonnull String puzzleServerId)
    {
        Intent intent = new Intent(context, OneCrosswordActivity.class);
        intent.putExtra(BF_PUZZLE_SERVER_ID, puzzleServerId);
        return intent;
    }

    private @Nonnull CrosswordBackgroundView mCrosswordBgImage;
    private @Nonnull IOnePuzzleModel mPuzzleModel;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mPuzzleServerId;
    private @Nonnull String mSessionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_crossword);
        mCrosswordBgImage = (CrosswordBackgroundView) findViewById(R.id.one_crossword_view);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mPuzzleServerId = extras.getString(BF_PUZZLE_SERVER_ID);
        }
        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mBcConnector = new BcConnector(this);
    }

    @Override
    protected void onResume()
    {
        mPuzzleModel = new OnePuzzleModel(mBcConnector, mSessionKey, mPuzzleServerId);
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
