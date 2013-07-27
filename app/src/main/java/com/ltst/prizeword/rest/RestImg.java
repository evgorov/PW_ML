package com.ltst.prizeword.rest;

import com.ltst.prizeword.dowloading.IImagesDownloadingClient;
import com.ltst.prizeword.dowloading.ImagesDownloadingClient;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 23.07.13.
 */
public class RestImg {

    public static @Nonnull
    IImagesDownloadingClient createImagesClient()
    {
        IImagesDownloadingClient client = new ImagesDownloadingClient();
        return client;
    }
}
