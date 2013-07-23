package com.ltst.prizeword.dowloading;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;
import com.ltst.prizeword.dowloading.IImagesDownloadingClient;
import com.ltst.prizeword.rest.RestImg;

import javax.annotation.Nonnull;

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
    public Bundle execute(DbService.DbTaskEnv env) {

        Bundle bundle = env.extras;
        String imageUrl = (bundle == null ? null : bundle.getString(BF_IMAGEID));

        if(imageUrl != null)
        {
            IImagesDownloadingClient client = RestImg.createImagesClient();
            byte[] buffer = client.getImage(imageUrl);

            if(buffer != null)
            {
//                saveImageToDb(env, imageUrl, buffer);
                Bundle result = new Bundle();
                result.putByteArray(BF_BITMAP, buffer);
                return result;
            }
        }
        return null;
    }
}
