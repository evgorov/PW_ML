package com.ltst.prizeword.crossword.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public class CrosswordFragmentHolder {

    public class CurrentCrosswordPanel{

        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CurrentCrosswordPanel(@Nonnull View view){

            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
        }

    }

    public class ArchiveCrosswordPanel{
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public ArchiveCrosswordPanel(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    public class BuyCrosswordPanel{

        @Nonnull public TextView mRestHintsTV;

        @Nonnull public TextView mBuy10TV;
        @Nonnull public TextView mBuy20TV;
        @Nonnull public TextView mBuy30TV;

        @Nonnull public LinearLayout mBuy10Button;
        @Nonnull public LinearLayout mBuy20Button;
        @Nonnull public LinearLayout mBuy30Button;

        public BuyCrosswordPanel(@Nonnull View view){
            mRestHintsTV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_count);
            mBuy10TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_10_price);
            mBuy20TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_20_price);
            mBuy30TV = (TextView) view.findViewById(R.id.crossword_fragment_current_rest_buy_30_price);
            mBuy10Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_10_btn);
            mBuy20Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_20_btn);
            mBuy30Button = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_rest_buy_30_btn);
        }
    }

    // ================== ARCHIVE CROSSWORD PANEL ITEM ======================

    public class ArrchivePanelItemBrilliant extends ArchivePanelItemAbstract{

        public ArrchivePanelItemBrilliant(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_fg);
            mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_badges_container);
            mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_splitter);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_brilliant_switcher);
        }
    }

    public class ArrchivePanelItemGold extends ArchivePanelItemAbstract{

        public ArrchivePanelItemGold(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_gold_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_fg);
            mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_badges_container);
            mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_splitter);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_gold_switcher);
        }
    }

    public class ArrchivePanelItemSilver extends ArchivePanelItemAbstract{

        public ArrchivePanelItemSilver(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_fg);
            mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_badges_container);
            mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_splitter);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver_switcher);
        }
    }

    public class ArrchivePanelItemSilver2 extends ArchivePanelItemAbstract{

        public ArrchivePanelItemSilver2(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_fg);
            mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_badges_container);
            mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_splitter);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver2_switcher);
        }
    }

    public class ArrchivePanelItemFree extends ArchivePanelItemAbstract{

        public ArrchivePanelItemFree(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_free_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_fg);
            mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_badges_container);
            mSplitterLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_splitter);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_free_switcher);
        }
    }

    // ================== CURRENT CROSSWORD PANEL ITEM ======================

    public class CurrentPanelItemBrilliant extends CurrentPanelItemAbstract{

        public CurrentPanelItemBrilliant(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_buy_price);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_fg);
        }
    }

    public class CurrentPanelItemGold extends CurrentPanelItemAbstract{

        public CurrentPanelItemGold(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_gold_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_gold_buy_price);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_gold_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_gold_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_gold_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_gold_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_gold_progress_fg);
        }
    }

    public class CurrentPanelItemSilver extends CurrentPanelItemAbstract{

        public CurrentPanelItemSilver(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_silver_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_silver_buy_price);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver_progress_fg);
        }
    }

    public class CurrentPanelItemSilver2 extends CurrentPanelItemAbstract{

        public CurrentPanelItemSilver2(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_silver2_buy_price);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver2_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_silver2_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_silver2_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_silver2_progress_fg);
        }
    }

    public class CurrentPanelItemFree extends CurrentPanelItemAbstract{

        public CurrentPanelItemFree(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_current_free_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_current_free_buy_price);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_free_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_free_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_free_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_free_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_free_progress_fg);
        }
    }

    // ================== BADGE RESOLVE ======================

    public class ReloveBadgeItemBrilliant extends ReloveBadgeItemAbstract{

        public ReloveBadgeItemBrilliant(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_brilliant_score);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_brilliant_number_container);
        }
    }

    public class ReloveBadgeItemGold extends ReloveBadgeItemAbstract{

        public ReloveBadgeItemGold(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_gold_score);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_gold_number_container);
        }
    }

    public class ReloveBadgeItemSilver extends ReloveBadgeItemAbstract{

        public ReloveBadgeItemSilver(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_silver_score);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_silver_number_container);
        }
    }

    public class ReloveBadgeItemFree extends ReloveBadgeItemAbstract{

        public ReloveBadgeItemFree(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_free_score);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_free_number_container);
        }
    }

    // ================== BADGE UNRESOLVE ======================

    public class UnreloveBadgeItemBrilliant extends UnreloveBadgeItemAbstract{

        public UnreloveBadgeItemBrilliant(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_brilliant_percent);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_number_container);
            mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_bg);
            mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_fg);
        }
    }

    public class UnreloveBadgeItemGold extends UnreloveBadgeItemAbstract{

        public UnreloveBadgeItemGold(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_gold_percent);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_number_container);
            mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_bg);
            mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_fg);
        }
    }

    public class UnreloveBadgeItemSilver extends UnreloveBadgeItemAbstract{

        public UnreloveBadgeItemSilver(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_silver_percent);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_number_container);
            mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_bg);
            mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_fg);
        }
    }

    public class UnreloveBadgeItemFree extends UnreloveBadgeItemAbstract{

        public UnreloveBadgeItemFree(@Nonnull LayoutInflater inflater, int id){

            @Nonnull View view = inflater.inflate(id, null, false);
            mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_free_percent);
            mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_number_container);
            mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_bg);
            mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_fg);
        }
    }

    // ================== ABSTRACT CROSSWORD PANELS ITEM ======================


    public class ArchivePanelItemAbstract{
        @Nonnull public TextView mRatioResolvedTV;
        @Nonnull public TextView mPercentResolvedTV;
        @Nonnull public TextView mTotalScoreTV;
        @Nonnull public LinearLayout mProgressBackgroudLL;
        @Nonnull public LinearLayout mProgressForegroudLL;

        @Nonnull public LinearLayout mCrosswordContainerLL;

        @Nonnull public LinearLayout mSplitterLL;
        @Nonnull public ToggleButton mSwitchToogleButton;
    }

    public class CurrentPanelItemAbstract{
        @Nonnull public TextView mCountCrosswordsTV;
        @Nonnull public TextView mCountScoreTV;
        @Nonnull public LinearLayout mBuyButton;
        @Nonnull public TextView mBuyPriceTV;
        @Nonnull public TextView mRatioResolvedTV;
        @Nonnull public TextView mPercentResolvedTV;
        @Nonnull public TextView mTotalScoreTV;
        @Nonnull public LinearLayout mProgressBackgroudLL;
        @Nonnull public LinearLayout mProgressForegroudLL;
    }

    public class ReloveBadgeItemAbstract{
        @Nonnull public LinearLayout mBitmapLL;
        @Nonnull public TextView mScoreTV;
    }

    public class UnreloveBadgeItemAbstract{
        @Nonnull public TextView mPercentTV;
        @Nonnull public LinearLayout mBitmapLL;
        @Nonnull public LinearLayout mProgressBGLL;
        @Nonnull public LinearLayout mProgressFGLL;
    }

}