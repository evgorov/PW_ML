package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.annotation.Nonnull;

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
    private @Nonnull SeekBar pProgressSeekBar;
    private @Nonnull TextView pScoreText;

    private @Nonnull LinearLayout pBuyCrosswordContaiter;
    private @Nonnull TextView pBuyCountText;
    private @Nonnull TextView pBuyScore;
    private @Nonnull LinearLayout pBuyButton;
    private @Nonnull TextView pBuyPrice;

    private @Nonnull BadgeGridView pBadgeContainer;


    public CrosswordSet(@Nonnull Context context, @Nonnull ICrosswordFragment iCrosswordFragment) {

        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mICrosswordFragment = iCrosswordFragment;

        this.mRootView =  mInflater.inflate(R.layout.crossword_panel, null, false);
        this.pMonthBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_splitter_month_bg);
        this.pMonthText = (TextView) mRootView.findViewById(R.id.crossword_panel_splitter_month_text);
        this.pTitleImage = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_logo_image);
        this.pTitleText = (TextView) mRootView.findViewById(R.id.crossword_panel_title_text);
        this.pSwitcher = (ToggleButton) mRootView.findViewById(R.id.crossword_panel_switcher);

        this.pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_current_crossword_container);
        this.pRatioText = (TextView) mRootView.findViewById(R.id.crossword_panel_ratio);
        this.pProgressText = (TextView) mRootView.findViewById(R.id.crossword_panel_percent);
        this.pProgressSeekBar = (SeekBar) mRootView.findViewById(R.id.crossword_set_progressbar);
        pProgressSeekBar.setEnabled(false);
        this.pScoreText = (TextView) mRootView.findViewById(R.id.crossword_panel_score);

        this.pBuyCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_crossword_container);
        this.pBuyCountText = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_count);
        this.pBuyScore = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_score);
        this.pBuyButton = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_button);
        this.pBuyPrice = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_price);

        this.pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.crossword_panel_badges_container);

        this.pMonthBackground.setVisibility(View.GONE);

//        this.mRootView.setVisibility(View.GONE);

    }

    public @Nonnull View getView()
    {
        return this.mRootView;
    }

    public @Nonnull BadgeAdapter getAdapter(){
        return (BadgeAdapter) this.pBadgeContainer.getAdapter();
    }

    public void fillPanel(@Nonnull CrosswordPanelData data)
    {
//        this.mRootView.setVisibility(View.VISIBLE);

        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            this.pBuyPrice.setText(R.string.buy_three_dollar);
            this.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_br));
            this.pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            this.pBuyPrice.setText(R.string.buy_two_dollar);
            this.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_au));
            this.pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            this.pBuyPrice.setText(R.string.buy_two_dollar);
            this.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag));
            this.pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER2)
        {
            this.pBuyPrice.setText(R.string.buy_two_dollar);
            this.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag2));
            this.pTitleText.setText(R.string.puzzless_hint_silver2_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            this.pBuyPrice.setText(R.string.buy_free);
            this.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_fr));
            this.pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        this.pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        this.pProgressText.setText(sbProgress.toString());

        this.pScoreText.setText(String.valueOf(data.mScore));

        this.pBuyCountText.setText(String.valueOf(data.mBuyCount));

        this.pBuyScore.setText(String.valueOf(data.mBuyScore));

        this.pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType));

        if(data.mMonth == Calendar.getInstance().get(Calendar.MONTH)+1)
        {
            // Текущие наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.CURRENT;
            this.pTitleImage.setVisibility(View.VISIBLE);
            this.pBadgeContainer.setVisibility(View.VISIBLE);
            if(data.mBought)
            {
                // Куплены;
                this.pSwitcher.setVisibility(View.GONE);
                this.pBuyCrosswordContaiter.setVisibility(View.GONE);
            }
            else
            {
                // Некуплены;
                this.pSwitcher.setVisibility(View.GONE);
                this.pCurrentCrosswordContaiter.setVisibility(View.GONE);
                this.pBadgeContainer.setVisibility(View.GONE);
            }
        }
        else
        {
            // Архивные наборы сетов сканвордов;
            mCrosswordSetType = CrosswordSetType.ARCHIVE;
            this.pTitleImage.setVisibility(View.VISIBLE);
            this.pBuyCrosswordContaiter.setVisibility(View.GONE);
            this.pBadgeContainer.setVisibility(View.GONE);
            if(data.mMonth == 0)
            {
            }
            else
            {
                DateFormatSymbols symbols = new DateFormatSymbols();
                this.pMonthText.setText(symbols.getMonths()[data.mMonth-1]);
            }

        }

        final @Nonnull CrosswordPanelData dataFinal = data;
        this.pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pBadgeContainer.setVisibility(b ? View.GONE : View.VISIBLE);
            }
        });

        this.pBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mICrosswordFragment.buyCrosswordSet(dataFinal.mServerId);
            }
        });

        this.pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mICrosswordFragment.choiceCrossword();
            }
        });
    }

    public void setVisibleMonth(boolean visible)
    {
        this.pMonthBackground.setVisibility(visible ? View.VISIBLE : View.GONE);
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
