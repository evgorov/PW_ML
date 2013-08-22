package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 15.08.13.
 */
public class BadgeHolder {

    @Nonnull View mRootView;
    @Nonnull LinearLayout mBackground;
    @Nonnull LinearLayout mForegroud;
    @Nonnull LinearLayout mNumber;
    @Nonnull TextView mScore;
    @Nonnull TextView mPercent;
    @Nonnull SeekBar mProgressSeekBar;

    @Nonnull LinearLayout mUnresolverContainer;
    @Nonnull LinearLayout mResolverContainer;


    public BadgeHolder( @Nonnull Context context, @Nullable View view ) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = (view == null) ? inflater.inflate(R.layout.crossword_badge, null, false) : view;
        mBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_badge_bg);
        mForegroud = (LinearLayout) mRootView.findViewById(R.id.crossword_badge_fg);
        mNumber = (LinearLayout) mRootView.findViewById(R.id.crossword_badge_number);
        mProgressSeekBar = (SeekBar) mRootView.findViewById(R.id.crossword_badge_progressbar);
        mProgressSeekBar.setEnabled(false);
        mScore = (TextView) mRootView.findViewById(R.id.crossword_badge_score);
        mPercent = (TextView) mRootView.findViewById(R.id.crossword_badge_rercent);

        mUnresolverContainer = (LinearLayout) mRootView.findViewById(R.id.crossword_badge_unresolved_container);
        mResolverContainer = (LinearLayout) mRootView.findViewById(R.id.crossword_badge_resolved_container);
    }
}
