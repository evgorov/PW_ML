package com.ltst.przwrd.crossword.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.IBcConnectorOwner;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.model.HintsModel;
import com.ltst.przwrd.crossword.model.IPuzzleSetModel;
import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.manadges.IManadges;
import com.ltst.przwrd.navigation.IFragmentsHolderActivity;
import com.ltst.przwrd.navigation.INavigationDrawerHolder;
import com.ltst.przwrd.navigation.NavigationActivity;
import com.ltst.przwrd.news.INewsModel;
import com.ltst.przwrd.news.NewsModel;
import com.ltst.przwrd.score.Coefficients;
import com.ltst.przwrd.score.CoefficientsModel;
import com.ltst.przwrd.sounds.SoundsWork;
import com.ltst.przwrd.swipe.ITouchInterface;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.constants.Strings;
import org.omich.velo.handlers.IListenerVoid;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrosswordsFragment extends SherlockFragment
        implements View.OnClickListener,
        ICrosswordFragment,
        ITouchInterface
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.crossword.mRootView.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    public static final @Nonnull String BF_SETS = FRAGMENT_ID + ".allSets";
    public static final @Nonnull String BF_PUZZLES = FRAGMENT_ID + ".allPuzzles";
    public static final @Nonnull String BF_COEFFICIENTS = FRAGMENT_ID + ".coefficients";
    public static final @Nonnull String BF_HINTS_COUNT = FRAGMENT_ID + ".hintsCount";

    private static final int REQUEST_ANSWER_CROSSWORD_SET_ID = 500;

    private @Nonnull Context mContext;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;
    private @Nonnull com.ltst.przwrd.navigation.IFragmentsHolderActivity mIFragmentActivity;
    private @Nonnull String mSessionKey;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;
    private @Nonnull HintsManager mHintsManager;
    private @Nonnull CoefficientsModel mCoefficientsModel;

    private @Nonnull View mProgressbarSync;
    private @Nonnull View mProgressBar;
    private @Nonnull Button mMenuBackButton;
    private @Nonnull NewsHolder mNewsHolder;
    private @Nonnull CrosswordFragmentHolder mCrosswordFragmentHolder;

    private @Nonnull INewsModel mNewsModel;
    private @Nonnull HintsModel mHintsModel;
    private @Nonnull IManadges mIManadges;

    private boolean flgOneUpload;

    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        flgOneUpload = false;
        mContext = (Context) activity;
        mINavigationDrawerHolder = (INavigationDrawerHolder) activity;
        mIFragmentActivity = (IFragmentsHolderActivity) activity;
        mIManadges = (IManadges) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.crossword_fragment_layout, container, false);

        mMenuBackButton = (Button) v.findViewById(R.id.crossword_fragment_header_menu_btn);
        mMenuBackButton.setOnClickListener(this);
        if(mIFragmentActivity.getIsTablet())
        {
            mMenuBackButton.setVisibility(View.GONE);
        }

        mNewsHolder = new NewsHolder(mContext, this, inflater, v);
        mCrosswordFragmentHolder = new CrosswordFragmentHolder(mContext, this, inflater, v);
        mHintsManager = new HintsManager(mContext, this, v);

        mProgressBar =  v.findViewById(R.id.archive_progressBar);
        mProgressbarSync = v.findViewById(R.id.crossword_progressbar_sync);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case REQUEST_ANSWER_CROSSWORD_SET_ID:
                {
                    if(data.hasExtra(OneCrosswordActivity.BF_PUZZLE_SET))
                    {
                        @Nonnull String puzzleSetServerId = data.getStringExtra(OneCrosswordActivity.BF_PUZZLE_SET);
                        if(puzzleSetServerId !=null && puzzleSetServerId != Strings.EMPTY)
                        {
                            CrosswordSetUpdateMember.mPuzzleSetServerId = puzzleSetServerId;
                            CrosswordSetUpdateMember.mNeedUpdate = true;
                        }
                    }
                }break;
                default:break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart()
    {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressbarSync.setVisibility(View.GONE);
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();

        mCoefficientsModel = new CoefficientsModel(mSessionKey, mBcConnector);
        mPuzzleSetModel = new PuzzleSetModel(mContext, mBcConnector, mSessionKey);
        mNewsModel = new NewsModel(mSessionKey, mBcConnector);
        mHintsModel = new HintsModel(mBcConnector, mSessionKey);

        mPuzzleSetModel.updateHints(handlerUpdateHints);

        if(!flgOneUpload)
        {
            mNewsModel.updateFromInternet(mRefreshHandler);
            mCoefficientsModel.updateFromInternet(handlerUpdatePuzzleSet);
            if(mCrosswordFragmentHolder.getMapSets().size() == 0
                    || mCrosswordFragmentHolder.getMapPuzzles().size() == 0)
            {
//                mPuzzleSetModel.updateCurrentSets(handlerUpdateCurrentSets);
                mPuzzleSetModel.updateTotalDataByDb(handlerUpdateSetsFromDB);
//                flgOneUpload = true;
            }
            else
            {
                createCrosswordPanel();
                skipProgressBar();
            }
        }
        else
        {
            if(CrosswordSetUpdateMember.mNeedUpdate)
            {
                CrosswordSetUpdateMember.mNeedUpdate = false;
                updateOneSet(CrosswordSetUpdateMember.mPuzzleSetServerId);
            }
            else
            {
                skipProgressBar();
            }
        }

        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onStop()
    {
        mCoefficientsModel.close();
        mPuzzleSetModel.close();
        mNewsModel.close();
        mHintsModel.close();
        NavigationActivity.debug("stop");
        super.onStop();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putInt(BF_HINTS_COUNT, mHintsManager.getHintsCount());
        outState.putSerializable(BF_SETS, mCrosswordFragmentHolder.getMapSets());
        outState.putSerializable(BF_PUZZLES, mCrosswordFragmentHolder.getMapPuzzles());
        outState.putParcelable(BF_COEFFICIENTS, mCrosswordFragmentHolder.getCoefficients());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            mHintsManager.setHintsCount(savedInstanceState.getInt(BF_HINTS_COUNT));
            mCrosswordFragmentHolder.setMapPuzzles((HashMap<String, List<Puzzle>>) savedInstanceState.getSerializable(BF_PUZZLES));
            mCrosswordFragmentHolder.setMapSets((HashMap<String, List<PuzzleSet>>) savedInstanceState.getSerializable(BF_SETS));
            mCrosswordFragmentHolder.setCoefficients((Coefficients) savedInstanceState.getParcelable(BF_COEFFICIENTS));
        }
        super.onViewStateRestored(savedInstanceState);
    }

    public void skipProgressBar()
    {
        NavigationActivity.debug("skip");
        mProgressBar.setVisibility(View.GONE);
        if(!flgOneUpload)
        {
            NavigationActivity.debug("request inventory");
            mIManadges.getManadgeHolder().reloadInventory(new IListenerVoid(){
                @Override
                public void handle() {
                    NavigationActivity.debug("start sync");
                    mProgressbarSync.setVisibility(View.VISIBLE);
                    mPuzzleSetModel.updateSync(handlerSync);
                }
            });
        }
        else
        {
            NavigationActivity.debug("start sync");
            mProgressbarSync.setVisibility(View.VISIBLE);
            mPuzzleSetModel.updateSync(handlerSync);
        }
        flgOneUpload = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
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
            default:
                break;
        }
    }

    // =============================================


    private void createCrosswordPanel()
    {
        @Nullable List<PuzzleSet> sets = mPuzzleSetModel.getPuzzleSets();
        @Nullable HashMap<String, List<Puzzle>> mapPuzzles = mPuzzleSetModel.getPuzzlesSet();
        if(sets != null && mapPuzzles != null)
        {
            mCrosswordFragmentHolder.fillSet(sets, mapPuzzles);
        }
    }

        private IListenerVoid handlerUpdateSetsFromDB = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            createCrosswordPanel();

            if(mCrosswordFragmentHolder.isNeedUploadAllPuzzlrSetsFromInternet())
            {
                mPuzzleSetModel.updateTotalDataByInternet(handlerUpdateSetsFromServer);
            }
            else if(mCrosswordFragmentHolder.isNeedUploadCurrentPuzzlrSetsFromInternet())
            {
                mPuzzleSetModel.updateCurrentSets(handlerUpdateCurrentSets);
            }
            else
            {
                mPuzzleSetModel.synchronizePuzzleUserData();
                skipProgressBar();
            }
        }
    };

    private IListenerVoid handlerUpdateSetsFromServer = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            createCrosswordPanel();
            skipProgressBar();
        }
    };

    private IListenerVoid handlerUpdatePuzzleSet = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            mCrosswordFragmentHolder.setCoefficients(mCoefficientsModel.getCoefficients());
        }
    };

    private IListenerVoid handlerUpdateHints = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            int hintsCount = mPuzzleSetModel.getHintsCount();
            mHintsManager.setHintsCount(hintsCount);
        }
    };

    private IListenerVoid handlerUpdateCurrentSets = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            createCrosswordPanel();
            skipProgressBar();
        }
    };

    private IListenerVoid handlerSync = new IListenerVoid()
    {
        @Override
        public void handle()
        {
            NavigationActivity.debug("end sync!");
            mProgressbarSync.setVisibility(View.GONE);
        }
    };

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
                            @Nonnull Intent intent = OneCrosswordActivity.
                                    createIntent(mContext, puzzleSet, puzzle.serverId,
                                            mHintsManager.getHintsCount(),
                                            mIFragmentActivity.getVkSwitch(), mIFragmentActivity.getFbSwitch());
                            startActivityForResult(intent, REQUEST_ANSWER_CROSSWORD_SET_ID);
                            return;
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void purchaseResult(boolean result) {
        if(result)
        {
            updateCurrentSet();
        }
        else
        {
            skipProgressBar();
        }
    }

    public void updateCurrentSet() {
//        mProgressBar.setVisibility(View.VISIBLE);
        mPuzzleSetModel.updateTotalDataByDb(new IListenerVoid() {
            @Override
            public void handle() {
                createCrosswordPanel();
                skipProgressBar();
            }
        });
    }

    @Override
    public void updateOneSet(@Nonnull String puzzleSetServerId) {
        if(mPuzzleSetModel == null)
            return;
        mProgressBar.setVisibility(View.VISIBLE);
        mPuzzleSetModel.updateOneSet(CrosswordSetUpdateMember.mPuzzleSetServerId, new IListenerVoid() {
            @Override
            public void handle() {
                createCrosswordPanel();
                skipProgressBar();
            }
        });
    }

    @Override
    public HintsModel getHintsModel() {
        return mHintsModel;
    }

    @Override
    public IPuzzleSetModel getPuzzleSetModel() {
        return mPuzzleSetModel;
    }

    @Override
    public void waitLoader(boolean wait) {
        if(wait)
        {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            skipProgressBar();
        }
    }

    private final @Nonnull IListenerVoid mRefreshHandler = new IListenerVoid()
    {
        @Override public void handle()
        {
            mNewsHolder.fillNews(mNewsModel.getNews());
        }
    };

    @Override
    public void notifySwipe(SwipeMethod swipe) {
        mNewsHolder.notifySwipe(swipe);
    }

    private static class CrosswordSetUpdateMember
    {
        static @Nonnull String mPuzzleSetServerId;
        static boolean mNeedUpdate;
    }

}