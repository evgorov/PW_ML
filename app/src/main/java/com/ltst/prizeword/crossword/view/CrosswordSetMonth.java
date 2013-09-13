package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.R;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSetMonth{

    private @Nonnull Context mContext;
    private @Nonnull LayoutInflater mInflater;

    public @Nonnull LinearLayout mLinearLayoutBrilliant;
    public @Nonnull LinearLayout mLinearLayoutGold;
    public @Nonnull LinearLayout mLinearLayoutSilver;
    public @Nonnull LinearLayout mLinearLayoutSilver2;
    public @Nonnull LinearLayout mLinearLayoutFree;

    private @Nonnull HashMap<PuzzleSetModel.PuzzleSetType, CrosswordSet> mCrosswordDatas;

    public CrosswordSetMonth(@Nonnull Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCrosswordDatas = new HashMap<PuzzleSetModel.PuzzleSetType, CrosswordSet>(5);

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

    public void addCrosswordSet(@Nonnull CrosswordPanelData data, @Nonnull CrosswordSet view){
        @Nonnull PuzzleSetModel.PuzzleSetType type = data.mType;
        mCrosswordDatas.put(type, view);

        if(type == PuzzleSetModel.PuzzleSetType.BRILLIANT){
            mLinearLayoutBrilliant.addView(view.getView());
            mLinearLayoutBrilliant.setVisibility(View.VISIBLE);
            view.setVisibleMonth(false);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.GOLD){
            mLinearLayoutGold.addView(view.getView());
            mLinearLayoutGold.setVisibility(View.VISIBLE);
            view.setVisibleMonth(false);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.SILVER){
            mLinearLayoutSilver.addView(view.getView());
            mLinearLayoutSilver.setVisibility(View.VISIBLE);
            view.setVisibleMonth(false);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.SILVER2){
            mLinearLayoutSilver2.addView(view.getView());
            mLinearLayoutSilver2.setVisibility(View.VISIBLE);
            view.setVisibleMonth(false);
        }
        else if(type == PuzzleSetModel.PuzzleSetType.FREE){
            mLinearLayoutFree.addView(view.getView());
            mLinearLayoutFree.setVisibility(View.VISIBLE);
            view.setVisibleMonth(false);
        }
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

    public void setSortSets()
    {
        boolean visability = true;
        for(PuzzleSetModel.PuzzleSetType type : PuzzleSetModel.PuzzleSetType.values())
        {
            CrosswordSet crosswordSet = mCrosswordDatas.get(type);
            if(crosswordSet == null)
                continue;
            if(crosswordSet.getCrosswordSetType() == CrosswordSet.CrosswordSetType.CURRENT)
                return;
            LinearLayout layout = getMonthContainer(type);
            if(layout == null)
                continue;
            if(layout.getVisibility() == View.GONE)
            {
                crosswordSet.setVisibleMonth(false);
            }
            else
            {
                crosswordSet.setVisibleMonth(visability);
                if(visability)
                    visability = false;
            }
        }
    }


}
