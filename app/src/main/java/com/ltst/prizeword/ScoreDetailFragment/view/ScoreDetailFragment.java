package com.ltst.prizeword.scoredetailfragment.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.crossword.view.ICrosswordFragment;
import com.ltst.prizeword.score.CoefficientsModel;
import com.ltst.prizeword.scoredetailfragment.model.ScoreDataModel;
import com.ltst.prizeword.scoredetailfragment.model.SolvedPuzzleSetModel;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreDetailFragment extends SherlockFragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.scoreDetailFragment.scoredetailfragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ScoreDetailFragment.class.getName();

    private @Nonnull ListView mScoreListVeiw;

    private @Nonnull android.content.Context mContext;
    private @Nullable IBcConnector mBcConnector;
    private @Nullable String mSessionKey;
    private @Nullable ScoreDataModel mFriendDataModel;
    private @Nullable SolvedPuzzleSetModel mPuzzleSetModel;
    private @Nullable ScoreDetailAdapter mScoreDetailAdapter;
    private @Nullable CoefficientsModel mCoefModel;
    private @Nonnull View mFooterView;
    private @Nonnull View mCrosswordView;

    private @Nonnull ScoreCrosswordFragmentHolder mScoreHolder;

    @Override public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.detail_score_fragment_layout, container, false);


        mScoreListVeiw = (ListView) v.findViewById(R.id.score_listview);
        mFooterView = inflater.inflate(R.layout.score_footer,null);
        mCrosswordView = inflater.inflate(R.layout.score_crossword_panel_container,null);
        mScoreHolder = new ScoreCrosswordFragmentHolder(mContext, this, inflater,mCrosswordView);
        mScoreListVeiw.setDivider(null);
        mScoreListVeiw.addFooterView(mFooterView);
        mScoreListVeiw.addHeaderView(mCrosswordView);
        return v;
    }

    @Override
    public void onStart()
    {
        Log.i("DetailScore.onStart()"); //$NON-NLS-1$
        initListView();
        super.onStart();
    }

    private void initListView()
    {
        Log.i("RatingFragment.initListView()"); //$NON-NLS-1$
        assert mContext != null && mBcConnector != null && mSessionKey != null
                : "Fragment must be attached to activity. Context, BcConnector, SessionKey must be initialized";

        ScoreDataModel friendmodel = mFriendDataModel;
        if (friendmodel == null)
        {
            friendmodel = new ScoreDataModel(mSessionKey, mBcConnector);
            mFriendDataModel = friendmodel;
        }

        ScoreDetailAdapter adapter = mScoreDetailAdapter;
        if (adapter == null)
        {
            adapter = new ScoreDetailAdapter(mContext, mFriendDataModel);
            mScoreDetailAdapter = adapter;
            adapter.setRefreshHandler(mRefreshHandler);
            mScoreListVeiw.setAdapter(mScoreDetailAdapter);
        }
    }

    @Override
    public void onResume()
    {
        Log.i("RatingFragment.onResume()"); //$NON-NLS-1$
        super.onResume();
        mPuzzleSetModel = new SolvedPuzzleSetModel(mBcConnector, mSessionKey);
        mPuzzleSetModel.updateDataByInternet(updateSetsFromDBHandler);
        ScoreDataModel friendm = mFriendDataModel;
        if (friendm != null)
        {
            friendm.resumeLoading();
        }
        ScoreDetailAdapter adapter = mScoreDetailAdapter;
        if (adapter != null)
        {
            adapter.updateByInternet();
        }
    }

    @Override
    public void onPause()
    {
        Log.i("RatingFragment.onPause()"); //$NON-NLS-1$
        mPuzzleSetModel.close();
        ScoreDataModel friendsm = mFriendDataModel;
        if (friendsm != null)
        {
            friendsm.pauseLoading();
        }
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        Log.i("RatingFragment.onDestroy()"); //$NON-NLS-1$
        ScoreDataModel friendsmodel = mFriendDataModel;
        if (friendsmodel != null)
        {
            friendsmodel.close();
            mFriendDataModel = null;
            mPuzzleSetModel=null;
            mScoreDetailAdapter = null;
        }
        super.onDestroy();

    }

    private void createCrosswordPanel()
    {
        @Nonnull List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
        @Nonnull HashMap<String, List<Puzzle>> mapPuzzles = mPuzzleSetModel.getPuzzlesSet();
        mScoreHolder.fillSet(sets, mapPuzzles);
    }

    private IListenerVoid
            updateSetsFromDBHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            createCrosswordPanel();

            if (!mPuzzleSetModel.getPuzzleSets().isEmpty())
            {
                //skipProgressBar();
                return;
            }
            mPuzzleSetModel.updateTotalDataByInternet(updateSetsFromServerHandler);

        }
    };

    private IListenerVoid
            updateSetsFromServerHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            createCrosswordPanel();
            //skipProgressBar();
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
    private final @Nonnull IListenerVoid mRefreshHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            //ProgressBar bar = mProgressBar;
            //assert bar !=null;
            //bar.setVisibility(View.GONE);
            //mScoreListVeiw.setVisibility(View.VISIBLE);
        }
    };

}
