package com.ltst.prizeword.crossword.view;

import com.ltst.prizeword.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.navigation.INavigationDrawerHolder;

import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListenerVoid;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrosswordsFragment extends SherlockFragment
                                implements View.OnClickListener
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.crossword.view.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    private @Nonnull Context mContext;
//    private @Nonnull Button mCrossWordButton;

    private static final int BADGE_ID = 1;

    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull INavigationDrawerHolder mINavigationDrawerHolder;
    private @Nonnull String mSessionKey;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;

    private @Nonnull LinearLayout mViewCurrentContainer;
    private @Nonnull LinearLayout mViewArchiveContainer;

    private @Nonnull Button mMenuBackButton;

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
        mViewCurrentContainer = (LinearLayout) v.findViewById(R.id.crossword_fragment_current_container);
        mViewArchiveContainer = (LinearLayout) v.findViewById(R.id.crossword_fragment_archive_container);
        mMenuBackButton = (Button) v.findViewById(R.id.crossword_fragment_header_menu_btn);
        mMenuBackButton.setOnClickListener(this);

        LinearLayout viewCurrentBrilliant = (LinearLayout) inflater.inflate(R.layout.crossword_current_brilliant, null, false);
        LinearLayout viewCurrentFree = (LinearLayout) inflater.inflate(R.layout.crossword_current_free, null, false);

        LinearLayout viewArchiveGold = (LinearLayout) inflater.inflate(R.layout.crossword_archive_gold, null, false);
        LinearLayout viewArchiveSilver = (LinearLayout) inflater.inflate(R.layout.crossword_archive_silver, null, false);

        ((LinearLayout) viewCurrentFree.findViewById(R.id.crossword_current_free_buy_panel)).setVisibility(View.GONE);
        ((LinearLayout) viewArchiveGold.findViewById(R.id.crossword_archive_gold_splitter)).setVisibility(View.GONE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.crossword_number_free_1);
        ImageView iv = new ImageView(mContext);
        iv.setImageBitmap(bitmap);
//        bitmap.recycle();

        View badgeCurrentFree1 = inflater.inflate(R.layout.crossword_badge_free_unresolved, null, false);
        badgeCurrentFree1.setId(BADGE_ID);
        badgeCurrentFree1.setOnClickListener(this);

        ((LinearLayout)badgeCurrentFree1.findViewById(R.id.crossword_badge_unresolved_free_number_container)).addView(iv);

        viewCurrentFree.addView(badgeCurrentFree1);

        mViewCurrentContainer.addView(viewCurrentBrilliant);
        mViewCurrentContainer.addView(viewCurrentFree);

        mViewArchiveContainer.addView(viewArchiveGold);
        mViewArchiveContainer.addView(viewArchiveSilver);

        return v;
    }

    @Override
    public void onResume()
    {
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mPuzzleSetModel = new PuzzleSetModel(mBcConnector, mSessionKey);
        mPuzzleSetModel.updateDataByInternet(updateHandler);
        mPuzzleSetModel.updateDataByDb(updateHandler);
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
            @Nonnull Intent intent = OneCrosswordActivity.createIntent(mContext, freeSet);
            mContext.startActivity(intent);
        }
    }

    private IListenerVoid updateHandler = new IListenerVoid()
    {
        @Override
        public void handle()
        {

        }
    };
}
