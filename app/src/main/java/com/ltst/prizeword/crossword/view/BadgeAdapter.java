package com.ltst.prizeword.crossword.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.crossword.model.PuzzleSetModel;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 13.08.13.
 */
public class BadgeAdapter extends BaseAdapter {

    private @Nonnull Context mContext;
    private @Nonnull List<BadgeData> mData;
    private @Nonnull PuzzleSetModel.PuzzleSetType mType;

    public BadgeAdapter(@Nonnull Context context, @Nonnull PuzzleSetModel.PuzzleSetType type) {
        this.mContext = context;
        this.mData = new ArrayList<BadgeData>();
        this.mType = type;
    }

    public void addBadgeData(@Nonnull BadgeData data)
    {
        boolean flag = false;
        for(int i=0; i<mData.size(); i++)
        {
            BadgeData bd = mData.get(i);
            if(bd.mServerId.equals(data.mServerId))
            {
                flag = true;
                mData.remove(i);
                mData.add(i,data);
                break;
            }
        }
        if(!flag){
            mData.add(data);
        }
    }

    @Nullable
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        // Выбираем фон эмблемы;
        @Nonnull BadgeData data = mData.get(position);
        @Nonnull BadgeHolder badge = new BadgeHolder(mContext, view);

        int idBackground = 0;
        if(mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
                idBackground = R.drawable.puzzles_badges_bg_brilliant;
        else if(mType == PuzzleSetModel.PuzzleSetType.GOLD)
                idBackground = R.drawable.puzzles_badges_bg_gold;
        else if(mType == PuzzleSetModel.PuzzleSetType.SILVER)
                idBackground = R.drawable.puzzles_badges_bg_silver;
        else if(mType == PuzzleSetModel.PuzzleSetType.FREE)
                idBackground = R.drawable.puzzles_badges_bg_free;
        else
            idBackground = R.drawable.puzzles_badges_bg_free;

        // Выбираем фасад эмблемы;
        int idForeground = 0;
        if(data.mStatus)
        {
            if(mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
                    idForeground = R.drawable.puzzles_badges_bg_brilliant_resolved;
            else if(mType == PuzzleSetModel.PuzzleSetType.GOLD)
                    idForeground = R.drawable.puzzles_badges_bg_gold_resolved;
            else if(mType == PuzzleSetModel.PuzzleSetType.SILVER)
                    idForeground = R.drawable.puzzles_badges_bg_silver_resolved;
            else if(mType == PuzzleSetModel.PuzzleSetType.FREE)
                    idForeground = R.drawable.puzzles_badges_bg_unresolved;
        }
        else
        {
            idForeground = R.drawable.puzzles_badges_bg_unresolved;
        }

        // Выбираем цифорку;
        int idNumber = 0;
        if(mType == PuzzleSetModel.PuzzleSetType.BRILLIANT)
        {
            switch (position+1)
            {
                case 1:  idNumber = R.drawable.crossword_number_brilliant_1; break;
                case 2:  idNumber = R.drawable.crossword_number_brilliant_2; break;
                case 3:  idNumber = R.drawable.crossword_number_brilliant_3; break;
                case 4:  idNumber = R.drawable.crossword_number_brilliant_4; break;
                case 5:  idNumber = R.drawable.crossword_number_brilliant_5; break;
                case 6:  idNumber = R.drawable.crossword_number_brilliant_6; break;
                case 7:  idNumber = R.drawable.crossword_number_brilliant_7; break;
                case 8:  idNumber = R.drawable.crossword_number_brilliant_8; break;
                case 9:  idNumber = R.drawable.crossword_number_brilliant_9; break;
                case 10: idNumber = R.drawable.crossword_number_brilliant_10; break;
                case 11: idNumber = R.drawable.crossword_number_brilliant_11; break;
                case 12: idNumber = R.drawable.crossword_number_brilliant_12; break;
                case 13: idNumber = R.drawable.crossword_number_brilliant_13; break;
                case 14: idNumber = R.drawable.crossword_number_brilliant_14; break;
                case 15: idNumber = R.drawable.crossword_number_brilliant_15; break;
                case 16: idNumber = R.drawable.crossword_number_brilliant_16; break;
                case 17: idNumber = R.drawable.crossword_number_brilliant_17; break;
                case 18: idNumber = R.drawable.crossword_number_brilliant_18; break;
                case 19: idNumber = R.drawable.crossword_number_brilliant_19; break;
                case 20: idNumber = R.drawable.crossword_number_brilliant_20; break;
                case 21: idNumber = R.drawable.crossword_number_brilliant_21; break;
                case 22: idNumber = R.drawable.crossword_number_brilliant_22; break;
                case 23: idNumber = R.drawable.crossword_number_brilliant_23; break;
                case 24: idNumber = R.drawable.crossword_number_brilliant_24; break;
                case 25: idNumber = R.drawable.crossword_number_brilliant_25; break;
                case 26: idNumber = R.drawable.crossword_number_brilliant_26; break;
                case 27: idNumber = R.drawable.crossword_number_brilliant_27; break;
                case 28: idNumber = R.drawable.crossword_number_brilliant_28; break;
                case 29: idNumber = R.drawable.crossword_number_brilliant_29; break;
                case 30: idNumber = R.drawable.crossword_number_brilliant_30; break;
                case 31: idNumber = R.drawable.crossword_number_brilliant_31; break;
                case 32: idNumber = R.drawable.crossword_number_brilliant_32; break;
                default: idNumber = R.drawable.crossword_number_brilliant_32; break;
            }
        }
        else if(mType == PuzzleSetModel.PuzzleSetType.GOLD)
        {
            switch (position+1)
            {
                case 1:  idNumber = R.drawable.crossword_number_gold_1; break;
                case 2:  idNumber = R.drawable.crossword_number_gold_2; break;
                case 3:  idNumber = R.drawable.crossword_number_gold_3; break;
                case 4:  idNumber = R.drawable.crossword_number_gold_4; break;
                case 5:  idNumber = R.drawable.crossword_number_gold_5; break;
                case 6:  idNumber = R.drawable.crossword_number_gold_6; break;
                case 7:  idNumber = R.drawable.crossword_number_gold_7; break;
                case 8:  idNumber = R.drawable.crossword_number_gold_8; break;
                case 9:  idNumber = R.drawable.crossword_number_gold_9; break;
                case 10: idNumber = R.drawable.crossword_number_gold_10; break;
                case 11: idNumber = R.drawable.crossword_number_gold_11; break;
                case 12: idNumber = R.drawable.crossword_number_gold_12; break;
                case 13: idNumber = R.drawable.crossword_number_gold_13; break;
                case 14: idNumber = R.drawable.crossword_number_gold_14; break;
                case 15: idNumber = R.drawable.crossword_number_gold_15; break;
                case 16: idNumber = R.drawable.crossword_number_gold_16; break;
                case 17: idNumber = R.drawable.crossword_number_gold_17; break;
                case 18: idNumber = R.drawable.crossword_number_gold_18; break;
                case 19: idNumber = R.drawable.crossword_number_gold_19; break;
                case 20: idNumber = R.drawable.crossword_number_gold_20; break;
                case 21: idNumber = R.drawable.crossword_number_gold_21; break;
                case 22: idNumber = R.drawable.crossword_number_gold_22; break;
                case 23: idNumber = R.drawable.crossword_number_gold_23; break;
                case 24: idNumber = R.drawable.crossword_number_gold_24; break;
                case 25: idNumber = R.drawable.crossword_number_gold_25; break;
                case 26: idNumber = R.drawable.crossword_number_gold_26; break;
                case 27: idNumber = R.drawable.crossword_number_gold_27; break;
                case 28: idNumber = R.drawable.crossword_number_gold_28; break;
                case 29: idNumber = R.drawable.crossword_number_gold_29; break;
                case 30: idNumber = R.drawable.crossword_number_gold_30; break;
                case 31: idNumber = R.drawable.crossword_number_gold_31; break;
                case 32: idNumber = R.drawable.crossword_number_gold_32; break;
                default: idNumber = R.drawable.crossword_number_gold_32; break;
            }
        }
        else if(mType == PuzzleSetModel.PuzzleSetType.SILVER)
        {
            switch (position+1)
            {
                case 1:  idNumber = R.drawable.crossword_number_silver_1; break;
                case 2:  idNumber = R.drawable.crossword_number_silver_2; break;
                case 3:  idNumber = R.drawable.crossword_number_silver_3; break;
                case 4:  idNumber = R.drawable.crossword_number_silver_4; break;
                case 5:  idNumber = R.drawable.crossword_number_silver_5; break;
                case 6:  idNumber = R.drawable.crossword_number_silver_6; break;
                case 7:  idNumber = R.drawable.crossword_number_silver_7; break;
                case 8:  idNumber = R.drawable.crossword_number_silver_8; break;
                case 9:  idNumber = R.drawable.crossword_number_silver_9; break;
                case 10: idNumber = R.drawable.crossword_number_silver_10; break;
                case 11: idNumber = R.drawable.crossword_number_silver_11; break;
                case 12: idNumber = R.drawable.crossword_number_silver_12; break;
                case 13: idNumber = R.drawable.crossword_number_silver_13; break;
                case 14: idNumber = R.drawable.crossword_number_silver_14; break;
                case 15: idNumber = R.drawable.crossword_number_silver_15; break;
                case 16: idNumber = R.drawable.crossword_number_silver_16; break;
                case 17: idNumber = R.drawable.crossword_number_silver_17; break;
                case 18: idNumber = R.drawable.crossword_number_silver_18; break;
                case 19: idNumber = R.drawable.crossword_number_silver_19; break;
                case 20: idNumber = R.drawable.crossword_number_silver_20; break;
                case 21: idNumber = R.drawable.crossword_number_silver_21; break;
                case 22: idNumber = R.drawable.crossword_number_silver_22; break;
                case 23: idNumber = R.drawable.crossword_number_silver_23; break;
                case 24: idNumber = R.drawable.crossword_number_silver_24; break;
                case 25: idNumber = R.drawable.crossword_number_silver_25; break;
                case 26: idNumber = R.drawable.crossword_number_silver_26; break;
                case 27: idNumber = R.drawable.crossword_number_silver_27; break;
                case 28: idNumber = R.drawable.crossword_number_silver_28; break;
                case 29: idNumber = R.drawable.crossword_number_silver_29; break;
                case 30: idNumber = R.drawable.crossword_number_silver_30; break;
                case 31: idNumber = R.drawable.crossword_number_silver_31; break;
                case 32: idNumber = R.drawable.crossword_number_silver_32; break;
                default: idNumber = R.drawable.crossword_number_silver_32; break;
            }
        }
        else if(mType == PuzzleSetModel.PuzzleSetType.FREE)
        {
            switch (position+1)
            {
                case 1:  idNumber = R.drawable.crossword_number_free_1; break;
                case 2:  idNumber = R.drawable.crossword_number_free_2; break;
                case 3:  idNumber = R.drawable.crossword_number_free_3; break;
                case 4:  idNumber = R.drawable.crossword_number_free_4; break;
                case 5:  idNumber = R.drawable.crossword_number_free_5; break;
                case 6:  idNumber = R.drawable.crossword_number_free_6; break;
                case 7:  idNumber = R.drawable.crossword_number_free_7; break;
                case 8:  idNumber = R.drawable.crossword_number_free_8; break;
                case 9:  idNumber = R.drawable.crossword_number_free_9; break;
                case 10: idNumber = R.drawable.crossword_number_free_10; break;
                case 11: idNumber = R.drawable.crossword_number_free_11; break;
                case 12: idNumber = R.drawable.crossword_number_free_12; break;
                case 13: idNumber = R.drawable.crossword_number_free_13; break;
                case 14: idNumber = R.drawable.crossword_number_free_14; break;
                case 15: idNumber = R.drawable.crossword_number_free_15; break;
                case 16: idNumber = R.drawable.crossword_number_free_16; break;
                case 17: idNumber = R.drawable.crossword_number_free_17; break;
                case 18: idNumber = R.drawable.crossword_number_free_18; break;
                case 19: idNumber = R.drawable.crossword_number_free_19; break;
                case 20: idNumber = R.drawable.crossword_number_free_20; break;
                case 21: idNumber = R.drawable.crossword_number_free_21; break;
                case 22: idNumber = R.drawable.crossword_number_free_22; break;
                case 23: idNumber = R.drawable.crossword_number_free_23; break;
                case 24: idNumber = R.drawable.crossword_number_free_24; break;
                case 25: idNumber = R.drawable.crossword_number_free_25; break;
                case 26: idNumber = R.drawable.crossword_number_free_26; break;
                case 27: idNumber = R.drawable.crossword_number_free_27; break;
                case 28: idNumber = R.drawable.crossword_number_free_28; break;
                case 29: idNumber = R.drawable.crossword_number_free_29; break;
                case 30: idNumber = R.drawable.crossword_number_free_30; break;
                case 31: idNumber = R.drawable.crossword_number_free_31; break;
                case 32: idNumber = R.drawable.crossword_number_free_32; break;
                default: idNumber = R.drawable.crossword_number_free_32; break;
            }
        }

        badge.mPercent.setText(String.valueOf(data.mProgress)+"%");
        badge.mScore.setText(String.valueOf(data.mScore));
        badge.mBackground.setBackgroundDrawable(mContext.getResources().getDrawable(idBackground));
        badge.mForegroud.setBackgroundDrawable(mContext.getResources().getDrawable(idForeground));
        badge.mNumber.setBackgroundDrawable(mContext.getResources().getDrawable(idNumber));

        if(data.mStatus)
        {
            // Решенные;
            badge.mUnresolverContainer.setVisibility(View.GONE);
            badge.mResolverContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            // Нерешенные;
            badge.mResolverContainer.setVisibility(View.GONE);
            badge.mUnresolverContainer.setVisibility(View.VISIBLE);
        }

        return badge.mRootView;
    }



    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).mId;
    }

}
