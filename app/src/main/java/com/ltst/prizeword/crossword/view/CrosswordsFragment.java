package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.LoadPuzzleSetsFromInternet;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.handlers.IListenerVoid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrosswordsFragment extends SherlockFragment
                                implements View.OnClickListener,
                                ICrosswordFragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.crossword.view.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    private @Nonnull Context mContext;

    private static final int BADGE_ID = 1;

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;
    private @Nonnull String mSessionKey;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;
    private @Nonnull HintsManager mHintsManager;

    private @Nonnull View mRoot;
    private @Nonnull TextView mHintsCountView;
    private @Nonnull Button mMenuBackButton;
    private @Nonnull CrosswordFragmentHolder mCrosswordFragmentHolder;

    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner)activity).getBcConnector();
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

        CrosswordPanelData dataPanel1 = new CrosswordPanelData();
        dataPanel1.mKind = CrosswordPanelData.KIND_ARCHIVE;
        dataPanel1.mType = BadgeData.TYPE_FREE;
        dataPanel1.mMonth = "Июнь";

        dataPanel1.mBadgeData = new BadgeData[1];
        for(int i=0; i<dataPanel1.mBadgeData.length; i++){
            BadgeData badge = new BadgeData();
            badge.mNumber = i+1;
            badge.mStatus = (i%2 == 0) ? BadgeData.STATUS_UNRESOLVED : BadgeData.STATUS_RESOLVED;
            badge.mProgress = 95;
            badge.mScore = 9000;
            dataPanel1.mBadgeData[i] = badge;
        }
        mCrosswordFragmentHolder.addPanel(dataPanel1);

       mHintsCountView = (TextView) v.findViewById(R.id.crossword_fragment_current_rest_count);

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
        mPuzzleSetModel.updateDataByDb(updateHandler);
        mPuzzleSetModel.updateDataByInternet(updateHandler);
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
//        mCrossWordButton.setOnClickListener(this);

        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case BADGE_ID:
//            case R.id.view_crossword:
                launchCrosswordActivity();
                break;

            case R.id.crossword_fragment_header_menu_btn:
                if(mINavigationDrawerHolder.isLockDrawerOpen())
                    mINavigationDrawerHolder.lockDrawerClosed();
                else
                    mINavigationDrawerHolder.lockDrawerOpened();
                break;

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
            if(set.type.equals(PuzzleSetModel.FREE))
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

    private IListenerVoid updateHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mHintsCountView.setText(String.valueOf(mPuzzleSetModel.getHintsCount()));
        }
    };

    private IListenerInt hintsChangeHandler = new IListenerInt()
    {
        @Override
        public void handle(int i)
        {
            mPuzzleSetModel.updateDataByDb(updateHandler);
        }
    };

    @Override
    public void buyCrosswordSet() {

    }

    @Override
    public void choiceCrossword() {
        launchCrosswordActivity();
    }

    private void reloadCrosswordsFromInternet()
    {
        LoadCrosswordFromInternetSession session = new LoadCrosswordFromInternetSession() {
            @Nonnull
            @Override
            protected Intent createIntent() {
                return LoadPuzzleSetsFromInternet.createIntent(mSessionKey);
            }
        };
        session.update(new IListenerVoid() {
            @Override
            public void handle() {

            }
        });
    }

    private abstract class LoadCrosswordFromInternetSession extends ModelUpdater<DbService.DbTaskEnv>
    {
        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<DbService.DbTaskEnv>> getServiceClass()
        {
            return DbService.class;
        }

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<DbService.DbTaskEnv>> getTaskClass() {
            return LoadPuzzleSetsFromInternet.class;
        }

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null){
                return;
            }
        }
    }

}
