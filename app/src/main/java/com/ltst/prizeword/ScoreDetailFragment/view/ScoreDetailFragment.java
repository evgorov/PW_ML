package com.ltst.prizeword.ScoreDetailFragment.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class ScoreDetailFragment extends SherlockFragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.scoreDetailFragment.ScoreDetailFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = ScoreDetailFragment.class.getName();

    private @Nonnull ListView mScoreListVeiw;

    private @Nonnull android.content.Context mContext;

    @Override public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        super.onAttach(activity);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.detail_score_fragment_layout, container, false);
        mScoreListVeiw = (ListView) v.findViewById(R.id.score_listview);
        return v;
    }
    @Override
    public void onResume()
    {
        super.onResume();
    }

}
