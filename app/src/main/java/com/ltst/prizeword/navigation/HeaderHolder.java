package com.ltst.prizeword.navigation;

import android.content.Context;
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
    private @Nonnull Context mContext;
    public @Nonnull ImageView imgPhoto;
    public @Nonnull TextView tvNickname;
    public @Nonnull TextView tvRecordtitle;
    public @Nonnull TextView tvPoints;
    public @Nonnull Button btnLogout;

    public HeaderHolder(@Nonnull Context context, @Nonnull View v){
        this.mContext = context;
        this.imgPhoto = (ImageView) v.findViewById(R.id.header_listview_photo_img);
        this.tvNickname = (TextView) v.findViewById(R.id.header_listview_nickname_tview);
        this.tvPoints = (TextView) v.findViewById(R.id.header_listview_points_tview);
        this.tvRecordtitle = (TextView) v.findViewById(R.id.header_listview_personal_record_tview);
        this.btnLogout = (Button) v.findViewById(R.id.header_listview_logout_btn);

    }

    public void setImage(@Nullable Bitmap bitmap){
        if(bitmap != null){
//            if(bitmap.hasAlpha())
//                bitmap.setHasAlpha(false);
//            this.imgPhoto.setImageBitmap(bitmap);
            int size = (int) mContext.getResources().getDimension(R.dimen.size_avatar);
            this.imgPhoto.setImageBitmap(Bitmap.createScaledBitmap(bitmap, size, size, false));
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
