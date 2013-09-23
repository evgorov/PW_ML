package com.ltst.prizeword.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ltst.prizeword.R;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 27.07.13.
 */
public class ChoiceImageSourceHolder extends Dialog {

    public @Nonnull Button mCameraButton;
    public @Nonnull Button mGalleryButton;
    private Context mContext;

    public ChoiceImageSourceHolder(Context context) {
        super(context);
        this.mContext = context;
        Resources res = context.getResources();
        setContentView(R.layout.choice_photo_dialog_layout);
        setTitle(res.getString(R.string.choice_source));
        mCameraButton = ((Button) findViewById(R.id.choice_photo_dialog_camera_btn));
        mGalleryButton = ((Button) findViewById(R.id.choice_photo_dialog_gallery_btn));
        getWindow().setLayout((int) res.getDimension(R.dimen.choise_photo_dialog_width), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void show() {
        super.show();
        if(!BcTaskHelper.isNetworkAvailable(mContext))
        {
            Toast.makeText(mContext, NonnullableCasts.getStringOrEmpty(
                    mContext.getString(R.string.msg_no_internet)), Toast.LENGTH_LONG).show();
            mCameraButton.setEnabled(false);
            mGalleryButton.setEnabled(false);
        }
        else
        {
            mCameraButton.setEnabled(true);
            mGalleryButton.setEnabled(true);
        }
    }
}
