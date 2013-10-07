package com.ltst.przwrd.crossword.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ltst.przwrd.crossword.model.PuzzleSetModel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSetMonth{

    private @Nonnull Context mContext;

    public @Nonnull LinearLayout mLinearLayoutBrilliant;
    public @Nonnull LinearLayout mLinearLayoutGold;
    public @Nonnull LinearLayout mLinearLayoutSilver;
    public @Nonnull LinearLayout mLinearLayoutSilver2;
    public @Nonnull LinearLayout mLinearLayoutFree;

    private @Nonnull List<CrosswordSet> mCrosswordDatas;

    public CrosswordSetMonth(@Nonnull Context context) {
        this.mContext = context;

        mCrosswordDatas = new ArrayList<CrosswordSet>();

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayoutBrilliant = new LinearLayout(mContext, null);
        mLinearLayoutBrilliant.setLayoutParams(lp);
        mLinearLayoutBrilliant.setVisibility(View.GONE);
        mLinearLayoutBrilliant.setOrientation(LinearLayout.VERTICAL);
        mLinearLayoutGold = new LinearLayout(mContext, null);
        mLinearLayoutGold.setLayoutParams(lp);
        mLinearLayoutGold.setVisibility(View.GONE);
        mLinearLayoutGold.setOrientation(LinearLayout.VERTICAL);
        mLinearLayoutSilver = new LinearLayout(mContext, null);
        mLinearLayoutSilver.setVisibility(View.GONE);
        mLinearLayoutSilver.setLayoutParams(lp);
        mLinearLayoutSilver.setOrientation(LinearLayout.VERTICAL);
        mLinearLayoutSilver2 = new LinearLayout(mContext, null);
        mLinearLayoutSilver2.setLayoutParams(lp);
        mLinearLayoutSilver2.setVisibility(View.GONE);
        mLinearLayoutSilver2.setOrientation(LinearLayout.VERTICAL);
        mLinearLayoutFree = new LinearLayout(mContext, null);
        mLinearLayoutFree.setLayoutParams(lp);
        mLinearLayoutFree.setVisibility(View.GONE);
        mLinearLayoutFree.setOrientation(LinearLayout.VERTICAL);
    }

    public void addCrosswordSet(@Nonnull CrosswordSet view)
    {
        @Nonnull CrosswordSetData data = view.getCrosswordSetData();
        @Nonnull PuzzleSetModel.PuzzleSetType type = data.mType;

        if(type == PuzzleSetModel.PuzzleSetType.BRILLIANT){
            mLinearLayoutBrilliant.addView(view.getView());
            mLinearLayoutBrilliant.setVisibility(View.VISIBLE);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.GOLD){
            mLinearLayoutGold.addView(view.getView());
            mLinearLayoutGold.setVisibility(View.VISIBLE);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.SILVER){
            mLinearLayoutSilver.addView(view.getView());
            mLinearLayoutSilver.setVisibility(View.VISIBLE);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.SILVER2){
            mLinearLayoutSilver2.addView(view.getView());
            mLinearLayoutSilver2.setVisibility(View.VISIBLE);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.FREE){
            mLinearLayoutFree.addView(view.getView());
            mLinearLayoutFree.setVisibility(View.VISIBLE);
        }
        mCrosswordDatas.add(view);
    }

    private @Nullable LinearLayout getMonthContainer(PuzzleSetModel.PuzzleSetType type)
    {
        switch (type)
        {
            case BRILLIANT: return mLinearLayoutBrilliant;
            case GOLD: return mLinearLayoutGold;
            case SILVER: return mLinearLayoutSilver;
            case SILVER2: return mLinearLayoutSilver2;
            case FREE: return mLinearLayoutFree;
            default: break;
        }
        return null;
    }

    public CrosswordSet getCrosswordSet(long id)
    {
        for(CrosswordSet crosswordSet : mCrosswordDatas)
        {
            if(crosswordSet.getCrosswordSetData().mId == id)
            {
                return crosswordSet;
            }
        }
        return null;
    }

    public void setSortSets()
    {
        boolean visability = true;
        for(PuzzleSetModel.PuzzleSetType type : PuzzleSetModel.PuzzleSetType.values())
        {
            for(CrosswordSet crosswordSet : mCrosswordDatas)
            {
                if(crosswordSet.getCrosswordSetData().mType != type
                        || crosswordSet.getCrosswordSetType() == CrosswordSet.CrosswordSetType.CURRENT)
                    continue;

                if(crosswordSet.getView().getVisibility() != View.VISIBLE)
                {
                    crosswordSet.setVisibleMonth(false);
                }
                else
                {
                    crosswordSet.setVisibleMonth(visability);
                    if(visability)
                    {
                        visability = false;
                    }
                }
            }
        }
    }

    public boolean emptyCrosswordDatas()
    {
        return mCrosswordDatas.isEmpty();
    }

}
