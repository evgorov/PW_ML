package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSet {

    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull Context mContext;
    private @Nonnull LayoutInflater mInflater;

    private @Nonnull CrosswordSetType mCrosswordSetType;

    private @Nonnull View mRootView;
    private @Nonnull LinearLayout pMonthBackground;
    private @Nonnull TextView pMonthText;
    private @Nonnull LinearLayout pTitleImage;
    private @Nonnull TextView pTitleText;
    private @Nonnull ToggleButton pSwitcher;

    private @Nonnull LinearLayout pCurrentCrosswordContaiter;
    private @Nonnull TextView pRatioText;
    private @Nonnull TextView pProgressText;
    private @Nonnull BadgeProgressBar pProgress;
    private @Nonnull TextView pScoreText;

    private @Nonnull LinearLayout pBuyCrosswordContaiter;
    private @Nonnull TextView pBuyCountText;
    private @Nonnull TextView pBuyScore;
    private @Nonnull LinearLayout pBuyButton;
    private @Nonnull TextView pBuyPrice;

    private @Nonnull BadgeGridView pBadgeContainer;

    private @Nullable String mSetServerId = null;


    public CrosswordSet(@Nonnull Context context, @Nonnull ICrosswordFragment iCrosswordFragment) {

        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mICrosswordFragment = iCrosswordFragment;

        mRootView =  mInflater.inflate(R.layout.crossword_panel, null, false);
        pMonthBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_splitter_month_bg);
        pMonthText = (TextView) mRootView.findViewById(R.id.crossword_panel_splitter_month_text);
        pTitleImage = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_logo_image);
        pTitleText = (TextView) mRootView.findViewById(R.id.crossword_panel_title_text);
        pSwitcher = (ToggleButton) mRootView.findViewById(R.id.crossword_panel_switcher);

        pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_current_crossword_container);
        pRatioText = (TextView) mRootView.findViewById(R.id.crossword_panel_ratio);
        pProgressText = (TextView) mRootView.findViewById(R.id.crossword_panel_percent);
        pProgress = new BadgeProgressBar(context, mRootView, R.id.crossword_panel_progress_bg, R.id.crossword_panel_progress_fg);
        pScoreText = (TextView) mRootView.findViewById(R.id.crossword_panel_score);

        pBuyCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_crossword_container);
        pBuyCountText = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_count);
        pBuyScore = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_score);
        pBuyButton = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_button);
        pBuyPrice = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_price);

        pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.crossword_panel_badges_container);

        pMonthBackground.setVisibility(View.GONE);

        pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pBadgeContainer.setVisibility(b ? View.GONE : View.VISIBLE);
            }
        });

        pBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSetServerId != null)
                {
                    mICrosswordFragment.buyCrosswordSet(mSetServerId);
                }
            }
        });

        pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long puzzleId) {
                if (mSetServerId != null)
                {
                    mICrosswordFragment.choicePuzzle(mSetServerId, puzzleId);
                }
            }
        });

    }

    public @Nonnull View getView()
    {
        return mRootView;
    }

    public @Nonnull BadgeAdapter getAdapter(){
        return (BadgeAdapter) pBadgeContainer.getAdapter();
    }

    public void fillPanel(@Nonnull CrosswordPanelData data)
    {
//        mRootView.setVisibility(View.VISIBLE);

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            pBuyPrice.setText(R.string.buy_three_dollar);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_br));
            pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            pBuyPrice.setText(R.string.buy_two_dollar);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_au));
            pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            pBuyPrice.setText(R.string.buy_two_dollar);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag));
            pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER2)
        {
            pBuyPrice.setText(R.string.buy_two_dollar);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag2));
            pTitleText.setText(R.string.puzzless_hint_silver2_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            pBuyPrice.setText(R.string.buy_free);
            pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_fr));
            pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        pProgressText.setText(sbProgress.toString());
        pProgress.setProgress(data.mProgress);

        pScoreText.setText(String.valueOf(data.mScore));

        pBuyCountText.setText(String.valueOf(data.mBuyCount));

        pBuyScore.setText(String.valueOf(data.mBuyScore));

        if(pBadgeContainer.getAdapter() == null)
            pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType));

        if(data.mMonth == Calendar.getInstance().get(Calendar.MONTH)+1)
        {
            // Текущие наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.CURRENT;
            pTitleImage.setVisibility(View.VISIBLE);
            pSwitcher.setVisibility(View.GONE);

            if(mSetServerId == null)
            {
                pBadgeContainer.setVisibility(View.VISIBLE);
            }

            if(data.mBought)
            {
                // Куплены;
                pBuyCrosswordContaiter.setVisibility(View.GONE);
            }
            else
            {
                // Некуплены;
                pCurrentCrosswordContaiter.setVisibility(View.GONE);
                pBadgeContainer.setVisibility(View.GONE);
            }
        }
        else
        {
            // Архивные наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.ARCHIVE;
            pTitleImage.setVisibility(View.VISIBLE);
            pBuyCrosswordContaiter.setVisibility(View.GONE);

            if(mSetServerId == null)
            {
                pBadgeContainer.setVisibility(View.GONE);
            }
            if(data.mMonth == 0)
            {
            }
            else
            {
//                DateFormatSymbols symbols = new DateFormatSymbols();
//                pMonthText.setText(symbols.getMonths()[data.mMonth-1]);
                pMonthText.setText(mContext.getResources().getStringArray(
                        R.array.menu_group_months_at_imenit_padezh)[data.mMonth-1]);
            }
        }
        mSetServerId = data.mServerId;
    }

    public void setVisibleMonth(boolean visible)
    {
        pMonthBackground.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public CrosswordSetType getCrosswordSetType()
    {
        return mCrosswordSetType;
    }

    public enum CrosswordSetType{
        CURRENT,
        ARCHIVE
    }

}
