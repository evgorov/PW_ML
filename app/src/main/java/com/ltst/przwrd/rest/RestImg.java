package com.ltst.przwrd.rest;

import com.ltst.przwrd.dowloading.IImagesDownloadingClient;
import com.ltst.przwrd.dowloading.ImagesDownloadingClient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 23.07.13.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestImg {

    public static @Nonnull
    IImagesDownloadingClient createImagesClient()
    {
        IImagesDownloadingClient client = new ImagesDownloadingClient();
        return client;
    }
}
