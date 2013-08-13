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

import com.actionbarsherlock.R;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 13.08.13.
 */
public class BadgeAdapter extends BaseAdapter {

    private @Nonnull Context mContext;
    private BadgeData[] mData;

    public BadgeAdapter(@Nonnull Context context, BadgeData[] data) {
        this.mContext = context;
        this.mData = data;
    }

    @Nullable
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        // Выбираем фон эмблемы;
        BadgeData data = mData[position];
        int idBackground = 0;
        switch (data.mType){
            case BadgeData.TYPE_BRILLIANT:
                idBackground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_brilliant;
                break;
            case BadgeData.TYPE_GOLD:
                idBackground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_gold;
                break;
            case BadgeData.TYPE_SILVER:
                idBackground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_silver;
                break;
            case BadgeData.TYPE_FREE:
                idBackground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_free;
                break;
            default:
                break;
        }

        // Выбираем фасад эмблемы;
        int idForeground = 0;
        if(data.mStatus == BadgeData.STATUS_RESOLVED)
        {
            switch (data.mType){
                case BadgeData.TYPE_BRILLIANT:
                    idForeground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_brilliant_resolved;
                    break;
                case BadgeData.TYPE_GOLD:
                    idForeground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_gold_resolved;
                    break;
                case BadgeData.TYPE_SILVER:
                    idForeground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_silver_resolved;
                    break;
                case BadgeData.TYPE_FREE:
                    idForeground = com.ltst.prizeword.R.drawable.puzzles_badges_bg_unresolved;
                    break;
                default:
                    break;
            }
        }
        else if(data.mStatus == BadgeData.STATUS_UNRESOLVED)
        {
            idForeground = R.drawable.puzzles_badges_bg_unresolved;
        }

