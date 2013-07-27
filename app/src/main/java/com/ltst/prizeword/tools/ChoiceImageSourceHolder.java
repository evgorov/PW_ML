package com.ltst.prizeword.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 27.07.13.
 */
public class ChoiceImageSourceHolder extends Dialog {

    public @Nonnull Button mCameraButton;
    public @Nonnull Button mGalleryButton;

    public ChoiceImageSourceHolder(Context context) {
        super(context);

        Resources res = context.getResources();
        setContentView(R.layout.choice_photo_dialog_layout);
        setTitle(res.getString(R.string.choice_source));
        mCameraButton = ((Button) findViewById(R.id.choice_photo_dialog_camera_btn));
        mGalleryButton = ((Button) findViewById(R.id.choice_photo_dialog_gallery_btn));
        getWindow().setLayout((int) res.getDimension(R.dimen.choise_photo_dialog_width), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

}
