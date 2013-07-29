package com.ltst.prizeword.dowloading;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.rest.RestImg;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 23.07.13.
 */
public class LoadImageTask implements DbService.IDbTask {

    public static final String BF_BITMAP   = "LoadImageTask.Bitmap"; //$NON-NLS-1$
    public static final String BF_IMAGEID  = "LoadImageTask.ImageId"; //$NON-NLS-1$

    public static @Nonnull android.content.Intent createIntent(@Nonnull String imageId)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_IMAGEID, imageId);
        return intent;
    }

    @Override
    public Bundle execute(DbService.DbTaskEnv env)
    {
        Bundle extras = env.extras;
        if(extras == null)
            return null;

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            String imageUrl = (extras == null ? null : extras.getString(BF_IMAGEID));
            if(imageUrl != null)
            {
                IImagesDownloadingClient client = RestImg.createImagesClient();
                byte[] buffer = client.getImage(imageUrl);

                if(buffer != null)
                {
//                saveImageToDb(env, imageUrl, buffer);
                    return getLoadingImage(buffer);
                }
            }
        }
        return null;
    }

    private static @Nullable Bundle getLoadingImage(@Nullable byte[] buffer)
    {
        Bundle bundle = new Bundle();
        bundle.putByteArray(BF_BITMAP, buffer);
        return bundle;
    }

}