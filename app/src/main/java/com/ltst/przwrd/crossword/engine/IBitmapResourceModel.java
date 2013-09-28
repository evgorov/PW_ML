package com.ltst.przwrd.crossword.engine;

import org.omich.velo.handlers.IListener;

import java.util.List;

import javax.annotation.Nullable;

public interface IBitmapResourceModel
{
    public void loadBitmapEntity(final int resource, final @Nullable IListener<BitmapEntity> handler);
    public void loadTileBitmapEntityList(final int resource, int tileWidth, int tileHeight,
                                         final @Nullable IListener<List<BitmapEntity>> handler);
}
