package com.ltst.prizeword.crossword.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.PostPuzzleScoreModel;
import com.ltst.prizeword.score.CoefficientsModel;
import com.ltst.prizeword.score.ICoefficientsModel;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OneCrosswordActivity extends SherlockActivity implements View.OnClickListener
{
    public static final @Nonnull String BF_PUZZLE_SET = "OneCrosswordActivity.puzzleSet";
    public static final @Nonnull String BF_HINTS_COUNT = "OneCrosswordActivity.hintsCount";

    public static final @Nonnull String TIMER_TEXT_FORMAT = "%d:%2d";

    public static @Nonnull Intent createIntent(@Nonnull Context context, @Nonnull PuzzleSet set, @Nonnull String puzzleServerId, int hintsCount)
    {
        Intent intent = new Intent(context, OneCrosswordActivity.class);
        intent.putExtra(BF_PUZZLE_SET, set);
        intent.putExtra(BF_CURRENT_PUZZLE_SERVER_ID, puzzleServerId);
        intent.putExtra(BF_HINTS_COUNT, hintsCount);
        return intent;
    }

    public static final @Nonnull String BF_SESSION_KEY = "OneCrosswordActivity.sessionKey";
    public static final @Nonnull String BF_CURRENT_PUZZLE_SERVER_ID = "OneCrosswordActivity.currentPuzzleServerId";
    public static final @Nonnull String BF_CURRENT_PUZZLE_INDEX = "OneCrosswordActivity.currentPuzzleIndex";
    public static final @Nonnull String BF_TIME_LEFT = "OneCrosswordActivity.timeLeft";
    public static final @Nonnull String BF_TIME_GIVEN = "OneCrosswordActivity.timeGiven";

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;

    private @Nonnull PuzzleView mPuzzleView;
    private @Nonnull PuzzleResourcesAdapter mPuzzleAdapter;
    private @Nonnull String mCurrentPuzzleServerId;
    private int mCurrentPuzzleIndex = 0;
    private int mPuzzlesCount;

    private @Nonnull HintsModel mHintsModel;
    private int mHintsCount;
    private @Nonnull ICoefficientsModel mCoefficientsModel;
    private @Nonnull PostPuzzleScoreModel mPostPuzzleScoreModel;

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
    private @Nullable Bundle restoredBundle;

    private @Nonnull View mFinalScreen;
    private @Nonnull Button mFinalShareVkButton;
    private @Nonnull Button mFinalShareFbButton;
    private @Nonnull TextView mFinalScore;
    private @Nonnull TextView mFinalBonus;
    private @Nonnull ViewGroup mFinalFlipNumbersViewGroup;
    private @Nonnull Button mFinalMenuButton;
    private @Nonnull Button mFinalNextButton;


    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_one_crossword);

        if (bundle != null)
        {
            restoredBundle = bundle;
            mPuzzleSet = bundle.getParcelable(BF_PUZZLE_SET);
            mSessionKey = bundle.getString(BF_SESSION_KEY);
            mHintsCount = bundle.getInt(BF_HINTS_COUNT);
            mCurrentPuzzleServerId = bundle.getString(BF_CURRENT_PUZZLE_SERVER_ID);
            mCurrentPuzzleIndex = bundle.getInt(BF_CURRENT_PUZZLE_INDEX);
            mTimeGiven = bundle.getInt(BF_TIME_GIVEN);
            mTimeLeft = bundle.getInt(BF_TIME_LEFT);
        }
        else
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                mPuzzleSet = extras.getParcelable(BF_PUZZLE_SET);
                mHintsCount = extras.getInt(BF_HINTS_COUNT);
                mCurrentPuzzleServerId = extras.getString(BF_CURRENT_PUZZLE_SERVER_ID);
            }
            mSessionKey = SharedPreferencesValues.getSessionKey(this);
        }

        mBcConnector = new BcConnector(this);
        mHintsModel = new HintsModel(mBcConnector, mSessionKey);
        mCoefficientsModel = new CoefficientsModel(mSessionKey, mBcConnector);
        mPostPuzzleScoreModel = new PostPuzzleScoreModel(mSessionKey, mBcConnector);
        mPuzzlesCount = mPuzzleSet.puzzlesId.size();
    }

    @Override
    protected void onStart()
    {
        mCoefficientsModel.updateFromDatabase();
        mCoefficientsModel.updateFromInternet();

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

        mFinalScreen = findViewById(R.id.final_screen);

        mFinalShareVkButton = (Button)findViewById(R.id.final_share_vk_btn);
        mFinalShareFbButton = (Button)findViewById(R.id.final_share_fb_btn);
        mFinalScore = (TextView) findViewById(R.id.final_score);
        mFinalBonus = (TextView) findViewById(R.id.final_bonus);
        mFinalFlipNumbersViewGroup = (ViewGroup) findViewById(R.id.final_flip_number);
        mFinalMenuButton = (Button)findViewById(R.id.final_menu_btn);
        mFinalNextButton = (Button)findViewById(R.id.final_next_btn);

        mPuzzleAdapter = new PuzzleResourcesAdapter(mBcConnector, mSessionKey, mPuzzleSet);
        mPuzzleAdapter.setPuzzleUpdater(mPuzzleUpdater);
        mPuzzleAdapter.setPuzzleStateHandler(mStateUpdater);
        mPuzzleAdapter.setPuzzleSolvedHandler(mSolvedUpdater);
        mPuzzleView.setAdapter(mPuzzleAdapter);

        if (restoredBundle != null)
        {
            mPuzzleAdapter.restoreState(restoredBundle);
            mPuzzleView.restoreState(restoredBundle);
            if(!mTickerLaunched)
                tick();
        }
        else
        {
            selectNextUnsolvedPuzzle();
        }

        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);
        mStopPlayBtn.setOnClickListener(this);
        mHintBtn.setOnClickListener(this);
        mFinalMenuButton.setOnClickListener(this);
        mFinalNextButton.setOnClickListener(this);
        mHintBtn.setText(String.valueOf(mHintsCount));

        fillFlipNumbers(0);
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        mStopPlayFlag = true;
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
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(BF_SESSION_KEY, mSessionKey);
        outState.putParcelable(BF_PUZZLE_SET, mPuzzleSet);
        outState.putInt(BF_HINTS_COUNT, mHintsCount);
        outState.putString(BF_CURRENT_PUZZLE_SERVER_ID, mCurrentPuzzleServerId);
        outState.putInt(BF_CURRENT_PUZZLE_INDEX, mCurrentPuzzleIndex);
        outState.putInt(BF_TIME_GIVEN, mTimeGiven);
        outState.putInt(BF_TIME_LEFT, mTimeLeft);
        mPuzzleAdapter.saveState(outState);
        mPuzzleView.saveState(outState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void fillTimer()
    {
        int time = mTimeGiven - mTimeLeft;
        int min = time/60;
        int sec = time - min * 60;
        String timeText = String.format(TIMER_TEXT_FORMAT, min, sec);
        mTimerTextView.setText(timeText);
    }

    @Override public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.final_menu_btn:
            case R.id.gamefild_menu_btn:
                onBackPressed();
                break;
            case R.id.final_next_btn:
            case R.id.gamefild_next_btn:
                selectNextUnsolvedPuzzle();
                break;
            case R.id.header_hint_btn:
                useHint();
                break;
            case R.id.header_stop_play_btn:
                showPauseDialog(mStopPlayFlag);
                break;
        }
    }

    private void showPauseDialog(boolean show)
    {
        if (show)
        {
            mPuzzleView.cancelInput();
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
            if(!mTickerLaunched)
                tick();
        }
        mStopPlayFlag = !show;
    }

    private void showFinalDialog(boolean show)
    {
        if(show)
        {
            mStopPlayFlag = false;
            mFinalScreen.setVisibility(View.VISIBLE);
        }
        else
        {
            mFinalScreen.setVisibility(View.GONE);
        }
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

    private void selectNextUnsolvedPuzzle()
    {
        mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(mCurrentPuzzleIndex);
        mPuzzleAdapter.updatePuzzle(mCurrentPuzzleServerId);
        showPauseDialog(false);
        showFinalDialog(false);
        mCurrentPuzzleIndex++;
        if(mCurrentPuzzleIndex >= mPuzzlesCount)
        {
            mCurrentPuzzleIndex = 0;
        }
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
            else
                mTickerLaunched = false;
        }
    };

    private final @Nonnull IListenerVoid mPuzzleUpdater = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if(mPuzzleAdapter.isPuzzleSolved())
                selectNextUnsolvedPuzzle();
            mStateUpdater.handle();
            if(!mTickerLaunched)
                tick();
        }
    };

    private final @Nonnull IListenerVoid mStateUpdater = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            int percent = mPuzzleAdapter.getSolvedQuestionsPercent();
            mProgressTextView.setText(String.valueOf(percent));
            mProgressSeekBar.setProgress(percent);
            mTimeLeft = mPuzzleAdapter.getTimeLeft();
            mTimeGiven = mPuzzleAdapter.getTimeGiven();
        }
    };

    private final @Nonnull IListenerVoid mSolvedUpdater = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            showFinalDialog(true);
            PuzzleSetModel.PuzzleSetType type = PuzzleSetModel.getPuzzleTypeByString(mPuzzleSet.type);
            int timeSpent = mTimeGiven - mTimeLeft;
            int baseScore = mCoefficientsModel.getBaseScore(type);
            int bonusScore = mCoefficientsModel.getBonusScore(timeSpent, mTimeGiven);

            mFinalScore.setText(String.valueOf(baseScore));
            mFinalBonus.setText(String.valueOf(bonusScore));
            int sumScore = baseScore + bonusScore;

            mPostPuzzleScoreModel.post(mCurrentPuzzleServerId, sumScore);

            fillFlipNumbers(sumScore);
        }
    };

    private void fillFlipNumbers(int score)
    {
        if(mFinalFlipNumbersViewGroup.getChildCount() != 5)
            return;
        @Nullable TextView decThousandsTv = (TextView) mFinalFlipNumbersViewGroup.getChildAt(0);
        @Nullable TextView thousandsTv = (TextView) mFinalFlipNumbersViewGroup.getChildAt(1);
        @Nullable TextView hundredsTv = (TextView) mFinalFlipNumbersViewGroup.getChildAt(2);
        @Nullable TextView tensTv = (TextView) mFinalFlipNumbersViewGroup.getChildAt(3);
        @Nullable TextView lowerThanTenTv = (TextView) mFinalFlipNumbersViewGroup.getChildAt(4);

        assert  decThousandsTv != null &&
                thousandsTv != null &&
                hundredsTv != null &&
                tensTv != null &&
                lowerThanTenTv != null;

        int decThousands = score/10000;
        score -= decThousands * 10000;
        int thousands = score/1000;
        score -= thousands * 1000;
        int hundreds = score/100;
        score -= hundreds * 100;
        int tens = score/10;
        score -= tens * 10;
        int lowerTen = score;

        decThousandsTv.setText(String.valueOf(decThousands));
        thousandsTv.setText(String.valueOf(thousands));
        hundredsTv.setText(String.valueOf(hundreds));
        tensTv.setText(String.valueOf(tens));
        lowerThanTenTv.setText(String.valueOf(lowerTen));
    }

}
