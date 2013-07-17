package com.ltst.prizeword.crossword.view;

import android.os.Bundle;

import android.support.v7.widget.GridLayout;

import com.actionbarsherlock.app.SherlockActivity;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

public class OneCrosswordActivity extends SherlockActivity
{
    private @Nonnull CrosswordBackgroundView mCrosswordBgImage;
    private @Nonnull GridLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_crossword);
        mCrosswordBgImage = (CrosswordBackgroundView) findViewById(R.id.one_crossword_view);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
