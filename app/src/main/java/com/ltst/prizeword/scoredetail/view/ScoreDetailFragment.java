package com.ltst.prizeword.scoredetail.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.invitefriends.view.InviteFriendsFragment;
import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserDataModel;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;
import com.ltst.prizeword.score.Coefficients;
import com.ltst.prizeword.score.CoefficientsModel;
import com.ltst.prizeword.scoredetail.model.ScoreDataModel;
import com.ltst.prizeword.scoredetail.model.SolvedPuzzleSetModel;
import com.ltst.prizeword.sounds.SoundsWork;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScoreDetailFragment extends SherlockFragment implements View.OnClickListener
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
    private @Nonnull TextView mScoreInvitedCount;
    private @Nonnull TextView mScoreTV;
    private @Nonnull TextView mFriendTV;
    private @Nonnull TextView mScoreInMonth;
    private @Nonnull UserDataModel mUserDataModel;
    private @Nonnull Button mInviteBtn;
    private @Nonnull Button mMenuBtn;
    private @Nonnull IFragmentsHolderActivity mINavigationActivity;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;

    private @Nonnull ScoreCrosswordFragmentHolder mScoreHolder;

    @Override public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        mINavigationActivity = (IFragmentsHolderActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.detail_score_fragment_layout, container, false);


        mScoreListVeiw = (ListView) v.findViewById(R.id.score_listview);
        mFooterView = inflater.inflate(R.layout.score_footer, null);
        mInviteBtn = (Button) mFooterView.findViewById(R.id.score_invite_btn);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mCrosswordView = inflater.inflate(R.layout.score_crossword_panel_container, null);
        mScoreInvitedCount = (TextView) mCrosswordView.findViewById(R.id.score_invited_panel_ratio);
        mScoreTV = (TextView) mCrosswordView.findViewById(R.id.score_invited_score);
        mFriendTV = (TextView) mCrosswordView.findViewById(R.id.score_friends_name_from_count);
        mScoreInMonth = (TextView) v.findViewById(R.id.score_in_month_header);
        mScoreHolder = new ScoreCrosswordFragmentHolder(mContext, this, inflater, mCrosswordView);
        mScoreListVeiw.setDivider(null);
        mScoreListVeiw.addFooterView(mFooterView);
        mScoreListVeiw.addHeaderView(mCrosswordView);
        return v;
    }

    @Override
    public void onStart()
    {
        Log.i("DetailScore.onStart()"); //$NON-NLS-1$
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mCoefModel = new CoefficientsModel(mSessionKey, mBcConnector);
        mCoefModel.updateFromInternet(mRefreshCoef);
        initListView();
        super.onStart();
    }

    private void initListView()
    {
        Log.i("RatingFragment.initListView()"); //$NON-NLS-1$
        assert mContext != null && mBcConnector != null && mSessionKey != null && mCoefModel != null
                : "Fragment must be attached to activity. Context, BcConnector, SessionKey must be initialized";

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        mScoreInMonth.setText(mINavigationActivity.getScoreText());
        mScoreInMonth.append(" в " + mContext.getResources().getStringArray(R.array.menu_group_months_at_predlog_padezh)[month]);

        ScoreDataModel friendmodel = mFriendDataModel;
        if (friendmodel == null)
        {
            friendmodel = new ScoreDataModel(mSessionKey, mBcConnector);
            mFriendDataModel = friendmodel;
        }

        ScoreDetailAdapter adapter = mScoreDetailAdapter;
        if (adapter == null)
        {

            adapter = new ScoreDetailAdapter(mContext, mFriendDataModel, mCoefModel);
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
            adapter.updateCoefBynternet();
        }
        mInviteBtn.setOnClickListener(this);
        mMenuBtn.setOnClickListener(this);

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
            mPuzzleSetModel = null;
            mCoefModel = null;
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

    private final @Nonnull IListenerVoid mRefreshCoef = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            assert mScoreDetailAdapter != null;

            int count = mScoreDetailAdapter.getCountFriends();
            String[] str = mContext.getResources().getStringArray(R.array.friends_name_from_count);
            if (Integer.toString(count).endsWith("2") || Integer.toString(count).endsWith("3") || Integer.toString(count).endsWith("4"))
                mFriendTV.setText(str[1]);
            else if (Integer.toString(count).endsWith("1"))
                mFriendTV.setText(str[0]);
            else
                mFriendTV.setText(str[2]);
            mScoreInvitedCount.setText(Integer.toString(count));
            assert mCoefModel != null;
            Coefficients coefficients = mCoefModel.getCoefficients();
            assert coefficients != null;

            mScoreTV.setText(Integer.toString(coefficients.friendBonus * count));

        }
    };


    @Override public void onClick(View view)
    {
        SoundsWork.interfaceBtnMusic(mContext);
        switch (view.getId())
        {
            case R.id.score_invite_btn:
                mINavigationActivity.selectNavigationFragmentByClassname(InviteFriendsFragment.FRAGMENT_CLASSNAME);
                break;
            case R.id.header_menu_btn:
                mINavigationDrawerHolder.toogle();
                break;
        }
    }
}
