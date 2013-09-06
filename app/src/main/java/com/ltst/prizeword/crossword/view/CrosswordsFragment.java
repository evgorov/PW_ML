package com.ltst.prizeword.crossword.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.manadges.IManadges;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.sounds.SoundsWork;
import com.ltst.prizeword.swipe.ITouchInterface;
import com.ltst.prizeword.swipe.TouchDetector;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public class CrosswordsFragment extends SherlockFragment
        implements View.OnClickListener,
        ICrosswordFragment, ITouchInterface
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.crossword.mRootView.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    private @Nonnull Context mContext;

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;
    private @Nonnull String mSessionKey;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;
    private @Nonnull HintsManager mHintsManager;
    private @Nonnull IManadges mIManadges;

    private @Nonnull View mRoot;
    private @Nonnull TextView mHintsCountView;
    private @Nonnull Button mMenuBackButton;
    private @Nonnull CrosswordFragmentHolder mCrosswordFragmentHolder;
    private @Nonnull int[] mStringsMassive;
    private @Nonnull RelativeLayout mNewsLayout;
    private @Nonnull GestureDetector mGestureDetector;
    private @Nonnull LinearLayout mNewsIndicatorLayout;
    private @Nonnull ImageView mSimpleImage;
    private @Nonnull TextView mNewsSimpleText;
    private @Nonnull ImageView mNewsCloseBtn;
    private @Nonnull ProgressBar mProgressBar;

    private int mIndicatorPosition;

    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mIManadges = (IManadges) activity;
        mBcConnector = ((IBcConnectorOwner) activity).getBcConnector();
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.crossword_fragment_layout, container, false);

        mMenuBackButton = (Button) v.findViewById(R.id.crossword_fragment_header_menu_btn);
        mMenuBackButton.setOnClickListener(this);

        mCrosswordFragmentHolder = new CrosswordFragmentHolder(mContext, this, inflater, v);

        mHintsCountView = (TextView) v.findViewById(R.id.crossword_fragment_current_rest_count);

        mStringsMassive = new int[]{R.string.news1, R.string.news2, R.string.news3, R.string.news4, R.string.news5};
        mNewsLayout = (RelativeLayout) v.findViewById(R.id.news_layout);
        mGestureDetector = new GestureDetector(mContext, new TouchDetector(this));
        mNewsIndicatorLayout = (LinearLayout) v.findViewById(R.id.news_indicator_layout);
        mNewsSimpleText = (TextView) v.findViewById(R.id.news_simple_text);
        mNewsCloseBtn = (ImageView) v.findViewById(R.id.news_close_btn);
        mProgressBar = (ProgressBar) v.findViewById(R.id.archive_progressBar);
        mRoot = v;
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume()
    {
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);

        mHintsManager = new HintsManager(mContext, mIManadges, mBcConnector, mSessionKey, mRoot);

        mHintsManager.setHintChangeListener(hintsChangeHandler);
        mPuzzleSetModel = new PuzzleSetModel(mBcConnector, mSessionKey);
//        mPuzzleSetModel.updateDataByInternet(updateSetsFromDBHandler);
//        mPuzzleSetModel.updateTotalDataByDb(updateSetsFromDBHandler);
        mPuzzleSetModel.updateCurrentSets(updateCurrentSetsHandler);
