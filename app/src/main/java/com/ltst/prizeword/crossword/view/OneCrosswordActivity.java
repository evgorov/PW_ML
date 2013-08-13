package com.ltst.prizeword.crossword.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.tools.ErrorAlertDialog;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OneCrosswordActivity extends SherlockActivity implements View.OnClickListener
{
    public static final @Nonnull String BF_PUZZLE_SET = "OneCrosswordActivity.puzzleSet";
    public static final @Nonnull String BF_HINTS_COUNT = "OneCrosswordActivity.hintsCount";

    public static final @Nonnull String TIMER_TEXT_FORMAT = "%d:%2d";

    public static @Nonnull Intent createIntent(@Nonnull Context context, @Nonnull PuzzleSet set, int hintsCount)
    {
        Intent intent = new Intent(context, OneCrosswordActivity.class);
        intent.putExtra(BF_PUZZLE_SET, set);
        intent.putExtra(BF_HINTS_COUNT, hintsCount);
        return intent;
    }

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;

    private @Nonnull PuzzleView mPuzzleView;
    private @Nonnull PuzzleResourcesAdapter mPuzzleAdapter;
    private @Nonnull String mCurrentPuzzleServerId;
    private @Nonnull HintsModel mHintsModel;
    private int mHintsCount;
    private int mTimeLeft;
    private int mTimeGiven;
    private boolean mTickerLaunched = false;

    private @Nonnull Button mStopPlayBtn;
    private @Nonnull Button mHintBtn;
    private @Nonnull Button mMenuBtn;
    private @Nonnull Button mNextBtn;
    private @Nonnull View mAlertPause;
    private @Nonnull View mAlertPauseBg;
    private @Nonnull TextView mProgressTextView;
    private @Nonnull SeekBar mProgressSeekBar;

    private @Nonnull TextView mTimerTextView;

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
            mHintsCount = extras.getInt(BF_HINTS_COUNT);
        }
        mSessionKey = SharedPreferencesValues.getSessionKey(this);
        mBcConnector = new BcConnector(this);
        mHintsModel = new HintsModel(mBcConnector, mSessionKey);
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
        mTimerTextView = (TextView) findViewById(R.id.header_timer_textview);
        mAnimationSlideInTop = AnimationUtils.loadAnimation(this,R.anim.forget_slide_in_succes_view);
        mAnimationSlideOutTop = AnimationUtils.loadAnimation(this,R.anim.forget_slide_out_succes_view);
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        mStopPlayFlag = true;
        mPuzzleAdapter = new PuzzleResourcesAdapter(mBcConnector, mSessionKey, mPuzzleSet);
        mPuzzleAdapter.setPuzzleUpdater(new IListenerVoid()
        {
            @Override
            public void handle()
            {
                int percent = mPuzzleAdapter.getSolvedQuestionsPercent();
                mProgressTextView.setText(String.valueOf(percent));
                mProgressSeekBar.setProgress(percent);
                mTimeLeft = mPuzzleAdapter.getTimeLeft();
                mTimeGiven = mPuzzleAdapter.getTimeGiven();
                fillTimer();
                if(!mTickerLaunched)
                    tick();
            }
        });
        mPuzzleAdapter.updatePuzzle(mCurrentPuzzleServerId);
        mPuzzleView.setAdapter(mPuzzleAdapter);
        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);
        mStopPlayBtn.setOnClickListener(this);
        mHintBtn.setOnClickListener(this);
        mHintBtn.setText(String.valueOf(mHintsCount));
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        mStopPlayFlag = false;
        mTickerLaunched = false;
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        mPuzzleView.recycle();
        mPuzzleAdapter.updatePuzzleUserData();
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
                useHint();
                break;
            case R.id.header_stop_play_btn:
                if (mStopPlayFlag)
                {
                    mAlertPauseBg.setVisibility(View.VISIBLE);
                    mAnimationSlideInTop.reset();
                    mAlertPause.clearAnimation();
                    mAlertPause.startAnimation(mAnimationSlideInTop);
                    mStopPlayBtn.setBackgroundResource(R.drawable.header_play_but);
                }
                else
                {
                    mAnimationSlideOutTop.reset();
                    mAlertPause.clearAnimation();
                    mAlertPause.startAnimation(mAnimationSlideOutTop);
                    mAlertPauseBg.setVisibility(View.GONE);
                    mStopPlayBtn.setBackgroundResource(R.drawable.header_stop_but);
                    tick();
                }
                mStopPlayFlag = !mStopPlayFlag;
                break;
        }
    }

    private void fillTimer()
    {
        int time = mTimeGiven - mTimeLeft;
        int min = time/60;
        int sec = time - min * 60;
        String timeText = String.format(TIMER_TEXT_FORMAT, min, sec);
        mTimerTextView.setText(timeText);
    }

    private void useHint()
    {
        if(!mPuzzleAdapter.isInputMode())
            return;

        mPuzzleView.hideKeyboard();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.gamefield_hint_dialog_message)
               .setTitle(R.string.gamefield_hint_dialog_title)
               .setPositiveButton(R.string.gamefield_hint_dialog_ok, new DialogInterface.OnClickListener()
               {
                   @Override
                   public void onClick(DialogInterface dialog, int which)
                   {
                       mHintsCount --;
                       mHintsModel.changeHints(-1, new IListenerVoid()
                       {
                           @Override
                           public void handle()
                           {
                               mHintBtn.setText(String.valueOf(mHintsCount));
                               mPuzzleAdapter.setCurrentQuestionCorrect();
                               mPuzzleView.invalidate();
                           }
                       });
                   }
               })
               .setNegativeButton(R.string.gamefield_hint_dialog_cancel, new DialogInterface.OnClickListener()
               {
                   @Override
                   public void onClick(DialogInterface dialog, int which)
                   {
                        mPuzzleView.openKeyboard();
                   }
               });
        builder.create().show();
    }

    private void tick()
    {
        mTickerLaunched = true;
        mTimerTextView.postDelayed(mTicker, 1000);
    }

    private void makeTick()
    {
        mTimeLeft --;
        mPuzzleAdapter.setTimeLeft(mTimeLeft);
        fillTimer();
    }

    private Runnable mTicker = new Runnable()
    {
        @Override
        public void run()
        {
            if(mStopPlayFlag)
            {
                makeTick();
                tick();
            }
        }
    };
}
