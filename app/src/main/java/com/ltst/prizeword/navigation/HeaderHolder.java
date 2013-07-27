package com.ltst.prizeword.navigation;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 26.07.13.
 */
public class HeaderHolder {
    public @Nonnull ImageView imgPhoto;
    public @Nonnull TextView tvNickname;
    public @Nonnull TextView tvRecordtitle;
    public @Nonnull TextView tvPoints;
    public @Nonnull Button btnLogout;
    public @Nonnull ProgressBar pbLoading;

    public HeaderHolder(@Nonnull View v){
        this.imgPhoto = (ImageView) v.findViewById(R.id.header_listview_photo_img);
        this.tvNickname = (TextView) v.findViewById(R.id.header_listview_nickname_tview);
        this.tvPoints = (TextView) v.findViewById(R.id.header_listview_points_tview);
        this.tvRecordtitle = (TextView) v.findViewById(R.id.header_listview_personal_record_tview);
        this.btnLogout = (Button) v.findViewById(R.id.header_listview_logout_btn);
        this.pbLoading = (ProgressBar) v.findViewById(R.id.header_listview_progressbar);
        this.pbLoading.setVisibility(ProgressBar.GONE);
    }

    public void setImage(@Nullable Bitmap bitmap){
        if(bitmap != null){
            if(bitmap.hasAlpha())
                bitmap.setHasAlpha(false);
            this.imgPhoto.setImageBitmap(bitmap);
        } else {
            this.imgPhoto.setImageResource(R.drawable.login_register_ava_btn);
        }

    }

    public void clean(){
        this.tvNickname.setText(R.string.user);
        this.tvPoints.setText(String.valueOf(0));
        this.setImage(null);
    }
}