//        mPuzzleSetModel.updateDataByDb(updateSetsFromDBHandler);
//        mPuzzleSetModel.updateTotalDataByInternet(updateSetsFromServerHandler);


        mNewsIndicatorLayout.removeAllViewsInLayout();
        LinearLayout.LayoutParams params;
        for (int i = 0; i < mStringsMassive.length; i++)
        {
            mSimpleImage = new ImageView(mContext);
            mSimpleImage.setImageResource(R.drawable.puzzles_news_page);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i != 0)
                params.setMargins(4, 0, 0, 0);
            mSimpleImage.setLayoutParams(params);
            mSimpleImage.setId(i);

            mNewsIndicatorLayout.addView(mSimpleImage);
        }
        mIndicatorPosition = 0;
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
        mNewsSimpleText.setText(mStringsMassive[mIndicatorPosition]);

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        mPuzzleSetModel.close();
        super.onStop();
    }

    public void skipProgressBar()
    {
        ProgressBar bar = mProgressBar;
        assert bar != null;
        bar.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
//        mCrossWordButton.setOnClickListener(this);
        mNewsLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override public boolean onTouch(View view, MotionEvent motionEvent)
            {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });
        mNewsCloseBtn.setOnClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override
    public void onClick(View view)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        switch (view.getId())
        {
            case R.id.crossword_fragment_header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
            case R.id.news_close_btn:
                mNewsLayout.setVisibility(View.GONE);
            default:
                break;
        }
    }

    // =============================================


    private void createCrosswordPanel()
    {
        @Nonnull List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = mPuzzleSetModel.getPuzzlesSet();
        if(sets != null && mapPuzzles != null)
        {
            mCrosswordFragmentHolder.fillSet(sets, mapPuzzles);
        }
    }

    private IListenerVoid updateSetsFromDBHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
            createCrosswordPanel();
            @Nonnull List<PuzzleSet> puzzleSets = mPuzzleSetModel.getPuzzleSets();

            if (!puzzleSets.isEmpty())
            {
                Calendar cal = Calendar.getInstance();
                boolean flg = false;
                for(PuzzleSet puzzleSet : puzzleSets)
                {
                    if(puzzleSet.month == cal.get(Calendar.MONTH)+1 && puzzleSet.year == cal.get(Calendar.YEAR))
                    {
                        flg = true;
                    }
                }
                if(!flg)
                {
                    skipProgressBar();
                    return;
                }
                skipProgressBar();
            }
            else
            {
            }
            mPuzzleSetModel.updateTotalDataByInternet(updateSetsFromServerHandler);
        }
    };

    private IListenerVoid updateSetsFromServerHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
            createCrosswordPanel();
            skipProgressBar();
        }
    };

    private IListenerVoid updateCurrentSetsHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
            createCrosswordPanel();
            mPuzzleSetModel.updateTotalDataByDb(updateSetsFromDBHandler);
        }
    };

    private IListenerInt hintsChangeHandler = new IListenerInt()
    {
        @Override
        public void handle(int i)
        {
//            mPuzzleSetModel.updateDataByDb(updateSetsFromDBHandler);
        }
    };

    @Override


    public void buyCrosswordSet(@Nonnull String crosswordSetServerId)
    {

    }

    @Override
    public void choicePuzzle(@Nonnull String setServerId, long puzzleId)
    {
        @Nonnull HashMap<String, List<PuzzleSet>> mapSets = mCrosswordFragmentHolder.getMapSets();
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = mCrosswordFragmentHolder.getMapPuzzles();

        @Nonnull List<Puzzle> puzzles = mapPuzzles.get(setServerId);
        for (Puzzle puzzle : puzzles)
        {
            if (!puzzle.isSolved && puzzle.id == puzzleId)
            {
                for (List<PuzzleSet> listPuzzleSet : mapSets.values())
                {
                    for (PuzzleSet puzzleSet : listPuzzleSet)
                    {
                        if (puzzleSet.serverId.equals(setServerId))
                        {
                            @Nonnull Intent intent = OneCrosswordActivity.createIntent(mContext, puzzleSet, puzzle.serverId, mPuzzleSetModel.getHintsCount());
                            mContext.startActivity(intent);
                            break;
                        }
                    }
                }
                break;
            }
        }
    }


    @Override public void notifySwipe(SwipeMethod swipe)
    {
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page);
        if (swipe.equals(SwipeMethod.SWIPE_RIGHT))
        {
            if (mIndicatorPosition > 0)
                mIndicatorPosition--;
        } else if (swipe.equals(SwipeMethod.SWIPE_LEFT))
        {
            if (mIndicatorPosition < mStringsMassive.length - 1)
                mIndicatorPosition++;
        }
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
        mNewsSimpleText.setText(mStringsMassive[mIndicatorPosition]);

    }
}
