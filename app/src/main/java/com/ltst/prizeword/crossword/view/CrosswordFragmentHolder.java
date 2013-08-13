package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;

import java.util.HashMap;

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

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull View mViewCrossword;
    private static @Nonnull Context mContext;
    public @Nonnull CrosswordPanelCurrent mCrosswordPanelCurrent;
    public @Nonnull CrosswordPanelArchive mCrosswordPanelArchive;
    public @Nonnull CrosswordPanelBuy mCrosswordPanelBuy;

    private static @Nonnull HashMap<Integer,Integer> mapBadgeNumbersBrilliant = new HashMap<Integer, Integer>();
    private static @Nonnull HashMap<Integer,Integer> mapBadgeNumbersGold = new HashMap<Integer, Integer>();
    private static @Nonnull HashMap<Integer,Integer> mapBadgeNumbersSilver = new HashMap<Integer, Integer>();
    private static @Nonnull HashMap<Integer,Integer> mapBadgeNumbersFree = new HashMap<Integer, Integer>();


    public CrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mapBadgeNumbersBrilliant.put(1, R.drawable.crossword_number_brilliant_1);
        mapBadgeNumbersBrilliant.put(2, R.drawable.crossword_number_brilliant_2);
        mapBadgeNumbersBrilliant.put(3, R.drawable.crossword_number_brilliant_3);
        mapBadgeNumbersBrilliant.put(4, R.drawable.crossword_number_brilliant_4);
        mapBadgeNumbersBrilliant.put(5, R.drawable.crossword_number_brilliant_5);
        mapBadgeNumbersBrilliant.put(6, R.drawable.crossword_number_brilliant_6);
        mapBadgeNumbersBrilliant.put(7, R.drawable.crossword_number_brilliant_7);
        mapBadgeNumbersBrilliant.put(8, R.drawable.crossword_number_brilliant_8);
        mapBadgeNumbersBrilliant.put(9, R.drawable.crossword_number_brilliant_9);
        mapBadgeNumbersBrilliant.put(10, R.drawable.crossword_number_brilliant_10);
        mapBadgeNumbersBrilliant.put(11, R.drawable.crossword_number_brilliant_11);
        mapBadgeNumbersBrilliant.put(12, R.drawable.crossword_number_brilliant_12);
        mapBadgeNumbersBrilliant.put(13, R.drawable.crossword_number_brilliant_13);
        mapBadgeNumbersBrilliant.put(14, R.drawable.crossword_number_brilliant_14);
        mapBadgeNumbersBrilliant.put(15, R.drawable.crossword_number_brilliant_15);
        mapBadgeNumbersBrilliant.put(16, R.drawable.crossword_number_brilliant_16);
        mapBadgeNumbersBrilliant.put(17, R.drawable.crossword_number_brilliant_17);
        mapBadgeNumbersBrilliant.put(18, R.drawable.crossword_number_brilliant_18);
        mapBadgeNumbersBrilliant.put(19, R.drawable.crossword_number_brilliant_19);
        mapBadgeNumbersBrilliant.put(20, R.drawable.crossword_number_brilliant_20);
        mapBadgeNumbersBrilliant.put(21, R.drawable.crossword_number_brilliant_21);
        mapBadgeNumbersBrilliant.put(22, R.drawable.crossword_number_brilliant_22);
        mapBadgeNumbersBrilliant.put(23, R.drawable.crossword_number_brilliant_23);
        mapBadgeNumbersBrilliant.put(24, R.drawable.crossword_number_brilliant_24);
        mapBadgeNumbersBrilliant.put(25, R.drawable.crossword_number_brilliant_25);
        mapBadgeNumbersBrilliant.put(26, R.drawable.crossword_number_brilliant_26);
        mapBadgeNumbersBrilliant.put(27, R.drawable.crossword_number_brilliant_27);
        mapBadgeNumbersBrilliant.put(28, R.drawable.crossword_number_brilliant_28);
        mapBadgeNumbersBrilliant.put(29, R.drawable.crossword_number_brilliant_29);
        mapBadgeNumbersBrilliant.put(30, R.drawable.crossword_number_brilliant_30);
        mapBadgeNumbersBrilliant.put(31, R.drawable.crossword_number_brilliant_31);
        mapBadgeNumbersBrilliant.put(32, R.drawable.crossword_number_brilliant_32);

        mapBadgeNumbersGold.put(1, R.drawable.crossword_number_gold_1);
        mapBadgeNumbersGold.put(2, R.drawable.crossword_number_gold_2);
        mapBadgeNumbersGold.put(3, R.drawable.crossword_number_gold_3);
        mapBadgeNumbersGold.put(4, R.drawable.crossword_number_gold_4);
        mapBadgeNumbersGold.put(5, R.drawable.crossword_number_gold_5);
        mapBadgeNumbersGold.put(6, R.drawable.crossword_number_gold_6);
        mapBadgeNumbersGold.put(7, R.drawable.crossword_number_gold_7);
        mapBadgeNumbersGold.put(8, R.drawable.crossword_number_gold_8);
        mapBadgeNumbersGold.put(9, R.drawable.crossword_number_gold_9);
        mapBadgeNumbersGold.put(10, R.drawable.crossword_number_gold_10);
        mapBadgeNumbersGold.put(11, R.drawable.crossword_number_gold_11);
        mapBadgeNumbersGold.put(12, R.drawable.crossword_number_gold_12);
        mapBadgeNumbersGold.put(13, R.drawable.crossword_number_gold_13);
        mapBadgeNumbersGold.put(14, R.drawable.crossword_number_gold_14);
        mapBadgeNumbersGold.put(15, R.drawable.crossword_number_gold_15);
        mapBadgeNumbersGold.put(16, R.drawable.crossword_number_gold_16);
        mapBadgeNumbersGold.put(17, R.drawable.crossword_number_gold_17);
        mapBadgeNumbersGold.put(18, R.drawable.crossword_number_gold_18);
        mapBadgeNumbersGold.put(19, R.drawable.crossword_number_gold_19);
        mapBadgeNumbersGold.put(20, R.drawable.crossword_number_gold_20);
        mapBadgeNumbersGold.put(21, R.drawable.crossword_number_gold_21);
        mapBadgeNumbersGold.put(22, R.drawable.crossword_number_gold_22);
        mapBadgeNumbersGold.put(23, R.drawable.crossword_number_gold_23);
        mapBadgeNumbersGold.put(24, R.drawable.crossword_number_gold_24);
        mapBadgeNumbersGold.put(25, R.drawable.crossword_number_gold_25);
        mapBadgeNumbersGold.put(26, R.drawable.crossword_number_gold_26);
        mapBadgeNumbersGold.put(27, R.drawable.crossword_number_gold_27);
        mapBadgeNumbersGold.put(28, R.drawable.crossword_number_gold_28);
        mapBadgeNumbersGold.put(29, R.drawable.crossword_number_gold_29);
        mapBadgeNumbersGold.put(30, R.drawable.crossword_number_gold_30);
        mapBadgeNumbersGold.put(31, R.drawable.crossword_number_gold_31);
        mapBadgeNumbersGold.put(32, R.drawable.crossword_number_gold_32);

        mapBadgeNumbersSilver.put(1, R.drawable.crossword_number_silver_1);
        mapBadgeNumbersSilver.put(2, R.drawable.crossword_number_silver_2);
        mapBadgeNumbersSilver.put(3, R.drawable.crossword_number_silver_3);
        mapBadgeNumbersSilver.put(4, R.drawable.crossword_number_silver_4);
        mapBadgeNumbersSilver.put(5, R.drawable.crossword_number_silver_5);
        mapBadgeNumbersSilver.put(6, R.drawable.crossword_number_silver_6);
        mapBadgeNumbersSilver.put(7, R.drawable.crossword_number_silver_7);
        mapBadgeNumbersSilver.put(8, R.drawable.crossword_number_silver_8);
        mapBadgeNumbersSilver.put(9, R.drawable.crossword_number_silver_9);
        mapBadgeNumbersSilver.put(10, R.drawable.crossword_number_silver_10);
        mapBadgeNumbersSilver.put(11, R.drawable.crossword_number_silver_11);
        mapBadgeNumbersSilver.put(12, R.drawable.crossword_number_silver_12);
        mapBadgeNumbersSilver.put(13, R.drawable.crossword_number_silver_13);
        mapBadgeNumbersSilver.put(14, R.drawable.crossword_number_silver_14);
        mapBadgeNumbersSilver.put(15, R.drawable.crossword_number_silver_15);
        mapBadgeNumbersSilver.put(16, R.drawable.crossword_number_silver_16);
        mapBadgeNumbersSilver.put(17, R.drawable.crossword_number_silver_17);
        mapBadgeNumbersSilver.put(18, R.drawable.crossword_number_silver_18);
        mapBadgeNumbersSilver.put(19, R.drawable.crossword_number_silver_19);
        mapBadgeNumbersSilver.put(20, R.drawable.crossword_number_silver_20);
        mapBadgeNumbersSilver.put(21, R.drawable.crossword_number_silver_21);
        mapBadgeNumbersSilver.put(22, R.drawable.crossword_number_silver_22);
        mapBadgeNumbersSilver.put(23, R.drawable.crossword_number_silver_23);
        mapBadgeNumbersSilver.put(24, R.drawable.crossword_number_silver_24);
        mapBadgeNumbersSilver.put(25, R.drawable.crossword_number_silver_25);
        mapBadgeNumbersSilver.put(26, R.drawable.crossword_number_silver_26);
        mapBadgeNumbersSilver.put(27, R.drawable.crossword_number_silver_27);
        mapBadgeNumbersSilver.put(28, R.drawable.crossword_number_silver_28);
        mapBadgeNumbersSilver.put(29, R.drawable.crossword_number_silver_29);
        mapBadgeNumbersSilver.put(30, R.drawable.crossword_number_silver_30);
        mapBadgeNumbersSilver.put(31, R.drawable.crossword_number_silver_31);
        mapBadgeNumbersSilver.put(32, R.drawable.crossword_number_silver_32);

        mapBadgeNumbersFree.put(1, R.drawable.crossword_number_free_1);
        mapBadgeNumbersFree.put(2, R.drawable.crossword_number_free_2);
        mapBadgeNumbersFree.put(3, R.drawable.crossword_number_free_3);
        mapBadgeNumbersFree.put(4, R.drawable.crossword_number_free_4);
        mapBadgeNumbersFree.put(5, R.drawable.crossword_number_free_5);
        mapBadgeNumbersFree.put(6, R.drawable.crossword_number_free_6);
        mapBadgeNumbersFree.put(7, R.drawable.crossword_number_free_7);
        mapBadgeNumbersFree.put(8, R.drawable.crossword_number_free_8);
        mapBadgeNumbersFree.put(9, R.drawable.crossword_number_free_9);
        mapBadgeNumbersFree.put(10, R.drawable.crossword_number_free_10);
        mapBadgeNumbersFree.put(11, R.drawable.crossword_number_free_11);
        mapBadgeNumbersFree.put(12, R.drawable.crossword_number_free_12);
        mapBadgeNumbersFree.put(13, R.drawable.crossword_number_free_13);
        mapBadgeNumbersFree.put(14, R.drawable.crossword_number_free_14);
        mapBadgeNumbersFree.put(15, R.drawable.crossword_number_free_15);
        mapBadgeNumbersFree.put(16, R.drawable.crossword_number_free_16);
        mapBadgeNumbersFree.put(17, R.drawable.crossword_number_free_17);
        mapBadgeNumbersFree.put(18, R.drawable.crossword_number_free_18);
        mapBadgeNumbersFree.put(19, R.drawable.crossword_number_free_19);
        mapBadgeNumbersFree.put(20, R.drawable.crossword_number_free_20);
        mapBadgeNumbersFree.put(21, R.drawable.crossword_number_free_21);
        mapBadgeNumbersFree.put(22, R.drawable.crossword_number_free_22);
        mapBadgeNumbersFree.put(23, R.drawable.crossword_number_free_23);
        mapBadgeNumbersFree.put(24, R.drawable.crossword_number_free_24);
        mapBadgeNumbersFree.put(25, R.drawable.crossword_number_free_25);
        mapBadgeNumbersFree.put(26, R.drawable.crossword_number_free_26);
        mapBadgeNumbersFree.put(27, R.drawable.crossword_number_free_27);
        mapBadgeNumbersFree.put(28, R.drawable.crossword_number_free_28);
        mapBadgeNumbersFree.put(29, R.drawable.crossword_number_free_29);
        mapBadgeNumbersFree.put(30, R.drawable.crossword_number_free_30);
        mapBadgeNumbersFree.put(31, R.drawable.crossword_number_free_31);
        mapBadgeNumbersFree.put(32, R.drawable.crossword_number_free_32);

        mCrosswordPanelCurrent = new CrosswordPanelCurrent(view);
        mCrosswordPanelArchive = new CrosswordPanelArchive(view);
        mCrosswordPanelBuy = new CrosswordPanelBuy(view);

        View vCrossordArchiveBrilliant = CrosswordFragmentHolder.CrosswordElementArchive.addView(PANEL_ARCHIVE_BRILLIANT);
        View vCrossordArchiveGold = CrosswordFragmentHolder.CrosswordElementArchive.addView(PANEL_ARCHIVE_GOLD);
        View vCrossordArchiveGFree = CrosswordFragmentHolder.CrosswordElementArchive.addView(PANEL_ARCHIVE_FREE);
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(MonthSplitterView.addView("Апрель"));
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveBrilliant);
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(PanelSplitterView.addView());
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveGold);
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(PanelSplitterView.addView());
        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveGFree);

        View vBadgeUnresolved = CrosswordFragmentHolder.BadgeUnresolved.addView(BADGE_UNRESOLVE_FREE, 1);

        CrosswordFragmentHolder.CrosswordElementArchive.mCrosswordContainerLL.addView(vBadgeUnresolved);

    }

    // ================== SPLITTER WITH MONTH ======================

    static public class MonthSplitterView {

        @Nonnull static public TextView mMonthTV;

        public MonthSplitterView(@Nonnull View view){
            mMonthTV = (TextView) view.findViewById(R.id.crossword_splitter_month_textview);
        }

        static View addView(@Nonnull String month)
        {
            @Nonnull View view =  mInflater.inflate(R.layout.crossword_splitter_month, null, false);
            mMonthTV = (TextView) view.findViewById(R.id.crossword_splitter_month_textview);
            mMonthTV.setText(month);
            return view;
        }

    }

    static public class PanelSplitterView {

        static View addView()
        {
            @Nonnull View view =  mInflater.inflate(R.layout.crossword_splitter_panel, null, false);
            return view;
        }

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

        @Nonnull static public ToggleButton mSwitchToogleButton;

        static View addView(final int view_id)
        {
            @Nonnull View view = null;

            switch (view_id){
                case PANEL_ARCHIVE_BRILLIANT:{
                    view =  mInflater.inflate(R.layout.crossword_archive_brilliant, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_badges_container);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_brilliant_switcher);
                }break;
                case PANEL_ARCHIVE_GOLD:{
                    view = mInflater.inflate(R.layout.crossword_archive_gold, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_gold_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_gold_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_gold_badges_container);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_gold_switcher);
                }break;
                case PANEL_ARCHIVE_SILVER:{
                    view = mInflater.inflate(R.layout.crossword_archive_silver, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver_badges_container);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver_switcher);
                }break;
                case PANEL_ARCHIVE_SILVER2:{
                    view = mInflater.inflate(R.layout.crossword_archive_silver2, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_silver2_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_silver2_badges_container);
                    mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_silver2_switcher);
                }break;
                case PANEL_ARCHIVE_FREE:{
                    view = mInflater.inflate(R.layout.crossword_archive_free, null, false);
                    mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_partition);
                    mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_free_percent);
                    mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_free_score);
                    mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_bg);
                    mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_progress_fg);
                    mCrosswordContainerLL = (LinearLayout) view.findViewById(R.id.crossword_archive_free_badges_container);
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

        static View addView(final int view_id)
        {
            @Nonnull View view = null;
            switch (view_id){
                case PANEL_CURRENT_BRILLIANT:{
                    view = mInflater.inflate(R.layout.crossword_current_brilliant, null, false);
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
                    view = mInflater.inflate(R.layout.crossword_current_gold, null, false);
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
                    view = mInflater.inflate(R.layout.crossword_current_silver, null, false);
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
                    view = mInflater.inflate(R.layout.crossword_current_silver2, null, false);
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
                    view = mInflater.inflate(R.layout.crossword_current_free, null, false);
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

        static View addView(final int view_id, final int number)
        {
            @Nonnull View view = null;
            @Nonnull Bitmap bitmap = null;
            switch (view_id){
                case BADGE_RESOLVE_BRILLIANT:{
                    view = mInflater.inflate(R.layout.crossword_badge_brilliant_resolved, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_brilliant_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_brilliant_number_container);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersBrilliant.get(number));
                }break;
                case BADGE_RESOLVE_GOLD:{
                    view = mInflater.inflate(R.layout.crossword_badge_gold_resolved, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_gold_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_gold_number_container);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersGold.get(number));
                }break;
                case BADGE_RESOLVE_SILVER:
                case BADGE_RESOLVE_SILVER2:{
                    view = mInflater.inflate(R.layout.crossword_badge_silver_resolved, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_silver_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_silver_number_container);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersSilver.get(number));
                }break;
                case BADGE_RESOLVE_FREE:{
                    view = mInflater.inflate(R.layout.crossword_badge_free_resolved, null, false);
                    mScoreTV = (TextView) view.findViewById(R.id.crossword_badge_resolved_free_score);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_free_number_container);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersFree.get(number));
                }break;
                default: break;
            }
            ImageView image = new ImageView(mContext);
            image.setImageBitmap(bitmap);
            mBitmapLL.addView(image);
