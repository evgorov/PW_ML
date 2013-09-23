package com.ltst.prizeword.crossword.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.SharedPreferencesHelper;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.engine.PuzzleResourcesAdapter;
import com.ltst.prizeword.crossword.model.HintsModel;
import com.ltst.prizeword.crossword.model.PostPuzzleScoreModel;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.crossword.sharing.MessageShareModel;
import com.ltst.prizeword.manadges.IIabHelper;
import com.ltst.prizeword.manadges.IManadges;
import com.ltst.prizeword.manadges.IManageHolder;
import com.ltst.prizeword.manadges.ManageHolder;
import com.ltst.prizeword.navigation.INavigationActivity;
import com.ltst.prizeword.navigation.NavigationActivity;
import com.ltst.prizeword.score.CoefficientsModel;
import com.ltst.prizeword.score.ICoefficientsModel;
import com.ltst.prizeword.sounds.IListenerQuestionAnswered;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.tools.CustomProgressBar;
import com.ltst.prizeword.tools.DimenTools;
import com.ltst.prizeword.tools.ErrorAlertDialog;

import org.omich.velo.bcops.client.BcConnector;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OneCrosswordActivity extends SherlockActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, INavigationActivity, Animation.AnimationListener
{
    public static final @Nonnull String BF_PUZZLE_SET = "OneCrosswordActivity.puzzleSet";
    public static final @Nonnull String BF_HINTS_COUNT = "OneCrosswordActivity.hintsCount";

    public static final @Nonnull String TIMER_TEXT_FORMAT = "%02d:%02d";

    static public final @Nonnull String GOOGLE_PLAY_PRODUCT_ID_HINTS_10 = "hints10";

    public static @Nonnull
    Intent createIntent(@Nonnull Context context, @Nonnull PuzzleSet set, @Nonnull String puzzleServerId,
                        int hintsCount, boolean VkSharing, boolean FbSharing)
    {
        Intent intent = new Intent(context, OneCrosswordActivity.class);
        intent.putExtra(BF_PUZZLE_SET, set);
        intent.putExtra(BF_CURRENT_PUZZLE_SERVER_ID, puzzleServerId);
        intent.putExtra(BF_HINTS_COUNT, hintsCount);
        intent.putExtra(BF_VK_SHARING, VkSharing);
        intent.putExtra(BF_FB_SHARING, FbSharing);
        return intent;
    }

    public static final @Nonnull String BF_SESSION_KEY = "OneCrosswordActivity.sessionKey";
    public static final @Nonnull String BF_CURRENT_PUZZLE_SERVER_ID = "OneCrosswordActivity.currentPuzzleServerId";
    public static final @Nonnull String BF_CURRENT_PUZZLE_INDEX = "OneCrosswordActivity.currentPuzzleIndex";
    public static final @Nonnull String BF_TIME_LEFT = "OneCrosswordActivity.timeLeft";
    public static final @Nonnull String BF_TIME_GIVEN = "OneCrosswordActivity.timeGiven";
    public static final @Nonnull String BF_VK_SHARING = "OneCrosswordActivity.vkSharing";
    public static final @Nonnull String BF_FB_SHARING = "OneCrosswordActivity.fbSharing";

    public static final @Nonnull String BF_MUSIC_STOP = "OneCrosswordActivity.musicStop";
    public static final @Nonnull String BF_SOUND_STOP = "OneCrosswordActivity.soundStop";

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull Context mContext;
    private @Nonnull PuzzleSet mPuzzleSet;
    private @Nonnull String mSessionKey;

    private @Nonnull PuzzleView mPuzzleView;
    private @Nonnull PuzzleResourcesAdapter mPuzzleAdapter;
    private @Nullable String mCurrentPuzzleServerId;
    private int mCurrentPuzzleIndex = 0;
    private int mPuzzlesCount;

    private boolean mPuzzleLoaded = false;
    private boolean mHasFirstPuzzle = false;

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
    private @Nonnull View mFinalAlert;
    private @Nonnull View mFinalShareAlert;
    private @Nonnull TextView mProgressTextView;
    private @Nonnull CustomProgressBar mProgressSeekBar;
    private @Nonnull TextView mTimerTextView;

    private boolean mStopPlayFlag;

    private boolean mIsClosed = false;

    private @Nonnull Animation mAnimationSlideInTop;
    private @Nonnull Animation mAnimationSlideOutTop;
    private @Nonnull Animation animForScore;
    private @Nonnull Animation animForBonus;
    private @Nonnull Animation mAnimationSlideInTopForFinal;
    private @Nonnull Animation mAnimationSlideInTopForFinalShare;

    private @Nullable Bundle restoredBundle;
    private @Nonnull View mFinalScreen;
    private @Nonnull Button mFinalShareVkButton;
    private @Nonnull Button mFinalShareFbButton;
    private @Nonnull TextView mFinalScore;
    private @Nonnull TextView mFinalBonus;
    private @Nonnull ViewGroup mFinalFlipNumbersViewGroup;
    private @Nonnull Button mFinalMenuButton;
    private @Nonnull Button mFinalNextButton;
    private @Nonnull android.widget.ToggleButton mPauseMusic;

    private @Nonnull android.widget.ToggleButton mPauseSound;
    private @Nonnull FlipNumberAnimator mFlipNumberAnimator;

    private @Nonnull View mRootView;
    private @Nonnull View mProgressBar;

    private boolean mResourcesDecoded = false;
    private boolean mFbSharing;
    private boolean mVKSharing;
    private @Nullable String mShareMessage;
    private @Nonnull MessageShareModel mShareModel;

    private UiLifecycleHelper uiHelper;

    private @Nonnull ManageHolder mManadgeHolder;
    int mSumScore = 0;
    int mBaseScore = 0;
    int mBonusScore = 0;

    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.activity_one_crossword);

        // Что бы телефон не засыпал при разгадывании сканворда;
