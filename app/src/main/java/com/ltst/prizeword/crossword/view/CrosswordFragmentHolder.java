package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 12.08.13.
 */
public class CrosswordFragmentHolder {

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull View mViewCrossword;
    private static @Nonnull Context mContext;
    public @Nonnull CrosswordPanelCurrentHolder mCrosswordPanelCurrent;
    public @Nonnull CrosswordPanelArchiveHolder mCrosswordPanelArchive;

    public CrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);
        mCrosswordPanelArchive = new CrosswordPanelArchiveHolder(view);
    }

    // ================== CROSSWORD PANELS ======================

    static public class CrosswordPanelCurrentHolder {

        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelCurrentHolder(@Nonnull View view){

            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
        }
    }

    static public class CrosswordPanelArchiveHolder {
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelArchiveHolder(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    // ================== CROSSWORD PANELS ITEM ======================

    public void addPanel(@Nonnull CrosswordPanelData data)
    {

        @Nonnull View view =  mInflater.inflate(R.layout.crossword_panel, null, false);
        @Nonnull LinearLayout pMonthBackground = (LinearLayout) view.findViewById(R.id.crossword_panel_splitter_month_bg);
        @Nonnull TextView pMonthText = (TextView) view.findViewById(R.id.crossword_panel_splitter_month_text);
        @Nonnull ImageView pTitleImage = (ImageView) view.findViewById(R.id.crossword_panel_logo_image);
        @Nonnull TextView pTitleText = (TextView) view.findViewById(R.id.crossword_panel_title_text);
        @Nonnull ToggleButton pSwitcher = (ToggleButton) view.findViewById(R.id.crossword_panel_switcher);

        @Nonnull LinearLayout pCurrentCrosswordContaiter = (LinearLayout) view.findViewById(R.id.crossword_panel_current_crossword_container);
        @Nonnull TextView pRatioText = (TextView) view.findViewById(R.id.crossword_panel_ratio);
        @Nonnull TextView pProgressText = (TextView) view.findViewById(R.id.crossword_panel_percent);
        @Nonnull LinearLayout pProgressBackground = (LinearLayout) view.findViewById(R.id.crossword_panel_progress_bg);
        @Nonnull LinearLayout pProgressForeround = (LinearLayout) view.findViewById(R.id.crossword_panel_progress_fg);
        @Nonnull TextView pScoreText = (TextView) view.findViewById(R.id.crossword_panel_score);

        @Nonnull LinearLayout pBuyCrosswordContaiter = (LinearLayout) view.findViewById(R.id.crossword_panel_buy_crossword_container);
        @Nonnull TextView pBuyCountText = (TextView) view.findViewById(R.id.crossword_panel_buy_count);
        @Nonnull TextView pBuyScore = (TextView) view.findViewById(R.id.crossword_panel_buy_score);
        @Nonnull LinearLayout pBuyButton = (LinearLayout) view.findViewById(R.id.crossword_panel_buy_button);
        @Nonnull TextView pBuyPrice = (TextView) view.findViewById(R.id.crossword_panel_buy_price);

        @Nonnull final BadgeGridView pBadgeContainer = (BadgeGridView) view.findViewById(R.id.crossword_panel_badges_container);

        @Nonnull Bitmap bitmap = null;

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            pBuyPrice.setText(R.string.buy_three_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_br);
            pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            pBuyPrice.setText(R.string.buy_two_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_au);
            pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            pBuyPrice.setText(R.string.buy_two_dollar);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_ag);
            pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            pBuyPrice.setText(R.string.buy_free);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.puzzles_set_fr);
            pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }
        pTitleImage.setImageBitmap(bitmap);
//        bitmap.recycle();

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        pProgressText.setText(sbProgress.toString());

        pScoreText.setText(String.valueOf(data.mScore));

        pBuyCountText.setText(String.valueOf(data.mBuyCount));

        pBuyScore.setText(String.valueOf(data.mBuyScore));

        if(data.mKind == CrosswordPanelData.KIND_CURRENT)
        {
            pSwitcher.setVisibility(View.GONE);
            pBuyCrosswordContaiter.setVisibility(View.GONE);
            pMonthBackground.setVisibility(View.GONE);

            if(data.mBadgeData != null){
                pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType,data.mBadgeData));
            }
            mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(view);
        }
        else if(data.mKind == CrosswordPanelData.KIND_BUY)
        {
            pSwitcher.setVisibility(View.GONE);
            pCurrentCrosswordContaiter.setVisibility(View.GONE);
            pMonthBackground.setVisibility(View.GONE);
            pBadgeContainer.setVisibility(View.GONE);

            mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(view);
        }
        else if(data.mKind == CrosswordPanelData.KIND_ARCHIVE)
        {
            pTitleImage.setVisibility(View.GONE);
            pBuyCrosswordContaiter.setVisibility(View.GONE);
            if(data.mMonth == null)
                pMonthBackground.setVisibility(View.GONE);
            else
                pMonthText.setText(data.mMonth);

            if(data.mBadgeData != null){
                pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType,data.mBadgeData));
            }
            mCrosswordPanelArchive.mCrosswordsContainerLL.addView(view);

            pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pBadgeContainer.setVisibility(b ? View.VISIBLE : View.GONE);
                }
            });

            pBuyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mICrosswordFragment.buyCrosswordSet();
                }
            });

            pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    mICrosswordFragment.choiceCrossword();
                }
            });
        }

    }

}