package com.ltst.prizeword.crossword.engine;

import org.omich.velo.handlers.IListener;

import java.util.List;

public interface IBitmapResourceModel
{
    public void loadBitmapEntity(final int resource, final IListener<BitmapEntity> handler);
    public void loadTileBitmapEntityList(final int resource, int tileWidth, int tileHeight,
                                         final IListener<List<BitmapEntity>> handler);
}
