package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
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

//    private @Nonnull PuzzleView mCrosswordBgImage;
    private @Nonnull IOnePuzzleModel mPuzzleModel;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;
    private @Nonnull String mCurrentPuzzleServerId;

    private @Nonnull PuzzleSurfaceView mPuzzleView;

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
        mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(0);
        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mBcConnector = new BcConnector(this);
    }

    @Override
    protected void onStart()
    {

//        mCrosswordBgImage = (PuzzleView) findViewById(R.id.one_crossword_view);
//        mCrosswordBgImage.setBackgroundTileBitmapRes(R.drawable.bg_dark_tile);
        mPuzzleView = (PuzzleSurfaceView) findViewById(R.id.puzzle_view);
        super.onStart();
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
    protected void onStop()
    {
//        mCrosswordBgImage.recycle();
        mPuzzleView.recycle();
        super.onStop();
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
            @Nullable Puzzle puzzle = mPuzzleModel.getPuzzle();
            if (puzzle != null)
            {
                PuzzleSetModel.PuzzleSetType type = PuzzleSetModel.getPuzzleTypeByString(mPuzzleSet.type);
                PuzzleViewInformation info = new PuzzleViewInformation(type, puzzle.questions);
                info.setBackgroundTileBitmapRes(R.drawable.bg_dark_tile);
//                mCrosswordBgImage.setPuzzleInfo(info);
                mPuzzleView.initializePuzzle(info);
            }
        }
    };
}
