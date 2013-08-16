package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.Puzzle;
import com.ltst.prizeword.crossword.model.PuzzleSet;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;

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
    private @Nonnull HashMap<Long, CrosswordSet> mListCrosswordSet;

    public CrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mListCrosswordSet = new HashMap<Long, CrosswordSet>();
        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);
        mCrosswordPanelArchive = new CrosswordPanelArchiveHolder(view);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int monthMaxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int scopeDays = Integer.valueOf(mContext.getResources().getString(R.string.puzzless_rest_scope_days));
        int restDays = monthMaxDays - day;

        DateFormatSymbols symbols = new DateFormatSymbols();
        mCrosswordPanelCurrent.mMonthTV.setText(symbols.getMonths()[month]);

        mCrosswordPanelCurrent.mRestDaysTV.setText(String.valueOf(restDays));
        mCrosswordPanelCurrent.mRestPanelLL.setBackgroundDrawable(
                mContext.getResources().getDrawable(restDays > scopeDays
                ? R.drawable.puzzles_current_puzzles_head_rest_panel_nocritical
                : R.drawable.puzzles_current_puzzles_head_rest_panel_clitical));

    }

    // ================== CROSSWORD PANELS ======================

    static private class CrosswordPanelCurrentHolder {

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

    static private class CrosswordPanelArchiveHolder {
        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelArchiveHolder(@Nonnull View view){
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
        }
    }

    private CrosswordSet getCrosswordSet(long id)
    {
        return (id >= 0 && mListCrosswordSet.containsKey(id)) ? mListCrosswordSet.get(id) : new CrosswordSet();
    }

    // ================== CROSSWORD PANELS ITEM ======================

    private static class CrosswordSet
    {
        @Nonnull View mRootView;
        @Nonnull LinearLayout pMonthBackground;
        @Nonnull TextView pMonthText;
        @Nonnull LinearLayout pTitleImage;
        @Nonnull TextView pTitleText;
        @Nonnull ToggleButton pSwitcher;

        @Nonnull LinearLayout pCurrentCrosswordContaiter;
        @Nonnull TextView pRatioText;
        @Nonnull TextView pProgressText;
        @Nonnull LinearLayout pProgressBackground;
        @Nonnull LinearLayout pProgressForeround;
        @Nonnull TextView pScoreText;

        @Nonnull LinearLayout pBuyCrosswordContaiter;
        @Nonnull TextView pBuyCountText;
        @Nonnull TextView pBuyScore;
        @Nonnull LinearLayout pBuyButton;
        @Nonnull TextView pBuyPrice;

        @Nonnull BadgeGridView pBadgeContainer;
        
        
        private CrosswordSet() {
            mRootView =  mInflater.inflate(R.layout.crossword_panel, null, false);
            pMonthBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_splitter_month_bg);
            pMonthText = (TextView) mRootView.findViewById(R.id.crossword_panel_splitter_month_text);
            pTitleImage = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_logo_image);
            pTitleText = (TextView) mRootView.findViewById(R.id.crossword_panel_title_text);
            pSwitcher = (ToggleButton) mRootView.findViewById(R.id.crossword_panel_switcher);

            pCurrentCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_current_crossword_container);
            pRatioText = (TextView) mRootView.findViewById(R.id.crossword_panel_ratio);
            pProgressText = (TextView) mRootView.findViewById(R.id.crossword_panel_percent);
            pProgressBackground = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_progress_bg);
            pProgressForeround = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_progress_fg);
            pScoreText = (TextView) mRootView.findViewById(R.id.crossword_panel_score);

            pBuyCrosswordContaiter = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_crossword_container);
            pBuyCountText = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_count);
            pBuyScore = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_score);
            pBuyButton = (LinearLayout) mRootView.findViewById(R.id.crossword_panel_buy_button);
            pBuyPrice = (TextView) mRootView.findViewById(R.id.crossword_panel_buy_price);

            pBadgeContainer = (BadgeGridView) mRootView.findViewById(R.id.crossword_panel_badges_container);
        }
    }

    public void addPanel(@Nonnull PuzzleSet set)
    {
        CrosswordPanelData data = new CrosswordPanelData();
        data.mId = set.id;
        data.mServerId = set.serverId;
        data.mKind = CrosswordPanelData.KIND_CURRENT;
        data.mType = PuzzleSetModel.getPuzzleTypeByString(set.type);
        data.mBought = set.isBought;
        data.mMonth = set.month;
        data.mYear = set.year;

        CrosswordSet crosswordSet = getCrosswordSet(data.mId);
        fillPanel(crosswordSet, data);
    }

    private void fillPanel(@Nonnull CrosswordSet crosswordSet, @Nonnull CrosswordPanelData data)
    {
        if (data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_three_dollar);
            crosswordSet.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_br));
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_brilliant_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_two_dollar);
            crosswordSet.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_au));
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_gold_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_two_dollar);
            crosswordSet.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_ag));
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_silver_crossword);
        }
        else if (data.mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            crosswordSet.pBuyPrice.setText(R.string.buy_free);
            crosswordSet.pTitleImage.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.puzzles_set_fr));
            crosswordSet.pTitleText.setText(R.string.puzzless_hint_free_crossword);
        }

        StringBuilder sbRatio = new StringBuilder();
        sbRatio.append(data.mResolveCount);
        sbRatio.append("/");
        sbRatio.append(data.mTotalCount);
        crosswordSet.pRatioText.setText(sbRatio.toString());

        StringBuilder sbProgress = new StringBuilder();
        sbProgress.append(data.mProgress);
        sbProgress.append("%");
        crosswordSet.pProgressText.setText(sbProgress.toString());

        crosswordSet.pScoreText.setText(String.valueOf(data.mScore));

        crosswordSet.pBuyCountText.setText(String.valueOf(data.mBuyCount));

        crosswordSet.pBuyScore.setText(String.valueOf(data.mBuyScore));

        crosswordSet.pBadgeContainer.setAdapter(new BadgeAdapter(mContext,data.mType));

