package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.GridView;
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

    final static int PANEL_BRILLIANT = 11;
    final static int PANEL_GOLD = 12;
    final static int PANEL_SILVER = 13;
    final static int PANEL_SILVER2 = 14;
    final static int PANEL_FREE = 15;

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull View mViewCrossword;
    private static @Nonnull Context mContext;
    public @Nonnull
    CrosswordWidgetCurrent mCrosswordPanelCurrent;
    public @Nonnull
    CrosswordWidgetArchive mCrosswordPanelArchive;
    public @Nonnull
    CrosswordWidgetBuy mCrosswordPanelBuy;

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

        mCrosswordPanelCurrent = new CrosswordWidgetCurrent(view);
        mCrosswordPanelArchive = new CrosswordWidgetArchive(view);
        mCrosswordPanelBuy = new CrosswordWidgetBuy(view);

//        View vCrossordCurrentBrilliant = CrosswordPanelCurrent.addView(PANEL_BRILLIANT);
//
//        View vCrossordBuyGold = CrosswordPanelBuy.addView(PANEL_GOLD);
//
//        View vCrossordArchiveBrilliant = CrosswordPanelArchive.addView(PANEL_BRILLIANT);
//        View vCrossordArchiveGold = CrosswordPanelArchive.addView(PANEL_GOLD);
//        View vCrossordArchiveGFree = CrosswordPanelArchive.addView(PANEL_FREE);
//
//        mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(vCrossordCurrentBrilliant);
//        mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(vCrossordBuyGold);
//
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(SplitterMonthView.addView("Апрель"));
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveBrilliant);
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(SplitterPanelView.addView());
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveGold);
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(SplitterPanelView.addView());
//        mCrosswordPanelArchive.mCrosswordsContainerLL.addView(vCrossordArchiveGFree);
//
//        BadgeData[] data = new BadgeData[10];
//        for(int i=0; i<10; i++){
//            BadgeData badge = new BadgeData();
//            badge.mNumber = 29;
//            badge.mStatus = BadgeData.STATUS_UNRESOLVED;
//            badge.mType = BadgeData.TYPE_BRILLIANT;
//            badge.mProgress = 95;
//            badge.mScore = 98000;
//            data[i] = badge;
//        }
//
//        CrosswordPanelArchive.mBadgeContainerLL.setAdapter(new BadgeAdapter(mContext,data));
    }

    // ================== SPLITTER WITH MONTH ======================

    static public class SplitterMonthView {

        @Nonnull static public TextView mMonthTV;

        static View addView(@Nonnull String month)
        {
            @Nonnull View view =  mInflater.inflate(R.layout.crossword_splitter_month, null, false);
            @Nonnull TextView mMonthTV = (TextView) view.findViewById(R.id.crossword_splitter_month_textview);
            mMonthTV.setText(month);
            return view;
        }

    }

    static public class SplitterPanelView {

        static View addView()
        {
            @Nonnull View view =  mInflater.inflate(R.layout.crossword_splitter_panel, null, false);
            return view;
        }

    }

    // ================== CROSSWORD PANELS ======================

    static public class CrosswordWidgetCurrent {

        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordWidgetCurrent(@Nonnull View view){

            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
        }
    }

    static public class CrosswordWidgetArchive {
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordWidgetArchive(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    static public class CrosswordWidgetBuy {

        @Nonnull public TextView mRestHintsTV;

        @Nonnull public TextView mBuy10TV;
        @Nonnull public TextView mBuy20TV;
        @Nonnull public TextView mBuy30TV;

        @Nonnull public LinearLayout mBuy10Button;
        @Nonnull public LinearLayout mBuy20Button;
        @Nonnull public LinearLayout mBuy30Button;

        public CrosswordWidgetBuy(@Nonnull View view){
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

    public void addPanel(@Nonnull CrosswordPanelData data)
    {
        if(data.mKind == CrosswordPanelData.KIND_ARCHIVE)
        {
            
        }
        else if(data.mKind == CrosswordPanelData.KIND_ARCHIVE)
        {

        }
        else if(data.mKind == CrosswordPanelData.KIND_ARCHIVE)
        {

        }

    }

    // ================== CROSSWORD PANELS ITEM ======================


    static private class CrosswordPanelArchive {
        @Nonnull static public TextView mTitleTV;
        @Nonnull static public TextView mRatioResolvedTV;
        @Nonnull static public TextView mPercentResolvedTV;
        @Nonnull static public TextView mTotalScoreTV;
        @Nonnull static public LinearLayout mProgressBackgroudLL;
        @Nonnull static public LinearLayout mProgressForegroudLL;

        @Nonnull static public GridView mBadgeContainerLL;

        @Nonnull static public ToggleButton mSwitchToogleButton;

        static View addView(final int view_id)
        {
            @Nonnull View view =  mInflater.inflate(R.layout.crossword_panel_archive, null, false);
            mTitleTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_title_text);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_archive_brilliant_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_archive_brilliant_progress_fg);
            mBadgeContainerLL = (GridView) view.findViewById(R.id.crossword_archive_brilliant_badges_container);
            mSwitchToogleButton = (ToggleButton) view.findViewById(R.id.crossword_archive_brilliant_switcher);

            switch (view_id){
                case PANEL_BRILLIANT:{
                    mTitleTV.setText(R.string.puzzless_hint_brilliant_crossword);
                }break;
                case PANEL_GOLD:{
                    mTitleTV.setText(R.string.puzzless_hint_gold_crossword);
                }break;
                case PANEL_SILVER:{
                    mTitleTV.setText(R.string.puzzless_hint_silver_crossword);
                }break;
                case PANEL_SILVER2:{
                    mTitleTV.setText(R.string.puzzless_hint_silver2_crossword);
                }break;
                case PANEL_FREE:{
                    mTitleTV.setText(R.string.puzzless_hint_free_crossword);
                }break;

                default: break;
            }

            mSwitchToogleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    mProgressForegroudLL->setVisibility(LinearLayout.GONE);
                }
            });

            return view;
        }
    }

    // ================== CROSSWORD PANELS ELEMENTS ======================

    static private class CrosswordPanelCurrent {
        @Nonnull static private ImageView mImage;
        @Nonnull static private TextView mTitleTV;
        @Nonnull static public TextView mRatioResolvedTV;
        @Nonnull static public TextView mPercentResolvedTV;
        @Nonnull static public TextView mTotalScoreTV;
        @Nonnull static public LinearLayout mProgressBackgroudLL;
        @Nonnull static public LinearLayout mProgressForegroudLL;
        @Nonnull static public GridView mBadgeContainerLL;

        static View addView(final int view_id)
        {
            @Nonnull View view = mInflater.inflate(R.layout.crossword_panel_current, null, false);
            mImage = (ImageView) view.findViewById(R.id.crossword_current_brilliant_logo_image);
            mTitleTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_title_text);
            mRatioResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_partition);
            mPercentResolvedTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_percent);
            mTotalScoreTV = (TextView) view.findViewById(R.id.crossword_current_brilliant_score);
            mProgressBackgroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_bg);
            mProgressForegroudLL = (LinearLayout) view.findViewById(R.id.crossword_current_brilliant_progress_fg);
            mBadgeContainerLL = (GridView) view.findViewById(R.id.crossword_current_brilliant_badges_container);

            Bitmap bitmap = null;

            switch (view_id){
                case PANEL_BRILLIANT:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_br);
                    mTitleTV.setText(R.string.puzzless_hint_brilliant_crossword);
                }break;
                case PANEL_GOLD:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_au);
                    mTitleTV.setText(R.string.puzzless_hint_gold_crossword);
                }break;
                case PANEL_SILVER:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag);
                    mTitleTV.setText(R.string.puzzless_hint_silver_crossword);
                }break;
                case PANEL_SILVER2:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag2);
                    mTitleTV.setText(R.string.puzzless_hint_silver2_crossword);
                }break;
                case PANEL_FREE:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_fr);
                    mTitleTV.setText(R.string.puzzless_hint_free_crossword);
                }break;
                default: break;
            }
            mImage.setImageBitmap(bitmap);
            return view;
        }
    }

    // ================== CROSSWORD PANELS ELEMENTS ======================

    static private class CrosswordPanelBuy {
        @Nonnull static private ImageView mImage;
        @Nonnull static private TextView mTitleTV;
        @Nonnull static public TextView mCountCrosswordsTV;
        @Nonnull static public TextView mCountScoreTV;
        @Nonnull static public LinearLayout mBuyButton;
        @Nonnull static public TextView mBuyPriceTV;

        static View addView(final int view_id)
        {
            @Nonnull View view = mInflater.inflate(R.layout.crossword_panel_buy, null, false);
            mImage = (ImageView) view.findViewById(R.id.crossword_buy_brilliant_logo_image);
            mTitleTV = (TextView) view.findViewById(R.id.crossword_buy_brilliant_title_text);
            mCountCrosswordsTV = (TextView) view.findViewById(R.id.crossword_buy_brilliant_buy_count);
            mCountScoreTV = (TextView) view.findViewById(R.id.crossword_buy_brilliant_buy_count);
            mBuyButton = (LinearLayout) view.findViewById(R.id.crossword_buy_brilliant_buy_button);
            mBuyPriceTV = (TextView) view.findViewById(R.id.crossword_buy_brilliant_buy_price);

            Bitmap bitmap = null;

            switch (view_id){
                case PANEL_BRILLIANT:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_br);
                    mTitleTV.setText(R.string.puzzless_hint_brilliant_crossword);
                }break;
                case PANEL_GOLD:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_au);
                    mTitleTV.setText(R.string.puzzless_hint_gold_crossword);
                }break;
                case PANEL_SILVER:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag);
                    mTitleTV.setText(R.string.puzzless_hint_silver_crossword);
                }break;
                case PANEL_SILVER2:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag2);
                    mTitleTV.setText(R.string.puzzless_hint_silver2_crossword);
                }break;
                case PANEL_FREE:{
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_fr);
                    mTitleTV.setText(R.string.puzzless_hint_free_crossword);
                }break;
                default: break;
            }
            mImage.setImageBitmap(bitmap);

            return view;
        }
    }
}