//            bitmap.recycle();
            return view;
        }
    }

    static public class BadgeUnresolved{
        @Nonnull static public TextView mPercentTV;
        @Nonnull static public LinearLayout mBitmapLL;
        @Nonnull static public LinearLayout mProgressBGLL;
        @Nonnull static public LinearLayout mProgressFGLL;

        static View addView(final int view_id, final int number)
        {
            @Nonnull View view = null;
            @Nonnull Bitmap bitmap = null;

            switch (view_id){
                case BADGE_UNRESOLVE_BRILLIANT:{
                    view = mInflater.inflate(R.layout.crossword_badge_brilliant_unresolved, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_brilliant_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_fg);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersBrilliant.get(number));
                }break;
                case BADGE_UNRESOLVE_GOLD:{
                    view = mInflater.inflate(R.layout.crossword_badge_gold_unresolved, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_gold_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_gold_progress_fg);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersGold.get(number));
                }break;
                case BADGE_UNRESOLVE_SILVER:
                case BADGE_UNRESOLVE_SILVER2:{
                    view = mInflater.inflate(R.layout.crossword_badge_silver_unresolved, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_silver_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_silver_progress_fg);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersSilver.get(number));
                }break;
                case BADGE_UNRESOLVE_FREE:{
                    view = mInflater.inflate(R.layout.crossword_badge_free_unresolved, null, false);
                    mPercentTV = (TextView) view.findViewById(R.id.crossword_badge_unresolved_free_percent);
                    mBitmapLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_number_container);
                    mProgressBGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_bg);
                    mProgressFGLL = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_free_progress_fg);
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), mapBadgeNumbersFree.get(number));
                }break;
                default: break;
            }

            ImageView image = new ImageView(mContext);
            image.setImageBitmap(bitmap);
            mBitmapLL.addView(image);
//            bitmap.recycle();
            return view;
        }
    }

    // ================== BADGES NUMBERS ======================

}