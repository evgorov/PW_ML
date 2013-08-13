package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;
import com.ltst.prizeword.crossword.model.PuzzleSet;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OneCrosswordActivity extends SherlockActivity implements View.OnClickListener
{
    public static final @Nonnull String BF_PUZZLE_SET = "OneCrosswordActivity.puzzleSet";

    public static @Nonnull Intent createIntent(@Nonnull Context context, @Nonnull PuzzleSet set)
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

    private @Nonnull Button mStopPlayBtn;
    private @Nonnull Button mHintBtn;
    private @Nonnull Button mMenuBtn;
    private @Nonnull Button mNextBtn;
    private @Nonnull View mAlertPause;
    private @Nonnull View mAlertPauseBg;
    private @Nonnull TextView mProgressTextView;
    private @Nonnull SeekBar mProgressSeekBar;


    private boolean mStopPlayFlag;
    private @Nonnull Animation mAnimationSlideInTop;
    private @Nonnull Animation mAnimationSlideOutTop;

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
        mNextBtn = (Button) findViewById(R.id.gamefild_next_btn);
        mMenuBtn = (Button) findViewById(R.id.gamefild_menu_btn);
        mStopPlayBtn = (Button) findViewById(R.id.header_stop_play_btn);
        mHintBtn = (Button) findViewById(R.id.header_hint_btn);
        mAlertPause = findViewById(R.id.gamefild_pause_alert);
        mAlertPauseBg = findViewById(R.id.gamefild_pause_bg);
        mProgressTextView = (TextView) findViewById(R.id.gamefield_progressbar_percent);
        mProgressSeekBar = (SeekBar)findViewById(R.id.gamefield_progressbar);
        mProgressSeekBar.setEnabled(false);
        mStopPlayFlag = true;
        mAnimationSlideInTop = AnimationUtils.loadAnimation(this,R.anim.forget_slide_in_succes_view);
        mAnimationSlideOutTop = AnimationUtils.loadAnimation(this,R.anim.forget_slide_out_succes_view);
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        mPuzzleAdapter = new PuzzleResourcesAdapter(mBcConnector, mSessionKey, mPuzzleSet);
        mPuzzleAdapter.setPuzzleUpdater(new IListenerVoid()
        {
            @Override
            public void handle()
            {
                int percent = mPuzzleAdapter.getSolvedQuestionsPercent();
                mProgressTextView.setText(String.valueOf(percent));
                mProgressSeekBar.setProgress(percent);
            }
        });
        mPuzzleAdapter.updatePuzzle(mCurrentPuzzleServerId);
        mPuzzleView.setAdapter(mPuzzleAdapter);
        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);
        mStopPlayBtn.setOnClickListener(this);
        mHintBtn.setOnClickListener(this);
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

    @Override public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.gamefild_menu_btn:
                onBackPressed();
                break;
            case R.id.gamefild_next_btn:
                break;
            case R.id.header_hint_btn:
                break;
            case R.id.header_stop_play_btn:
                if (!mStopPlayFlag)
                {

                    mAlertPauseBg.setVisibility(View.VISIBLE);
                    mAnimationSlideInTop.reset();
                    mAlertPause.clearAnimation();
                    mAlertPause.startAnimation(mAnimationSlideInTop);
                    mStopPlayBtn.setBackgroundResource(R.drawable.header_play_but);
                    mStopPlayFlag=true;

                } else
                {
                    mAnimationSlideOutTop.reset();
                    mAlertPause.clearAnimation();
                    mAlertPause.startAnimation(mAnimationSlideOutTop);
                    mAlertPauseBg.setVisibility(View.GONE);
                    mStopPlayBtn.setBackgroundResource(R.drawable.header_stop_but);
                    mStopPlayFlag=false;
                }
                break;
        }
    }
}
