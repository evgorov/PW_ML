package com.ltst.przwrd.scoredetail.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.przwrd.R;
import com.ltst.przwrd.crossword.model.Puzzle;
import com.ltst.przwrd.crossword.model.PuzzleSet;
import com.ltst.przwrd.crossword.model.PuzzleSetModel;
import com.ltst.przwrd.crossword.view.BadgeAdapter;
import com.ltst.przwrd.crossword.view.BadgeData;
import com.ltst.przwrd.crossword.view.CrosswordPanelData;
import com.ltst.przwrd.scoredetail.model.ScoreCrosswordSet;
import com.ltst.przwrd.scoredetail.model.ScoreCrosswordSetMonth;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public class ScoreCrosswordFragmentHolder
{

    private static @Nonnull LayoutInflater mInflater;
    private @Nonnull ScoreDetailFragment mScoreDetailFragment;
    private @Nonnull View mViewCrossword;
    private static @Nonnull Context mContext;
    public @Nonnull CrosswordPanelCurrentHolder mCrosswordPanelCurrent;
    private @Nonnull HashMap<Long, ScoreCrosswordSet> mListCrosswordSet;
    private @Nonnull HashMap<Integer, ScoreCrosswordSetMonth> mListCrosswordSetMonth;

    public ScoreCrosswordFragmentHolder(@Nonnull Context context, @Nonnull SherlockFragment fragment,
                                        @Nonnull LayoutInflater inflater, @Nonnull View view)
    {
        this.mInflater = inflater;
        this.mScoreDetailFragment = (ScoreDetailFragment) fragment;
        this.mViewCrossword = view;
        this.mContext = context;

        mListCrosswordSet = new HashMap<Long, ScoreCrosswordSet>();
        mListCrosswordSetMonth = new HashMap<Integer, ScoreCrosswordSetMonth>();
        mCrosswordPanelCurrent = new CrosswordPanelCurrentHolder(view);

    }

    // ================== CROSSWORD PANELS ======================

    static private class CrosswordPanelCurrentHolder
    {

        @Nonnull public LinearLayout mCrosswordsContainerLL;

        public CrosswordPanelCurrentHolder(@Nonnull View view)
        {
            mCrosswordsContainerLL = (LinearLayout) view.findViewById(R.id.score_crossword_fragment_current_container);
        }
    }


    private ScoreCrosswordSet getCrosswordSet(long id)
    {
        return (id >= 0 && mListCrosswordSet.containsKey(id)) ? mListCrosswordSet.get(id) : new ScoreCrosswordSet(mContext);
    }

    private ScoreCrosswordSetMonth getCrosswordSetMonth(int month)
    {
        return (month >= 0 && mListCrosswordSetMonth.containsKey(month)) ? mListCrosswordSetMonth.get(month) : new ScoreCrosswordSetMonth(mContext);
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
        return data;
    }

    public void fillSet(@Nonnull List<PuzzleSet> sets, @Nonnull HashMap<String, List<Puzzle>> mapPuzzles)
    {
        boolean flag = false;
        int count = 0;
        for (PuzzleSet set : sets)
        {
            count++;
            CrosswordPanelData data = extractCrosswordPanelData(set);

            if (count == sets.size())
            {
                data.mLAst = true;
            }
            if (set.isBought)
            {
                addPanel(data);
                if (!flag)
                {
                    data.mFirst = true;
                    flag = true;
                }
                @Nonnull List<Puzzle> puzzles = mapPuzzles.get(set.serverId);
                int solved = 0;
                int scores = 0;
                int percents = 0;
                for (@Nonnull Puzzle puzzle : puzzles)
                {

                    if (puzzle.isSolved)
                    {
                        addBadge(puzzle);
                        percents += puzzle.solvedPercent;
                        scores += puzzle.score;
                        if (puzzle.isSolved)
                            solved++;

                    }
                }
                if (puzzles.size() != 0)
                {
                    percents = percents / puzzles.size();
                    data.mScore = scores;
                    data.mProgress = percents;
                    data.mResolveCount = solved;
                    data.mTotalCount = puzzles.size();
                    addPanel(data);
                }
            }
        }
    }

    private void addPanel(@Nonnull CrosswordPanelData data)
    {
        @Nonnull ScoreCrosswordSetMonth crosswordSetMonth = getCrosswordSetMonth(data.mMonth);
        @Nonnull ScoreCrosswordSet crosswordSet = getCrosswordSet(data.mId);
        crosswordSet.fillPanel(data);
        if (!mListCrosswordSet.containsKey(data.mId))
        {
            mListCrosswordSet.put(data.mId, crosswordSet);
            crosswordSetMonth.addCrosswordSet(data.mType, crosswordSet);
        }
        if (!mListCrosswordSetMonth.containsKey(data.mMonth))
        {
            mListCrosswordSetMonth.put(data.mMonth, crosswordSetMonth);
            if (crosswordSet.getCrosswordSetType() == ScoreCrosswordSet.CrosswordSetType.CURRENT)
            {
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutBrilliant);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutGold);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutSilver2);
                mCrosswordPanelCurrent.mCrosswordsContainerLL.addView(crosswordSetMonth.mLinearLayoutFree);
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
        ScoreCrosswordSet crosswordSet = getCrosswordSet(data.mSetId);
        BadgeAdapter adapter = crosswordSet.getAdapter();
        adapter.addBadgeData(data);
        adapter.notifyDataSetChanged();
    }

}