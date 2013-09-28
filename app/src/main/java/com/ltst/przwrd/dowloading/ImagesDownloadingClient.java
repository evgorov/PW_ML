package com.ltst.przwrd.dowloading;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 23.07.13.
 */
public class ImagesDownloadingClient implements IImagesDownloadingClient {
    @Override
    @Nullable
    public byte[] getImage(@Nonnull String url)
    {
        try
        {
            return Downloader.download(url);
        }
        catch(Downloader.DownloaderException e)
        {
            return null;
        }
    }
}
