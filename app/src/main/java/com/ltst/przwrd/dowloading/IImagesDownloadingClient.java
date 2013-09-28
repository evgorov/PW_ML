package com.ltst.przwrd.dowloading;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 23.07.13.
 */
public interface IImagesDownloadingClient {
    @Nullable
    byte[] getImage(@Nonnull String url);
}
