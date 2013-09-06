package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;
import com.ltst.prizeword.scoredetailfragment.model.ScoreCrosswordSet;

import javax.annotation.Nonnull;

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

    public CrosswordSetMonth(@Nonnull Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

    public void addCrosswordSet(@Nonnull PuzzleSetModel.PuzzleSetType type, @Nonnull CrosswordSet view){
        if(type == PuzzleSetModel.PuzzleSetType.BRILLIANT){
            mLinearLayoutBrilliant.addView(view.getView());
            mLinearLayoutBrilliant.setVisibility(View.VISIBLE);
            view.setVisibleMonth(true);
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

}
