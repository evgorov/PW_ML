package com.ltst.przwrd.invitefriends.model;

import android.content.Intent;

import com.ltst.przwrd.db.IDbWriter;
import com.ltst.przwrd.dowloading.LoadImageTask;

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
