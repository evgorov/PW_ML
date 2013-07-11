package com.ltst.prizeword.crossword;

import com.ltst.prizeword.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import javax.annotation.Nonnull;

/**
 * Created by naghtarr on 7/11/13.
 */
public class CrosswordsFragment extends SherlockFragment
{
    public static final @Nonnull String FRAGMENT_ID = "com.ltst.prizeword.crossword.CrosswordsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = CrosswordsFragment.class.getName();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.crossword_fragment_layout, container, false);
        return v;
    }
}
