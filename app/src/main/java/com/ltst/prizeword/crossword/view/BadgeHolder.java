package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
    @Nonnull LinearLayout mProgressBackgroud;
    @Nonnull LinearLayout mProgressForegroud;

    @Nonnull LinearLayout mUnresolverContainer;
    @Nonnull LinearLayout mResolverContainer;


    public BadgeHolder( @Nonnull Context context, @Nullable View view ) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = (view == null) ? inflater.inflate(R.layout.crossword_badge, null, false) : view;
        mBackground = (LinearLayout) inflater.inflate(R.id.crossword_badge_bg, null, false);
        mForegroud = (LinearLayout) inflater.inflate(R.id.crossword_badge_fg, null, false);
        mNumber = (LinearLayout) inflater.inflate(R.id.crossword_badge_number, null, false);
        mProgressBackgroud = (LinearLayout) inflater.inflate(R.id.crossword_badge_progress_bg, null, false);
        mProgressForegroud = (LinearLayout) inflater.inflate(R.id.crossword_badge_progress_fg, null, false);
        mScore = (TextView) inflater.inflate(R.id.crossword_badge_score, null, false);
        mPercent = (TextView) inflater.inflate(R.id.crossword_badge_rercent, null, false);

        mUnresolverContainer = (LinearLayout) inflater.inflate(R.id.crossword_badge_unresolved_container, null, false);
        mResolverContainer = (LinearLayout) inflater.inflate(R.id.crossword_badge_resolved_container, null, false);
    }
}
