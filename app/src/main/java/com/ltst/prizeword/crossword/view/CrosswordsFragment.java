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

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.app.IBcConnectorOwner;
import com.ltst.prizeword.app.SharedPreferencesValues;
import com.ltst.prizeword.crossword.model.IPuzzleSetModel;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

public class CrosswordsFragment extends SherlockFragment
                                implements View.OnClickListener
{
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.crossword.view.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    private @Nonnull Context mContext;
    private @Nonnull Button mCrossWordButton;
    private @Nonnull IBcConnector mBcConnector;
    private @Nonnull String mSessionKey;
    private @Nonnull IPuzzleSetModel mPuzzleSetModel;

    // ==== Livecycle =================================

    @Override
    public void onAttach(Activity activity)
    {
        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner)activity).getBcConnector();
        mSessionKey = SharedPreferencesValues.getSessionKey(mContext);
        mPuzzleSetModel = new PuzzleSetModel(mBcConnector, mSessionKey);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.crossword_fragment_layout, container, false);
        mCrossWordButton = (Button) v.findViewById(R.id.view_crossword);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        mCrossWordButton.setOnClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    // ==== Events =================================

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.view_crossword:
                launchCrosswordActivity();
                break;
            default:
                break;
        }
    }

    // =============================================

    private void launchCrosswordActivity()
    {
        @Nonnull Intent intent = new Intent(mContext, OneCrosswordActivity.class);
        mContext.startActivity(intent);
    }

}