//        WindowManager.LayoutParams params = this.getWindow().getAttributes();
//        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//        params.screenBrightness = 0;
//        getWindow().setAttributes(params);

        if (!DimenTools.isTablet(this))
        {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        mBcConnector = new BcConnector(this);
        mManadgeHolder = new ManageHolder(this, mBcConnector);
        mManadgeHolder.instance();
        mManadgeHolder.registerHandlerBuyProductEvent(mManadgeBuyProductIListener);
        mManadgeHolder.registerProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10);

        mRootView = findViewById(R.id.gamefield_root_view);
        mPauseSound = (ToggleButton) findViewById(R.id.pause_sounds_switcher);
        mPauseMusic = (ToggleButton) findViewById(R.id.pause_music_switcher);
        mProgressBar = findViewById(R.id.crossword_progressBar);

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
            mHasFirstPuzzle = true;
            mPauseSound.setChecked(bundle.getBoolean(BF_SOUND_STOP));
            mPauseMusic.setChecked(bundle.getBoolean(BF_MUSIC_STOP));
        } else
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                mPuzzleSet = extras.getParcelable(BF_PUZZLE_SET);
                mHintsCount = extras.getInt(BF_HINTS_COUNT);
                mCurrentPuzzleServerId = extras.getString(BF_CURRENT_PUZZLE_SERVER_ID);
                if (mCurrentPuzzleServerId != null)
                {
                    mHasFirstPuzzle = true;
                }
                mVKSharing = extras.getBoolean(BF_VK_SHARING);
                mFbSharing = extras.getBoolean(BF_FB_SHARING);
            }
            mSessionKey = SharedPreferencesValues.getSessionKey(this);
            mPauseSound.setChecked(SharedPreferencesValues.getSoundSwitch(this));
            SoundsWork.ALL_SOUNDS_FLAG = SharedPreferencesValues.getSoundSwitch(this);
            mPauseMusic.setChecked(SharedPreferencesValues.getMusicSwitch(this));
        }
        mPuzzlesCount = mPuzzleSet.puzzlesId.size();

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(bundle);
    }

    @Override
    public void onBackPressed()
    {
        close();
    }

    @Override
    protected void onStart()
    {
        mContext = this.getBaseContext();
        mHintsModel = new HintsModel(mBcConnector, mSessionKey);
        mCoefficientsModel = new CoefficientsModel(mSessionKey, mBcConnector);
        mPostPuzzleScoreModel = new PostPuzzleScoreModel(mSessionKey, mBcConnector);

        mCoefficientsModel.updateFromDatabase();
        mCoefficientsModel.updateFromInternet(null);

        mPuzzleView = (PuzzleView) findViewById(R.id.puzzle_view);
        mNextBtn = (Button) findViewById(R.id.gamefild_next_btn);
        mMenuBtn = (Button) findViewById(R.id.gamefild_menu_btn);
        mStopPlayBtn = (Button) findViewById(R.id.header_stop_play_btn);
        mHintBtn = (Button) findViewById(R.id.header_hint_btn);
        mAlertPause = findViewById(R.id.gamefild_pause_alert);
        mAlertPauseBg = findViewById(R.id.gamefild_pause_bg);
        mProgressTextView = (TextView) findViewById(R.id.gamefield_progressbar_percent);
        mProgressSeekBar = new CustomProgressBar(getBaseContext(), mRootView, R.id.gamefield_progress_bg, R.id.gamefield_progress_fg);
        mProgressSeekBar.setMinimumWidth(20);
        mProgressSeekBar.setMardginTop(2);
        mProgressSeekBar.setMardginBottom(2);

        mTimerTextView = (TextView) findViewById(R.id.header_timer_textview);
        mAnimationSlideInTop = AnimationUtils.loadAnimation(this, R.anim.forget_slide_in_succes_view);
        mAnimationSlideOutTop = AnimationUtils.loadAnimation(this, R.anim.forget_slide_out_succes_view);
        mAnimationSlideInTopForFinal = AnimationUtils.loadAnimation(this, R.anim.forget_slide_in_succes_view);
        mAnimationSlideInTopForFinalShare = AnimationUtils.loadAnimation(this, R.anim.forget_slide_in_succes_view1);
        animForScore = AnimationUtils.loadAnimation(mContext, R.anim.scale_score_and_bonus);
        animForBonus = AnimationUtils.loadAnimation(mContext, R.anim.scale_score_and_bonus);

        mFinalScreen = findViewById(R.id.final_screen);
        mFinalAlert = findViewById(R.id.final_alert);
        mFinalShareAlert = findViewById(R.id.final_share_alert);
        mFinalShareVkButton = (Button) findViewById(R.id.final_share_vk_btn);
        mFinalShareFbButton = (Button) findViewById(R.id.final_share_fb_btn);
        mFinalScore = (TextView) findViewById(R.id.final_score);
        mFinalBonus = (TextView) findViewById(R.id.final_bonus);
        mFinalFlipNumbersViewGroup = (ViewGroup) findViewById(R.id.final_flip_number);
        mFinalMenuButton = (Button) findViewById(R.id.final_menu_btn);
        mFinalNextButton = (Button) findViewById(R.id.final_next_btn);

        mFinalShareVkButton.setEnabled(mVKSharing);
        mFinalShareFbButton.setEnabled(mFbSharing);

        mFlipNumberAnimator = new FlipNumberAnimator(this, mFinalFlipNumbersViewGroup);

        mPuzzleAdapter = new PuzzleResourcesAdapter(this, mBcConnector, mSessionKey, mPuzzleSet);
        mPuzzleAdapter.setPuzzleUpdater(mPuzzleUpdater);
        mPuzzleAdapter.setPuzzleStateHandler(mStateUpdater);
        mPuzzleAdapter.setPuzzleSolvedHandler(mSolvedUpdater);
        mPuzzleView.setAdapter(mPuzzleAdapter);
        mPuzzleView.setListenerQuestionAnswered(new IListenerQuestionAnswered()
        {
            @Override public void onQuestionAnswered()
            {
                if (SoundsWork.ALL_SOUNDS_FLAG)
                    SoundsWork.questionAnswered(OneCrosswordActivity.this);
            }
        });

        mPuzzleView.setResourcesDecodedHandler(mResourcesDecodingHandler);

        mShareModel = new MessageShareModel(mBcConnector, mSessionKey);

        mNextBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);
        mStopPlayBtn.setOnClickListener(this);
        mHintBtn.setOnClickListener(this);
        mFinalMenuButton.setOnClickListener(this);
        mFinalNextButton.setOnClickListener(this);
        mFinalShareVkButton.setOnClickListener(this);
        mFinalShareFbButton.setOnClickListener(this);
        mHintBtn.setText(String.valueOf(mHintsCount));

        mPauseMusic.setOnCheckedChangeListener(this);
        mPauseSound.setOnCheckedChangeListener(this);
        animForScore.setAnimationListener(this);
        animForBonus.setAnimationListener(this);
        mAnimationSlideInTopForFinal.setAnimationListener(this);
        mAnimationSlideInTopForFinalShare.setAnimationListener(this);

        super.onStart();
    }

    @Override
    protected void onResume()
    {
        if (restoredBundle != null)
        {
            mPuzzleAdapter.restoreState(restoredBundle);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
                mPuzzleView.restoreState(restoredBundle);
            mPuzzleLoaded = true;
            hideProgressBar();
            if (!mTickerLaunched && mPuzzleLoaded)
                tick();
        } else
        {
            loadPuzzle();
        }

//        showFinalDialog(true);
        //fillFlipNumbers(0);
        mResourcesDecoded = false;
        mStopPlayFlag = true;
        if (mPauseMusic.isChecked())
            SoundsWork.startBackgroundMusic(this);

        uiHelper.onResume();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        mStopPlayFlag = false;
        mTickerLaunched = false;
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        mPuzzleAdapter.updatePuzzleUserData();
        mPuzzleAdapter.close();
        mHintsModel.close();
        mCoefficientsModel.close();
        mPostPuzzleScoreModel.close();
        mShareModel.close();
        if (mPauseMusic.isChecked())
            SoundsWork.pauseBackgroundMusic();
        mPuzzleView.recycle();
        mHasFirstPuzzle = false;
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
        outState.putBoolean(BF_MUSIC_STOP, mPauseMusic.isChecked());
        outState.putBoolean(BF_SOUND_STOP, mPauseSound.isChecked());
        mPuzzleAdapter.saveState(outState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
            mPuzzleView.saveState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        if (mPauseMusic.isChecked())
            SoundsWork.pauseBackgroundMusic();
        SoundsWork.releaseMPBack();
        SoundsWork.releaseMPALL();
        SharedPreferencesHelper spref = SharedPreferencesHelper.getInstance(this);
        spref.putBoolean(SharedPreferencesValues.SP_MUSIC_SWITCH, mPauseMusic.isChecked());
        spref.putBoolean(SharedPreferencesValues.SP_SOUND_SWITCH, mPauseSound.isChecked());
        spref.commit();
        uiHelper.onDestroy();
        mManadgeHolder.dispose();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (mManadgeHolder.onActivityResult(requestCode, resultCode, data))
        {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback()
        {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data)
            {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data)
            {
                Log.i("Activity", "Success!");
            }
        });
    }

    private void close()
    {
        if (mStopPlayFlag)
        {
            showPauseDialog(true);
        } else
        {
            showProgressBar();
            mPuzzleAdapter.syncAnsweredQuestions(mOnCloseHandler);
            mPuzzleAdapter.updatePuzzleUserData();
        }
    }

    private void showProgressBar()
    {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar()
    {
        mProgressBar.setVisibility(View.GONE);
    }


    private void fillTimer()
    {
        int time = mTimeGiven - mTimeLeft;
        int min = time / 60;
        int sec = time - min * 60;
        String timeText = String.format(TIMER_TEXT_FORMAT, min, sec);
        mTimerTextView.setText(timeText);
    }

    @Override public void onClick(View v)
    {
        if (SoundsWork.ALL_SOUNDS_FLAG)
            SoundsWork.interfaceBtnMusic(this);
        switch (v.getId())
        {
            case R.id.final_menu_btn:
            case R.id.gamefild_menu_btn:
                close();
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
            case R.id.final_share_vk_btn:
                if (mShareMessage != null)
                {
                    mShareModel.shareMessageToVk(mShareMessage);
                }
                break;
            case R.id.final_share_fb_btn:
                if (mShareMessage != null)
                {
                    if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                            FacebookDialog.ShareDialogFeature.SHARE_DIALOG))
                    {
                        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                                .setDescription(mShareMessage)
                                .build();
                        uiHelper.trackPendingDialogCall(shareDialog.present());
                    } else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(R.string.facebook_app_error);
                        builder.setPositiveButton(R.string.ok_bnt_title, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent marketFbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
                                OneCrosswordActivity.this.startActivity(marketFbIntent);
                            }
                        })
                        .setNegativeButton(R.string.cancel_message_button, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .create().show();
                    }

                }
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
            mProgressSeekBar.repaint();
        } else
        {
            mAnimationSlideOutTop.reset();
            mAlertPause.clearAnimation();
            mAlertPause.startAnimation(mAnimationSlideOutTop);
            mAlertPauseBg.setVisibility(View.GONE);
            mStopPlayBtn.setBackgroundResource(R.drawable.header_stop_but);
            if (!mTickerLaunched && mPuzzleLoaded)
                tick();
        }
        mStopPlayFlag = !show;
    }

    private void showFinalDialog(boolean show)
    {
        if (show)
        {
            mStopPlayFlag = false;
            mFinalScreen.setVisibility(View.VISIBLE);
            mAnimationSlideInTopForFinal.reset();
            mFinalAlert.clearAnimation();
            mFinalAlert.startAnimation(mAnimationSlideInTopForFinal);
        } else
        {
            mFinalScreen.setVisibility(View.GONE);
        }
    }

    private void useHint()
    {
        if (!mPuzzleAdapter.isInputMode())
            return;

        mPuzzleView.hideKeyboard();
        if (mHintsCount <= 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.gamefield_hint_no_message).setTitle(R.string.gamefield_hint_dialog_title)
                    .setPositiveButton(R.string.gamefield_hint_dialog_ok, new DialogInterface.OnClickListener()
                    {
                        @Override public void onClick(DialogInterface dialogInterface, int i)
                        {
                            // сдесь должна быть покупка 10-и подсказок
                            mManadgeHolder.buyProduct(GOOGLE_PLAY_PRODUCT_ID_HINTS_10);
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
        } else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.gamefield_hint_dialog_message)
                    .setTitle(R.string.gamefield_hint_dialog_title)
                    .setPositiveButton(R.string.gamefield_hint_dialog_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mHintsCount--;

                            mHintsModel.changeHints(-1, new IListenerVoid()
                            {
                                @Override
                                public void handle()
                                {
                                    mHintBtn.setText(String.valueOf(mHintsCount));
                                    mPuzzleAdapter.setCurrentQuestionCorrect(new IListenerVoid()
                                    {
                                        @Override
                                        public void handle()
                                        {

                                            mPuzzleView.triggerAnimation();
                                            if (SoundsWork.ALL_SOUNDS_FLAG)
                                                SoundsWork.questionAnswered(OneCrosswordActivity.this);
                                        }
                                    });
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
    }

    private void selectNextUnsolvedPuzzle()
    {
        mPuzzleLoaded = false;
        if (mCurrentPuzzleServerId == null && !mHasFirstPuzzle)
        {
            mPuzzleAdapter.syncAnsweredQuestions(null);
            mCurrentPuzzleIndex++;
            if (mCurrentPuzzleIndex >= mPuzzlesCount)
                mCurrentPuzzleIndex = 0;
            mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(mCurrentPuzzleIndex);
        } else if (mHasFirstPuzzle)
        {
            mCurrentPuzzleIndex = mPuzzleSet.puzzlesId.indexOf(mCurrentPuzzleServerId);
            mHasFirstPuzzle = false;
        }
        loadPuzzle();
    }

    private void loadPuzzle()
    {
        if (mCurrentPuzzleServerId == null)
        {
            mCurrentPuzzleServerId = mPuzzleSet.puzzlesId.get(mCurrentPuzzleIndex);
        }
        if (mHasFirstPuzzle)
        {
            mCurrentPuzzleIndex = mPuzzleSet.puzzlesId.indexOf(mCurrentPuzzleServerId);
            mHasFirstPuzzle = false;
        }
        mPuzzleAdapter.updatePuzzle(mCurrentPuzzleServerId);
        mCurrentPuzzleServerId = null;
        showProgressBar();
        showPauseDialog(false);
        showFinalDialog(false);
    }

    private void formShareMessage(@Nullable String puzzleName, int timeSpent, int sumScore)
    {
        if (puzzleName == null)
            return;
        int timeInMinutes = timeSpent / 60;
        String shareFormat = getResources().getString(R.string.social_puzzle_share_message);
        mShareMessage = String.format(shareFormat, puzzleName, timeInMinutes, sumScore);
    }

    private void tick()
    {
        mTickerLaunched = true;
        mTimerTextView.postDelayed(mTicker, 1000);
    }

    private void makeTick()
    {
        mTimeLeft--;
        mPuzzleAdapter.setTimeLeft(mTimeLeft);
        fillTimer();
    }

    private Runnable mTicker = new Runnable()
    {
        @Override
        public void run()
        {
            if (mStopPlayFlag && mPuzzleLoaded)
            {
                makeTick();
                tick();
            } else
                mTickerLaunched = false;
        }
    };

    private final @Nonnull IListenerVoid mPuzzleUpdater = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mPuzzleAdapter.isPuzzleSolved())
            {
                selectNextUnsolvedPuzzle();
            }
            if (mResourcesDecoded)
            {
                hideProgressBar();
                mPuzzleLoaded = true;
                mStateUpdater.handle();
                if (!mTickerLaunched && mPuzzleLoaded)
                    tick();
            }
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
            if (SoundsWork.ALL_SOUNDS_FLAG)
                SoundsWork.puzzleSolved(OneCrosswordActivity.this);

            PuzzleSetModel.PuzzleSetType type = PuzzleSetModel.getPuzzleTypeByString(mPuzzleSet.type);
            int timeSpent = mTimeGiven - mTimeLeft;
            mBaseScore = mCoefficientsModel.getBaseScore(type);
            int bonus = mCoefficientsModel.getBonusScore(timeSpent, mTimeGiven);
            if (bonus < 0)
                bonus = 0;
            mBonusScore = bonus;

            /*OneCrosswordActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mFinalScore.setText(String.valueOf(baseScore));
                    mFinalScore.setAnimation(animForScore);
                    mFinalScore.startAnimation(animForScore);
                    mFinalBonus.setText(String.valueOf(bonusScore));
                    animForBonus.setStartOffset(1500);
                    mFinalBonus.setAnimation(animForBonus);
                    mFinalBonus.startAnimation(animForBonus);
                }
            });*/

            mSumScore = mBaseScore + mBonusScore;
            mPuzzleAdapter.setScore(mSumScore);
            if (mPuzzleAdapter.isPuzzleInCurrentMonth())
            {
                @Nonnull String puzzleId = mPuzzleSet.puzzlesId.get(mCurrentPuzzleIndex);
                mPostPuzzleScoreModel.post(puzzleId, mSumScore);
                //fillFlipNumbers(mSumScore);
                showFinalDialog(true);
                formShareMessage(mPuzzleAdapter.getPuzzleName(), timeSpent, mSumScore);
            }
        }
    };

    private final @Nonnull IListenerVoid mResourcesDecodingHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mResourcesDecoded = true;
            hideProgressBar();
            mPuzzleLoaded = true;
            mStateUpdater.handle();
            if (!mTickerLaunched && mPuzzleLoaded)
                tick();
        }
    };

    private final @Nonnull IListenerVoid mOnCloseHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            if (mIsClosed)
                return;

            mIsClosed = true;
