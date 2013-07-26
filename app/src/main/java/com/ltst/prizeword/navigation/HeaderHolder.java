package com.ltst.prizeword.navigation;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 26.07.13.
 */
public class HeaderHolder {
    public @Nonnull ImageView imgPhoto;
    public @Nonnull TextView tvNickname;
    public @Nonnull TextView tvRecordtitle;
    public @Nonnull TextView tvPoints;
    public @Nonnull Button btnLogout;

    public HeaderHolder(@Nonnull View v){
        this.imgPhoto = (ImageView) v.findViewById(R.id.header_listview_photo_img);
        this.tvNickname = (TextView) v.findViewById(R.id.header_listview_nickname_tview);
        this.tvPoints = (TextView) v.findViewById(R.id.header_listview_points_tview);
        this.tvRecordtitle = (TextView) v.findViewById(R.id.header_listview_personal_record_tview);
        this.btnLogout = (Button) v.findViewById(R.id.header_listview_logout_btn);
    }
}
