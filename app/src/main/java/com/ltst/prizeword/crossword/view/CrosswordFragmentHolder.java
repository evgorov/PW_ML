package com.ltst.prizeword.crossword.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public class CrosswordFragmentHolder {

    final static int BADGE_RESOLVE_BRILLIANT = 1;
    final static int BADGE_RESOLVE_GOLD = 2;
    final static int BADGE_RESOLVE_SILVER = 3;
    final static int BADGE_RESOLVE_SILVER2 = 4;
    final static int BADGE_RESOLVE_FREE = 5;

    final static int BADGE_UNRESOLVE_BRILLIANT = 6;
    final static int BADGE_UNRESOLVE_GOLD = 7;
    final static int BADGE_UNRESOLVE_SILVER = 8;
    final static int BADGE_UNRESOLVE_SILVER2 = 9;
    final static int BADGE_UNRESOLVE_FREE = 10;

    final static int PANEL_CURRENT_BRILLIANT = 11;
    final static int PANEL_CURRENT_GOLD = 12;
    final static int PANEL_CURRENT_SILVER = 13;
    final static int PANEL_CURRENT_SILVER2 = 14;
    final static int PANEL_CURRENT_FREE = 15;

    final static int PANEL_ARCHIVE_BRILLIANT = 16;
    final static int PANEL_ARCHIVE_GOLD = 17;
    final static int PANEL_ARCHIVE_SILVER = 18;
    final static int PANEL_ARCHIVE_SILVER2 = 19;
    final static int PANEL_ARCHIVE_FREE = 20;

    private @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull View mViewCrossword;
    private @Nonnull CrosswordPanelCurrent mCrosswordPanelCurrent;
    private @Nonnull CrosswordPanelArchive mCrosswordPanelArchive;
    private @Nonnull CrosswordPanelBuy mCrosswordPanelBuy;

    public CrosswordFragmentHolder(@Nonnull SherlockFragment fragment, @Nonnull LayoutInflater inflater, @Nonnull View view) {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;

        mCrosswordPanelCurrent = new CrosswordPanelCurrent(view);
        mCrosswordPanelArchive = new CrosswordPanelArchive(view);
        mCrosswordPanelBuy = new CrosswordPanelBuy(view);
    }

    // ================== CROSSWORD PANELS ======================

    static public class CrosswordPanelCurrent {

        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelCurrent(@Nonnull View view){

            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
        }
    }

    static public class CrosswordPanelArchive {
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelArchive(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    static public class CrosswordPanelBuy {

        @Nonnull public TextView mRestHintsTV;

        @Nonnull public TextView mBuy10TV;
        @Nonnull public TextView mBuy20TV;
        @Nonnull public TextView mBuy30TV;

        @Nonnull public LinearLayout mBuy10Button;
        @Nonnull public LinearLayout mBuy20Button;
        @Nonnull public LinearLayout mBuy30Button;

        public CrosswordPanelBuy(@Nonnull View view){
            mRestHintsTV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_count);
            mBuy10TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_10_price);
            mBuy20TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_20_price);
            mBuy30TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_30_price);
            mBuy10Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
            mBuy20Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
            mBuy30Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);
        }
    }

    // ================== CROSSWORD PANELS ITEM ======================


    static public class CrosswordElementArchive{
        @Nonnull static public TextView mRatioResolvedTV;
        @Nonnull static public TextView mPercentResolvedTV;
        @Nonnull static public TextView mTotalScoreTV;
        @Nonnull static public LinearLayout mProgressBackgroudLL;
        @Nonnull static public LinearLayout mProgressForegroudLL;

        @Nonnull static public LinearLayout mCrosswordContainerLL;

        @Nonnull static public LinearLayout mSplitterLL;
        @Nonnull static public ToggleButton mSwitchToogleButton;

        static View addView(final @Nonnull LayoutInflater inflater, final int view_id)
        {
            @Nonnull View view = null;

            switch (view_id){
                case PANEL_ARCHIVE_BRILLIANT:{
                    view =  inflater.inflate(R.layout.crossword_archive_brilliant, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_badges_container);
                    mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_splitter);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_brilliant_switcher);
                }break;
                case PANEL_ARCHIVE_GOLD:{
                    view = inflater.inflate(R.layout.crossword_archive_gold, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_gold_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_badges_container);
                    mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_splitter);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_gold_switcher);
                }break;
                case PANEL_ARCHIVE_SILVER:{
                    view = inflater.inflate(R.layout.crossword_archive_silver, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_badges_container);
                    mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_splitter);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver_switcher);
                }break;
                case PANEL_ARCHIVE_SILVER2:{
                    view = inflater.inflate(R.layout.crossword_archive_silver2, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_badges_container);
                    mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_splitter);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver2_switcher);
                }break;
                case PANEL_ARCHIVE_FREE:{
                    view = inflater.inflate(R.layout.crossword_archive_free, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_free_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_badges_container);
                    mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_splitter);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_free_switcher);
                }break;

                default: break;
            }
            return view;
        }
    }

    // ================== CROSSWORD PANELS ELEMENTS ======================

    static public class CrosswordElementCurrent{
        @Nonnull static public TextView mCountCrosswordsTV;
        @Nonnull static public TextView mCountScoreTV;
        @Nonnull static public LinearLayout mBuyButton;
        @Nonnull static public TextView mBuyPriceTV;
        @Nonnull static public TextView mRatioResolvedTV;
        @Nonnull static public TextView mPercentResolvedTV;
        @Nonnull static public TextView mTotalScoreTV;
        @Nonnull static public LinearLayout mProgressBackgroudLL;
        @Nonnull static public LinearLayout mProgressForegroudLL;

        static View addView(final @Nonnull LayoutInflater inflater, final int view_id)
        {
            @Nonnull View view = null;
            switch (view_id){
                case PANEL_CURRENT_BRILLIANT:{
                    view = inflater.inflate(R.layout.crossword_current_brilliant, null, false);
                    mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_count);
                    mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_count);
                    mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_buy_button);
                    mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_price);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_fg);
                }break;
                case PANEL_CURRENT_GOLD:{
                    view = inflater.inflate(R.layout.crossword_current_gold, null, false);
                    mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_count);
                    mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_count);
                    mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_gold_buy_button);
                    mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_price);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_gold_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_gold_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_gold_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_gold_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_gold_progress_fg);
                }break;
                case PANEL_CURRENT_SILVER:{
                    view = inflater.inflate(R.layout.crossword_current_silver, null, false);
                    mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_count);
                    mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_count);
                    mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_silver_buy_button);
                    mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_price);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver_progress_fg);
                }break;
                case PANEL_CURRENT_SILVER2:{
                    view = inflater.inflate(R.layout.crossword_current_silver2, null, false);
                    mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_count);
                    mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_count);
                    mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_buy_button);
                    mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_price);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver2_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver2_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver2_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_progress_fg);
                }break;
                case PANEL_CURRENT_FREE:{
                    view = inflater.inflate(R.layout.crossword_current_free, null, false);
                    mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_count);
                    mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_count);
                    mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_free_buy_button);
                    mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_price);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_free_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_free_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_free_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_free_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_free_progress_fg);
                }break;
                default: break;
            }
            return view;
        }
    }

    // ================== CROSSWORD BADGES ======================

    static public class BadgeResolved{
        @Nonnull static public LinearLayout mBitmapLL;
        @Nonnull static public TextView mScoreTV;

        static View addView(final @Nonnull LayoutInflater inflater, final int view_id)
        {        @Nonnull View view = null;
            switch (view_id){
                case BADGE_RESOLVE_BRILLIANT:{
                    view = inflater.inflate(view_id, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_brilliant_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_brilliant_number_container);
                }break;
                case BADGE_RESOLVE_GOLD:{
                    view = inflater.inflate(view_id, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_gold_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_gold_number_container);
                }break;
                case BADGE_RESOLVE_SILVER:
                case BADGE_RESOLVE_SILVER2:{
                    view = inflater.inflate(view_id, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_silver_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_silver_number_container);
                }break;
                case BADGE_RESOLVE_FREE:{
                    view = inflater.inflate(view_id, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_free_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_free_number_container);
                }break;
                default: break;
            }
            return view;
        }
    }

    static public class BadgeUnresolved{
        @Nonnull static public TextView mPercentTV;
        @Nonnull static public LinearLayout mBitmapLL;
        @Nonnull static public LinearLayout mProgressBGLL;
        @Nonnull static public LinearLayout mProgressFGLL;

        static View addView(final @Nonnull LayoutInflater inflater, final int view_id)
        {        @Nonnull View view = null;
            switch (view_id){
                case BADGE_UNRESOLVE_BRILLIANT:{
                    view = inflater.inflate(view_id, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_brilliant_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_fg);
                }break;
                case BADGE_UNRESOLVE_GOLD:{
                    view = inflater.inflate(view_id, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_gold_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_fg);
                }break;
                case BADGE_UNRESOLVE_SILVER:
                case BADGE_UNRESOLVE_SILVER2:{
                    view = inflater.inflate(view_id, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_silver_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_fg);
                }break;
                case BADGE_UNRESOLVE_FREE:{
                    view = inflater.inflate(view_id, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_free_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_fg);
                }break;
                default: break;
            }
            return view;
        }
    }

}