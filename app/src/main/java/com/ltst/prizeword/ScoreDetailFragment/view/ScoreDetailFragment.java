package com.ltst.prizeword.scoredetailfragment.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.score.CoefficientsModel;
import com.ltst.prizeword.scoredetailfragment.model.ScoreDataModel;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

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
    private @Nullable ScoreDetailAdapter mScoreDetailAdapter;
    private @Nullable CoefficientsModel mCoefModel;
    private @Nullable View mFooterView;
    @Override public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.detail_score_fragment_layout, container, false);
        mScoreListVeiw = (ListView) v.findViewById(R.id.score_listview);
        mFooterView = inflater.inflate(R.layout.score_footer,null);
        mScoreListVeiw.setDivider(null);
        mScoreListVeiw.addFooterView(mFooterView);
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
            mScoreDetailAdapter = null;
        }
        super.onDestroy();

    }

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
