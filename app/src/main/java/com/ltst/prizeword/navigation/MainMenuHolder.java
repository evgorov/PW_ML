package com.ltst.prizeword.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.prizeword.R;

import java.util.Calendar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public class MainMenuHolder {
    private @Nonnull Context mContext;
    public @Nonnull ImageView mImage;
    public @Nonnull TextView mNickname;
    public @Nonnull TextView mHightRecord;

    public @Nonnull View mMyCrossword;
    public @Nonnull View mShowRulesBtn;
    public @Nonnull View mLogoutBtn;

    public @Nonnull TextView mScore;
    public @Nonnull TextView mPosition;

    public @Nonnull TextView mMonth;

    public MainMenuHolder(@Nonnull Context context, @Nonnull View view){
        this.mContext = context;
        this.mImage = (ImageView) view.findViewById(R.id.header_listview_photo_img);
        this.mNickname = (TextView) view.findViewById(R.id.header_listview_nickname_tview);
        this.mHightRecord = (TextView) view.findViewById(R.id.header_listview_points_tview);
        this.mLogoutBtn = (View) view.findViewById(R.id.header_listview_logout_btn);

        this.mMonth = (TextView) view.findViewById(R.id.menu_current_month_txt);
        this.mMyCrossword = (View) view.findViewById(R.id.menu_mypuzzle_btn);
        this.mShowRulesBtn = (View) view.findViewById(R.id.menu_show_rules_btn);
        this.mScore = (TextView) view.findViewById(R.id.menu_pride_score_txt);
        this.mPosition = (TextView) view.findViewById(R.id.menu_pride_position_txt);

        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        mMonth.setText(mContext.getResources().getStringArray(R.array.menu_group_months)[month]);
    }

    public void setImage(@Nullable Bitmap bitmap){
        if(bitmap != null){
//            if(bitmap.hasAlpha())
//                bitmap.setHasAlpha(false);
//            this.mImage.setImageBitmap(bitmap);
            int size = (int) mContext.getResources().getDimension(R.dimen.size_avatar);
            this.mImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, size, size, false));
        } else {
            this.mImage.setImageResource(R.drawable.login_register_ava_btn);
        }

    }

    public void clean(){
        this.mNickname.setText(R.string.user);
        this.mHightRecord.setText(String.valueOf(0));
        this.setImage(null);
    }
}