//            hideProgressBar();
            Intent intent = new Intent();
            intent.putExtra(BF_PUZZLE_SET, mPuzzleSet.serverId);
            setResult(RESULT_OK, intent);
            NavigationActivity.debug("close crossword");
            finish();
        }
    };

    private void fillFlipNumbers(final int score)
    {
        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mFlipNumberAnimator.startAnimation(score);
            }
        });
    }

    @Override public void onCheckedChanged(CompoundButton compoundButton, boolean state)
    {
        if (state)
        {
            switch (compoundButton.getId())
            {
                case R.id.pause_music_switcher:
                    SoundsWork.startBackgroundMusic(this);
                    break;
                case R.id.pause_sounds_switcher:
                    SoundsWork.ALL_SOUNDS_FLAG = true;
                    break;
                default:
                    break;
            }
        } else
        {
            switch (compoundButton.getId())
            {
                case R.id.pause_music_switcher:
                    SoundsWork.pauseBackgroundMusic();
                    break;
                case R.id.pause_sounds_switcher:
                    SoundsWork.ALL_SOUNDS_FLAG = false;
                    break;
                default:
                    break;
            }
        }
    }

    @Nonnull
    IListener<Bundle> mManadgeBuyProductIListener = new IListener<Bundle>()
    {
        @Override
        public void handle(@Nullable Bundle bundle)
        {

            int count = 0;
            final @Nonnull String googleId = ManageHolder.extractFromBundleSKU(bundle);
            if (googleId.equals(GOOGLE_PLAY_PRODUCT_ID_HINTS_10))
            {
                count = 10;
            } else
                return;

            if (mHintsModel != null)
            {
                mHintsModel.changeHints(count, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {

                        // Меняем состояние товара;
                        mManadgeHolder.productBuyOnServer(googleId);
                    }
                });

                mHintsCount += count;

                mHintsModel.changeHints(count, new IListenerVoid()
                {
                    @Override
                    public void handle()
                    {
                        mHintBtn.setText(String.valueOf(mHintsCount));
                        hideProgressBar();
                    }
                });
            }
        }
    };

    @Override
    public void sendMessage(@Nonnull String msg)
    {
        ErrorAlertDialog.showDialog(this, msg);
    }

    @Override public void onAnimationStart(Animation animation)
    {

    }

    @Override public void onAnimationEnd(Animation animation)
    {

        if (animation.equals(mAnimationSlideInTopForFinal))
        {
            OneCrosswordActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mFinalShareAlert.setVisibility(View.VISIBLE);
                    mAnimationSlideInTopForFinalShare.reset();
                    mFinalShareAlert.clearAnimation();
                    mFinalShareAlert.startAnimation(mAnimationSlideInTopForFinalShare);
                }
            });
        }
        if (animation.equals(mAnimationSlideInTopForFinalShare))
        {
            OneCrosswordActivity.this.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mFinalScore.setText(String.valueOf(mBaseScore));
                    mFinalScore.setAnimation(animForScore);
                    mFinalScore.startAnimation(animForScore);
                    mFinalBonus.setText(String.valueOf(mBonusScore));
                    animForBonus.setStartOffset(1500);
                    mFinalBonus.setAnimation(animForBonus);
                    mFinalBonus.startAnimation(animForBonus);
                }
            });
        }

        if (animation.equals(animForBonus) || animation.equals(animForScore))
            SoundsWork.scoreSetSound(mContext);
        if (animation.equals(animForBonus))
            mFlipNumberAnimator.startAnimation(mSumScore);
    }

    @Override public void onAnimationRepeat(Animation animation)
    {

    }
}
