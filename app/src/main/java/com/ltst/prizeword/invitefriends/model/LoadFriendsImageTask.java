package com.ltst.prizeword.invitefriends.model;

import android.content.Intent;

import com.ltst.prizeword.db.IDbWriter;
import com.ltst.prizeword.dowloading.LoadImageTask;

import javax.annotation.Nonnull;

public class LoadFriendsImageTask extends LoadImageTask
{
    public static @Nonnull Intent createIntent(@Nonnull String imageId)
    {
        return LoadImageTask.createIntent(imageId);
    }

    protected void callPutImageMethod(@Nonnull IDbWriter storage, @Nonnull String key, @Nonnull byte[] bytes)
    {
        storage.putFriendsImage(key, bytes);
    }
}
