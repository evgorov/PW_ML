package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;
import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.score.Coefficients;
import com.ltst.przwrd.tools.DeclensionTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 12.08.13.
 */
public class CrosswordsFragmentHolder
{

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private static @Nonnull Context mContext;
    public @Nonnull CrosswordPanelCurrentHolder mCrosswordPanelCurrent;
    public @Nonnull CrosswordPanelBuyHolder mCrosswordPanelBuy;
    public @Nonnull CrosswordPanelArchiveHolder mCrosswordPanelArchive;
    private @Nonnull HashMap<String, CrosswordSetMonth> mListCrosswordSetMonth;
    private @Nonnull List<PuzzleSet> mPuzzleSetList;

    @Nonnull Coefficients mCoefficients;

    public CrosswordsFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                    @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mContext = context;

        mListCrosswordSetMonth = new HashMap<String, CrosswordSetMonth>();
        mPuzzleSetList = new ArrayList<PuzzleSet>();

        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);
        mCrosswordPanelArchive = new CrosswordPanelArchiveHolder(view);
        mCrosswordPanelBuy = new CrosswordPanelBuyHolder(view);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int monthMaxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int scopeDays = Integer.valueOf(mContext.getResources().getString(R.string.puzzless_rest_scope_days));
        int restDays = monthMaxDays - day + 1;

        mCrosswordPanelCurrent.mMonthTV.setText(
                mContext.getResources().getStringArray(R.array.menu_group_months_at_imenit_padezh)[month]);

        mCrosswordPanelCurrent.mRestDaysTV.setText(String.valueOf(restDays));
        String[] days = mContext.getResources().getStringArray(R.array.puzzless_hint_rest_days);
        mCrosswordPanelCurrent.mRestTextDaysTV.setText(DeclensionTools.units(restDays,days));
        mCrosswordPanelCurrent.mRestPanelLL.setVisibility(restDays > 5 ? View.GONE : View.VISIBLE); // скрываем панель, если осталось >5 дней
        mCrosswordPanelCurrent.mRestPanelLL.setBackgroundDrawable(
                mContext.getResources().getDrawable(restDays > scopeDays
                        ? R.drawable.puzzles_current_puzzles_head_rest_panel_nocritical
                        : R.drawable.puzzles_current_puzzles_head_rest_panel_clitical));

    }

    @Nonnull
    public Coefficients getCoefficients() {
        return mCoefficients;
    }

    public void setCoefficients(@Nonnull Coefficients coefficients) {
        this.mCoefficients = coefficients;
    }

    // ================== CROSSWORD PANELS ======================

    static private class CrosswordPanelCurrentHolder
    {
        @Nonnull public TextView mMonthTV;
        @Nonnull public TextView mRestDaysTV;
        @Nonnull public TextView mRestTextDaysTV;
        @Nonnull public LinearLayout mRestPanelLL;
        @Nonnull public LinearLayout mCrosswordsContainerLL;
        @Nonnull public BackFrameLayout mCrosswordsContainerBackgroud;

        public CrosswordPanelCurrentHolder(@Nonnull View view)
        {
            mMonthTV = (TextView) view.findViewById(R.id.crossword_fragment_current_month);
            mRestDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_days);
            mRestTextDaysTV = (TextView) view.findViewById(R.id.crossword_fragment_current_remain_count_textdays);
            mRestPanelLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_remain_panel);
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_current_container);
            mCrosswordsContainerBackgroud = (BackFrameLayout) view.findViewById(R.id.crossword_fragment_layout_container1);
        }
    }

    static private class CrosswordPanelBuyHolder {
        @Nonnull public BackFrameLayout mCrosswordsContainerBackgroud;
        public CrosswordPanelBuyHolder(@Nonnull View view){
            mCrosswordsContainerBackgroud = (BackFrameLayout) view.findViewById(R.id.crossword_fragment_layout_container2);
        }
    }

        static private class CrosswordPanelArchiveHolder {
        @Nonnull public LinearLayout mCrosswordsContainerLL;
            @Nonnull public BackFrameLayout mCrosswordsContainerBackgroud;

        public CrosswordPanelArchiveHolder(@Nonnull View view)
        {
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.crossword_fragment_archive_container);
            mCrosswordsContainerBackgroud = (BackFrameLayout) view.findViewById(R.id.crossword_fragment_layout_container3);
        }
    }

    private CrosswordSetMonth getCrosswordSetMonth(String key)
    {
        return (mListCrosswordSetMonth.containsKey(key)) ? mListCrosswordSetMonth.get(key) : new CrosswordSetMonth(mContext);
    }

    public PuzzleSet getPuzzleSet(@Nonnull String puzzleSetServerId)
    {
        for(@Nonnull PuzzleSet puzzleSet : mPuzzleSetList)
        {
            if(puzzleSet.serverId.equals(puzzleSetServerId))
                return puzzleSet;
        }
        return null;
    }

    public boolean isNeedUploadAllPuzzlrSetsFromInternet()
    {
        for(CrosswordSetMonth crosswordSetMonth : mListCrosswordSetMonth.values())
        {
            if(!crosswordSetMonth.emptyCrosswordDatas())
            {
                return false;
            }
        }
        return true;
    }

    public boolean isNeedUploadCurrentPuzzlrSetsFromInternet()
    {
        long currentTime = SharedPreferencesHelper.getInstance(mContext).getLong(SharedPreferencesValues.SP_CURRENT_DATE, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        int month = cal.get(Calendar.MONTH)+1;
        int year = cal.get(Calendar.YEAR);
        @Nonnull String key = formatTime(year, month);
        return !mListCrosswordSetMonth.containsKey(key);
    }

    // ================== CROSSWORD PANELS ITEM ======================

    public CrosswordSetData extractCrosswordPanelData(@Nonnull PuzzleSet set)
    {
        CrosswordSetData data = new CrosswordSetData();
        data.mId = set.id;
        data.mServerId = set.serverId;
        data.mKind = CrosswordSetData.KIND_CURRENT;
        data.mType = PuzzleSetModel.getPuzzleTypeByString(set.type);
        data.mBought = set.isBought;
        data.mMonth = set.month;
        data.mYear = set.year;
        data.mScore = 0;
        data.mProgress = 0;
        data.mTotalCount = 0;

        return data;
    }

    @Nonnull private String formatTime(int year, int month)
    {
        return String.format("%d-%d", year, month);
    }

    public void fillSet(@Nonnull List<PuzzleSet> sets, @Nonnull HashMap<String, List<Puzzle>> mapPuzzles)
    {
        mPuzzleSetList.addAll(sets);
        for (PuzzleSet set : sets)
        {
            CrosswordSetData data = extractCrosswordPanelData(set);
            addPanel(data);
            @Nonnull List<Puzzle> puzzles = mapPuzzles.get(set.serverId);

            int solved = 0;
            int scores = 0;
            int percents = 0;
            for (@Nonnull Puzzle puzzle : puzzles)
            {
                addBadge(puzzle);
                percents += puzzle.solvedPercent;
                scores += puzzle.score;
                if (puzzle.isSolved)
                    solved++;
            }
            if (puzzles.size() != 0)
            {
                percents = percents / puzzles.size();
            }
            data.mScore = scores;
            data.mProgress = percents;
            data.mResolveCount = solved;
            data.mTotalCount = puzzles.size();

            int baseScore = 0;
            switch (data.mType)
            {
                case BRILLIANT:
                    baseScore = mCoefficients.brilliantBaseScore;
                    break;
                case GOLD:
                    baseScore = mCoefficients.goldBaseScore;
                    break;
                case SILVER:
                    baseScore = mCoefficients.silver1BaseScore;
                    break;
                case SILVER2:
                    baseScore = mCoefficients.silver2BaseScore;
                    break;
                case FREE:
                    baseScore = mCoefficients.freeBaseScore;
                    break;
                default:
                    break;
            }

            data.mBuyScore = baseScore*data.mTotalCount;

            addPanel(data);
        }

        // сортируем пазлы в архиве;
        for(PuzzleSet set : sets)
        {
            CrosswordSetData data = extractCrosswordPanelData(set);
            String key = formatTime(data.mYear, data.mMonth);
            @Nonnull CrosswordSetMonth crosswordSetMonth = getCrosswordSetMonth(key);
            if(crosswordSetMonth != null)
            {
                crosswordSetMonth.setSortSets();
            }
        }
    }

    private void addPanel(@Nonnull CrosswordSetData data)
    {
        String key = formatTime(data.mYear, data.mMonth);
        @Nonnull CrosswordSetMonth crosswordSetMonth = getCrosswordSetMonth(key);
        @Nullable CrosswordSet crosswordSet = crosswordSetMonth.getCrosswordSet(data.mId);
        if(crosswordSet == null)
        {
            crosswordSet = new CrosswordSet(mContext, mICrosswordFragment);
            crosswordSet.fillPanel(data);
            crosswordSetMonth.addCrosswordSet(crosswordSet);
        }
        else
        {
            crosswordSet.fillPanel(data);
        }

        if (!mListCrosswordSetMonth.containsKey(key))
        {
            mListCrosswordSetMonth.put(key, crosswordSetMonth);
            if(crosswordSet.getCrosswordSetType() == CrosswordSet.CrosswordSetType.CURRENT)
            {
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutBrilliant);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutGold);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver2);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutFree);
            }
            else
            {
                if(data.mBought)
                {
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutBrilliant);
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutGold);
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver);
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver2);
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutFree);
                }
            }
        }

    }

    private void addBadge(@Nonnull Puzzle puzzle)
    {
        if (puzzle == null) return;
        BadgeData data = new BadgeData();
        data.mId = puzzle.id;
        data.mServerId = puzzle.serverId;
        data.mStatus = puzzle.isSolved;
        data.mScore = puzzle.score;
        data.mProgress = puzzle.solvedPercent;
        data.mSetId = puzzle.setId;

        for(CrosswordSetMonth crosswordSetMonths : mListCrosswordSetMonth.values())
        {
            CrosswordSet crosswordSet = crosswordSetMonths.getCrosswordSet(data.mSetId);
            if(crosswordSet == null)
                continue;
            BadgeAdapter adapter = crosswordSet.getAdapter();
            adapter.addBadgeData(data);
            adapter.notifyDataSetChanged();
            break;
        }
    }

}