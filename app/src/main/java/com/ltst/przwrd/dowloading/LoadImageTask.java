package com.ltst.przwrd.dowloading;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.ltst.przwrd.R;
import com.ltst.przwrd.db.DbService;
import com.ltst.przwrd.login.model.LoadUserDataFromDataBase;
import com.ltst.przwrd.rest.RestImg;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.errors.OmOutOfMemoryException;

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
    public static @Nullable Bitmap extractBitmapFromResult(@Nullable Bundle taskResult)
            throws OmOutOfMemoryException
    {
        byte[] png = taskResult == null ? null : taskResult.getByteArray(BF_BITMAP);

        if(png == null)
            return null;
        try
        {
            return BitmapFactory.decodeByteArray(png, 0, png.length);
        }
        catch(OutOfMemoryError e)
        {
            throw new OmOutOfMemoryException("Can't decode Bitmap", e); //$NON-NLS-1$
        }
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
                    env.dbw.putUserImage(buffer);
                    return LoadUserDataFromDataBase.getUserImageFromDB(env);
                }
            }
        }
        return LoadUserDataFromDataBase.getUserImageFromDB(env);
    }

}
