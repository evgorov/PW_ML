package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.view.LayoutInflater;

import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 21.08.13.
 */
public class CrosswordSetHolder {

    private @Nonnull ICrosswordFragment mICrosswordFragment;
    private @Nonnull Context mContext;
    private @Nonnull LayoutInflater mInflater;
    private @Nonnull CrosswordSet mCrosswordSetBrilliant;
    private @Nonnull CrosswordSet mCrosswordSetGold;
    private @Nonnull CrosswordSet mCrosswordSetSilver;
    private @Nonnull CrosswordSet mCrosswordSetSilver2;
    private @Nonnull CrosswordSet mCrosswordSetFree;

    public CrosswordSetHolder(@Nonnull ICrosswordFragment iCrosswordFragment, @Nonnull Context context, @Nonnull LayoutInflater inflater) {
        this.mICrosswordFragment = iCrosswordFragment;
        this.mContext = context;
        this.mInflater = inflater;

        mCrosswordSetBrilliant = new CrosswordSet(mContext, mInflater, mICrosswordFragment);
        mCrosswordSetGold = new CrosswordSet(mContext, mInflater, mICrosswordFragment);
        mCrosswordSetSilver = new CrosswordSet(mContext, mInflater, mICrosswordFragment);
        mCrosswordSetSilver2 = new CrosswordSet(mContext, mInflater, mICrosswordFragment);
        mCrosswordSetFree = new CrosswordSet(mContext, mInflater, mICrosswordFragment);
    }

    public void appendSetData(@Nonnull CrosswordPanelData data){
        if(data.mType == PuzzleSetModel.PuzzleSetType.BRILLIANT){
            mCrosswordSetBrilliant.fillPanel(data);
        }
        else if(data.mType == PuzzleSetModel.PuzzleSetType.GOLD){
            mCrosswordSetGold.fillPanel(data);
        }
        else if(data.mType == PuzzleSetModel.PuzzleSetType.SILVER){
            mCrosswordSetSilver.fillPanel(data);
        }
        else if(data.mType == PuzzleSetModel.PuzzleSetType.SILVER2){
            mCrosswordSetSilver2.fillPanel(data);
        }
        else if(data.mType == PuzzleSetModel.PuzzleSetType.FREE){
            mCrosswordSetFree.fillPanel(data);
        }
    }
}