        // Выбираем цифорку;
        int idNumber = 0;
        if(data.mType == BadgeData.TYPE_BRILLIANT)
        {
            switch (data.mNumber)
            {
                case 1:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_1; break;
                case 2:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_2; break;
                case 3:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_3; break;
                case 4:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_4; break;
                case 5:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_5; break;
                case 6:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_6; break;
                case 7:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_7; break;
                case 8:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_8; break;
                case 9:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_9; break;
                case 10: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_10; break;
                case 11: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_11; break;
                case 12: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_12; break;
                case 13: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_13; break;
                case 14: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_14; break;
                case 15: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_15; break;
                case 16: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_16; break;
                case 17: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_17; break;
                case 18: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_18; break;
                case 19: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_19; break;
                case 20: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_20; break;
                case 21: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_21; break;
                case 22: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_22; break;
                case 23: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_23; break;
                case 24: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_24; break;
                case 25: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_25; break;
                case 26: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_26; break;
                case 27: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_27; break;
                case 28: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_28; break;
                case 29: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_29; break;
                case 30: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_30; break;
                case 31: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_31; break;
                case 32: idNumber = com.ltst.prizeword.R.drawable.crossword_number_brilliant_32; break;
                default: break;
            }
        }
        else if(data.mType == BadgeData.TYPE_GOLD)
        {
            switch (data.mNumber)
            {
                case 1:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_1; break;
                case 2:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_2; break;
                case 3:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_3; break;
                case 4:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_4; break;
                case 5:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_5; break;
                case 6:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_6; break;
                case 7:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_7; break;
                case 8:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_8; break;
                case 9:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_9; break;
                case 10: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_10; break;
                case 11: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_11; break;
                case 12: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_12; break;
                case 13: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_13; break;
                case 14: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_14; break;
                case 15: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_15; break;
                case 16: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_16; break;
                case 17: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_17; break;
                case 18: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_18; break;
                case 19: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_19; break;
                case 20: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_20; break;
                case 21: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_21; break;
                case 22: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_22; break;
                case 23: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_23; break;
                case 24: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_24; break;
                case 25: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_25; break;
                case 26: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_26; break;
                case 27: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_27; break;
                case 28: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_28; break;
                case 29: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_29; break;
                case 30: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_30; break;
                case 31: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_31; break;
                case 32: idNumber = com.ltst.prizeword.R.drawable.crossword_number_gold_32; break;
                default: break;
            }
        }
        else if(data.mType == BadgeData.TYPE_SILVER)
        {
            switch (data.mNumber)
            {
                case 1:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_1; break;
                case 2:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_2; break;
                case 3:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_3; break;
                case 4:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_4; break;
                case 5:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_5; break;
                case 6:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_6; break;
                case 7:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_7; break;
                case 8:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_8; break;
                case 9:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_9; break;
                case 10: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_10; break;
                case 11: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_11; break;
                case 12: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_12; break;
                case 13: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_13; break;
                case 14: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_14; break;
                case 15: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_15; break;
                case 16: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_16; break;
                case 17: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_17; break;
                case 18: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_18; break;
                case 19: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_19; break;
                case 20: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_20; break;
                case 21: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_21; break;
                case 22: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_22; break;
                case 23: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_23; break;
                case 24: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_24; break;
                case 25: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_25; break;
                case 26: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_26; break;
                case 27: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_27; break;
                case 28: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_28; break;
                case 29: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_29; break;
                case 30: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_30; break;
                case 31: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_31; break;
                case 32: idNumber = com.ltst.prizeword.R.drawable.crossword_number_silver_32; break;
                default: break;
            }
        }
        else if(data.mType == BadgeData.TYPE_FREE)
        {
            switch (data.mNumber)
            {
                case 1:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_1; break;
                case 2:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_2; break;
                case 3:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_3; break;
                case 4:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_4; break;
                case 5:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_5; break;
                case 6:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_6; break;
                case 7:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_7; break;
                case 8:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_8; break;
                case 9:  idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_9; break;
                case 10: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_10; break;
                case 11: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_11; break;
                case 12: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_12; break;
                case 13: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_13; break;
                case 14: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_14; break;
                case 15: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_15; break;
                case 16: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_16; break;
                case 17: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_17; break;
                case 18: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_18; break;
                case 19: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_19; break;
                case 20: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_20; break;
                case 21: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_21; break;
                case 22: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_22; break;
                case 23: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_23; break;
                case 24: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_24; break;
                case 25: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_25; break;
                case 26: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_26; break;
                case 27: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_27; break;
                case 28: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_28; break;
                case 29: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_29; break;
                case 30: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_30; break;
                case 31: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_31; break;
                case 32: idNumber = com.ltst.prizeword.R.drawable.crossword_number_free_32; break;
                default: break;
            }
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(view == null)
        {
            if(data.mStatus == BadgeData.STATUS_RESOLVED)
            {
                view = inflater.inflate(R.layout.crossword_badge_resolved, null, false);
                LinearLayout layout_bg = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_brilliant_badge_bg);
                LinearLayout layout_fg = (LinearLayout) view.findViewById(R.id.crossword_badge_resolved_brilliant_number_container);
                TextView score = (TextView) view.findViewById(R.id.crossword_badge_resolved_brilliant_score);

                layout_bg.setBackgroundDrawable(mContext.getResources().getDrawable(idBackground));
                layout_fg.setBackgroundDrawable(mContext.getResources().getDrawable(idForeground));
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), idNumber);

                ImageView image = new ImageView(mContext);
                image.setImageBitmap(bitmap);
                layout_fg.addView(image);
//            bitmap.recycle();

            }
            else if(data.mStatus == BadgeData.STATUS_UNRESOLVED)
            {
                view = inflater.inflate(R.layout.crossword_badge_unresolved, null, false);
                LinearLayout layout_bg = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_badge_bg);
                LinearLayout layout_fg = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_number_container);
                TextView percent = (TextView) view.findViewById(R.id.crossword_badge_unresolved_brilliant_percent);
                LinearLayout layout_progress_bg = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_bg);
                LinearLayout layout_progress_fg = (LinearLayout) view.findViewById(R.id.crossword_badge_unresolved_brilliant_progress_fg);

                layout_bg.setBackgroundDrawable(mContext.getResources().getDrawable(idBackground));
                layout_fg.setBackgroundDrawable(mContext.getResources().getDrawable(idForeground));
                Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), idNumber);

                ImageView image = new ImageView(mContext);
                image.setImageBitmap(bitmap);
                layout_fg.addView(image);
//            bitmap.recycle();
            }
        }

        return view;
    }

    @Override
    public int getCount() {
        return mData.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

}
