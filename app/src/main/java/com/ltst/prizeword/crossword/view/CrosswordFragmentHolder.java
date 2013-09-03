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
import java.util.List;

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
    private @Nonnull HashMap<Integer, CrosswordSetMonth> mListCrosswordSetMonth;

    @Nonnull HashMap<String, List<PuzzleSet>> mMapSets;
    @Nonnull HashMap<String, List<Puzzle>> mMapPuzzles;


    public CrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                   @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mICrosswordFragment = (ICrosswordFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mMapSets = new HashMap<String, List<PuzzleSet>>();
        mMapPuzzles = new HashMap<String, List<Puzzle>>();

        mListCrosswordSet = new HashMap<Long, CrosswordSet>();
        mListCrosswordSetMonth = new HashMap<Integer, CrosswordSetMonth>();
        
        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);
        mCrosswordPanelArchive = new CrosswordPanelArchiveHolder(view);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int monthMaxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int scopeDays = Integer.valueOf(mContext.getResources().getString(R.string.puzzless_rest_scope_days));
        int restDays = monthMaxDays - day+1;

//        DateFormatSymbols symbols = new DateFormatSymbols();
//        mCrosswordPanelCurrent.mMonthTV.setText(symbols.getMonths()[month]);

        mCrosswordPanelCurrent.mMonthTV.setText(
                mContext.getResources().getStringArray(R.array.menu_group_months_at_imenit_padezh)[month]);

        mCrosswordPanelCurrent.mRestDaysTV.setText(String.valueOf(restDays));
        mCrosswordPanelCurrent.mRestPanelLL.setBackgroundDrawable(
                mContext.getResources().getDrawable(restDays > scopeDays
                ? R.drawable.puzzles_current_puzzles_head_rest_panel_nocritical
                : R.drawable.puzzles_current_puzzles_head_rest_panel_clitical));

    }

    @Nonnull
    public HashMap<String, List<PuzzleSet>> getMapSets() {
        return mMapSets;
    }

    @Nonnull
    public HashMap<String, List<Puzzle>> getMapPuzzles() {
        return mMapPuzzles;
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
        return (id >= 0 && mListCrosswordSet.containsKey(id)) ? mListCrosswordSet.get(id) : new CrosswordSet(mContext,mICrosswordFragment);
    }

    private CrosswordSetMonth getCrosswordSetMonth(int month)
    {
        return (month >= 0 && mListCrosswordSetMonth.containsKey(month)) ? mListCrosswordSetMonth.get(month) : new CrosswordSetMonth(mContext);
    }

    // ================== CROSSWORD PANELS ITEM ======================

    public CrosswordPanelData extractCrosswordPanelData(@Nonnull PuzzleSet set)
    {
        CrosswordPanelData data = new CrosswordPanelData();
        data.mId = set.id;
        data.mServerId = set.serverId;
        data.mKind = CrosswordPanelData.KIND_CURRENT;
        data.mType = PuzzleSetModel.getPuzzleTypeByString(set.type);
        data.mBought = set.isBought;
        data.mMonth = set.month;
        data.mYear = set.year;
        data.mScore = 0;
        data.mProgress = 0;
        data.mTotalCount = 0;
        data.mTotalCount = 0;
        return  data;
    }

    public void fillSet(@Nonnull List<PuzzleSet> sets, @Nonnull HashMap<String, List<Puzzle>> mapPuzzles)
    {
        if(!sets.isEmpty())
        {
            PuzzleSet set = sets.get(0);
            String key = String.format("%d-%d",set.year,set.month);
            if(mMapSets.containsKey(key))
                mMapSets.remove(key);
            mMapSets.put(key, sets);
        }

        for (String key : mapPuzzles.keySet())
        {
            if(mMapPuzzles.containsKey(key))
                mMapPuzzles.remove(key);
            mMapPuzzles.put(key, mapPuzzles.get(key));
        }

        for (PuzzleSet set : sets)
        {
            CrosswordPanelData data = extractCrosswordPanelData(set);
            addPanel(data);
            @Nonnull List<Puzzle> puzzles = mapPuzzles.get(set.serverId);
            int solved = 0;
            int scores = 0;
            int percents = 0;
            for(@Nonnull Puzzle puzzle : puzzles)
            {
                addBadge(puzzle);
                percents+=puzzle.solvedPercent;
                scores+=puzzle.score;
                if(puzzle.isSolved)
                    solved++;
            }
            if(puzzles.size() != 0)
            {
                percents = percents/puzzles.size();
            }
            data.mScore = scores;
            data.mProgress = percents;
            data.mResolveCount = solved;
            data.mTotalCount = puzzles.size();
            addPanel(data);
        }
    }

    private void addPanel(@Nonnull CrosswordPanelData data)
    {
        @Nonnull CrosswordSetMonth crosswordSetMonth = getCrosswordSetMonth(data.mMonth);
        @Nonnull CrosswordSet crosswordSet = getCrosswordSet(data.mId);
        crosswordSet.fillPanel(data);
        if(!mListCrosswordSet.containsKey(data.mId))
        {
            mListCrosswordSet.put(data.mId, crosswordSet);
            crosswordSetMonth.addCrosswordSet(data.mType, crosswordSet);
        }
        if(!mListCrosswordSetMonth.containsKey(data.mMonth))
        {
            mListCrosswordSetMonth.put(data.mMonth, crosswordSetMonth);
            if(crosswordSet.getCrosswordSetType() == CrosswordSet.CrosswordSetType.CURRENT){
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth);
            }
            else{
                if(data.mBought){
                    mCrosswordPanelArchive.mCrosswordsContainerLL.addView(crosswordSetMonth);
                }
            }
        }
    }

    private void addBadge(@Nonnull Puzzle puzzle){
        if(puzzle == null) return;
        BadgeData data = new BadgeData();
        data.mId = puzzle.id;
        data.mServerId = puzzle.serverId;
        data.mStatus = puzzle.isSolved;
        data.mScore = puzzle.score;
        data.mProgress = puzzle.solvedPercent;
        data.mSetId = puzzle.setId;
        CrosswordSet crosswordSet = getCrosswordSet(data.mSetId);
        BadgeAdapter adapter = crosswordSet.getAdapter();
        adapter.addBadgeData(data);
        adapter.notifyDataSetChanged();
    }

}