//        if(data.mKind == CrosswordPanelData.KIND_CURRENT)

        if(data.mMonth == Calendar.getInstance().get(Calendar.MONTH)+1)
        {
            // Текущие наборы сетов сканвордов;
            if(data.mBought)
            {
                // Куплены;
                crosswordSet.pSwitcher.setVisibility(View.GONE);
                crosswordSet.pBuyCrosswordContaiter.setVisibility(View.GONE);
                crosswordSet.pMonthBackground.setVisibility(View.GONE);
            }
            else
            {
                // Некуплены;
                crosswordSet.pSwitcher.setVisibility(View.GONE);
                crosswordSet.pCurrentCrosswordContaiter.setVisibility(View.GONE);
                crosswordSet.pMonthBackground.setVisibility(View.GONE);
                crosswordSet.pBadgeContainer.setVisibility(View.GONE);
            }
            // Если такого сета еще нет, то сохраняем его и добавляем на панель;
            if(!mListCrosswordSet.containsKey(data.mId))
            {
                mListCrosswordSet.put(data.mId, crosswordSet);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSet.mRootView);
                crosswordSet.pBadgeContainer.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            // Архивные наборы сетов сканвордов;
            crosswordSet.pTitleImage.setVisibility(View.GONE);
            crosswordSet.pBuyCrosswordContaiter.setVisibility(View.GONE);
            if(data.mMonth == 0)
            {
                crosswordSet.pMonthBackground.setVisibility(View.GONE);
            }
            else
            {
                DateFormatSymbols symbols = new DateFormatSymbols();
                crosswordSet.pMonthText.setText(symbols.getMonths()[data.mMonth-1]);
            }

            // Если такого сета еще нет, то сохраняем его и добавляем на панель;
            if(!mListCrosswordSet.containsKey(data.mId))
            {
                mListCrosswordSet.put(data.mId, crosswordSet);
                mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSet.mRootView);
                crosswordSet.pBadgeContainer.setVisibility(View.GONE);
            }
        }

        final @Nonnull CrosswordSet crosswordSetFinal = crosswordSet;
        final @Nonnull CrosswordPanelData dataFinal = data;
        crosswordSet.pSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                crosswordSetFinal.pBadgeContainer.setVisibility(b ? View.GONE : View.VISIBLE);
            }
        });

        crosswordSet.pBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mICrosswordFragment.buyCrosswordSet(dataFinal.mServerId);
            }
        });

        crosswordSet.pBadgeContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mICrosswordFragment.choiceCrossword();
            }
        });
    }

    public void addBadge(@Nonnull Puzzle puzzle){
        if(puzzle == null) return;
        BadgeData data = new BadgeData();
        data.mId = puzzle.id;
        data.mServerId = puzzle.serverId;
        data.mStatus = puzzle.isSolved;
        data.mScore = puzzle.score;
        data.mProgress = puzzle.solvedPercent;
        data.mSetId = puzzle.setId;
        CrosswordSet crosswordSet = getCrosswordSet(data.mSetId);
        BadgeAdapter adapter = (BadgeAdapter) crosswordSet.pBadgeContainer.getAdapter();
//        if(adapter == null)
//        {
//            adapter = new BadgeAdapter(mContext, PuzzleSetModel.PuzzleSetType.FREE);
//            crosswordSet.pBadgeContainer.setAdapter(adapter);
//        }
        adapter.addBadgeData(data);
        adapter.notifyDataSetChanged();
    }

}