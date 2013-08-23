package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.swipe.ITouchInterface;
import com.ltst.prizeword.swipe.TouchDetector;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    private int mIndicatorPosition;

//    private @Nonnull IOnePuzzleModel mPuzzleModel;

    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
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
        mRoot = v;
        return v;
    }

    @Override
    public void onResume()
    {
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mHintsManager = new HintsManager(mBcConnector, mSessionKey, mRoot);
        mHintsManager.setHintChangeListener(hintsChangeHandler);
        mPuzzleSetModel = new PuzzleSetModel(mBcConnector, mSessionKey);
//        mPuzzleSetModel.updateDataByInternet(updateSetsFromDBHandler);
        mPuzzleSetModel.updateTotalDataByDb(updateSetsFromDBHandler);
//        mPuzzleSetModel.updateDataByDb(updateSetsFromDBHandler);
//        mPuzzleSetModel.updateTotalDataByInternet(updateSetsFromServerHandler);

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
        switch (view.getId())
        {
            case R.id.crossword_fragment_header_menu_btn:
                if (mINavigationDrawerHolder.isLockDrawerOpen())
                    mINavigationDrawerHolder.lockDrawerClosed();
                else
                    mINavigationDrawerHolder.lockDrawerOpened();
                break;
            case R.id.news_close_btn:
                mNewsLayout.setVisibility(View.GONE);
            default:
                break;
        }
    }

    // =============================================

    private void launchCrosswordActivity()
    {
        List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
        @Nullable PuzzleSet freeSet = null;
        for (PuzzleSet set : sets)
        {
            if (set.type.equals(PuzzleSetModel.FREE))
            {
                freeSet = set;
                break;
            }
        }
        if (freeSet != null)
        {
            @Nonnull Intent intent = OneCrosswordActivity.createIntent(mContext, freeSet, mPuzzleSetModel.getHintsCount());
            mContext.startActivity(intent);
        }
    }

    private void createCrosswordPanel()
    {
        @Nonnull List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = mPuzzleSetModel.getPuzzlesSet();
        for (PuzzleSet set : sets)
        {
            mCrosswordFragmentHolder.addPanel(set);
            @Nonnull List<Puzzle> puzzles = mapPuzzles.get(set.serverId);
            for (@Nonnull Puzzle puzzle : puzzles)
            {
                mCrosswordFragmentHolder.addBadge(puzzle);
            }
        }
    }

    private IListenerVoid
            updateSetsFromDBHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
            createCrosswordPanel();
            mPuzzleSetModel.updateTotalDataByInternet(updateSetsFromServerHandler);
        }
    };

    private IListenerVoid
            updateSetsFromServerHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
            createCrosswordPanel();
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
    public void buyCrosswordSet(String crosswordSetServerId)
    {

    }

    @Override
    public void choiceCrossword()
    {
        launchCrosswordActivity();
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
            if (mIndicatorPosition < mStringsMassive.length-1)
                mIndicatorPosition++;
        }
        mSimpleImage = (ImageView) mRoot.findViewById(mIndicatorPosition);
        mSimpleImage.setImageResource(R.drawable.puzzles_news_page_current);
        mNewsSimpleText.setText(mStringsMassive[mIndicatorPosition]);

    }
}
