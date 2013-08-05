package com.ltst.prizeword.crossword.engine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;

import com.ltst.prizeword.app.ModelUpdater;
import com.ltst.prizeword.tools.decoding.BitmapDecoderTask;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.bcops.simple.BcService;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.handlers.IListener;
import org.omich.velo.log.Log;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapResourceModel implements IBitmapResourceModel
{
    private @Nonnull IBcConnector mBcConnector;

    public BitmapResourceModel(@Nonnull IBcConnector bcConnector)
    {
        mBcConnector = bcConnector;
    }

    @Override
    public void loadBitmapEntity(final int resource, @Nullable final IListener<BitmapEntity> handler)
    {
        BitmapEntityDecoder decoder = new SingleBitmapDecoder()
        {
            @Override
            public void handleBitmap(@Nonnull BitmapEntity entity)
            {
                if (handler != null)
                {
                    handler.handle(entity);
                }
            }

            @Nonnull
            @Override
            protected Intent createIntent()
            {
                return BitmapDecoderTask.createIntent(resource, null);
            }
        };
        decoder.update(null);
    }

    @Override
    public void loadTileBitmapEntityList(final int resource, int tileWidth, int tileHeight, final IListener<List<BitmapEntity>> handler)
    {
        final Rect rect = new Rect(0, 0, tileWidth, tileHeight);
        TilesBitmapDecoder decoder = new TilesBitmapDecoder()
        {
            @Override
            public void handleBitmapList(@Nonnull List<BitmapEntity> entity)
            {
                handler.handle(entity);
            }

            @Nonnull
            @Override
            protected Intent createIntent()
            {
                return BitmapDecoderTask.createIntent(resource, rect);
            }
        };
        decoder.update(null);
    }

    private abstract class TilesBitmapDecoder extends BitmapEntityDecoder
    {
        @Override
        public void handleBitmap(@Nonnull BitmapEntity entity){}

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            ArrayList<Bitmap> bitmaps = result.getParcelableArrayList(BitmapDecoderTask.BF_BITMAP_TILE_LIST);
            if (bitmaps == null)
            {
                return;
            }
            int resource = result.getInt(BitmapDecoderTask.BF_RESOURCE_ID);

            ArrayList<BitmapEntity> entities = new ArrayList<BitmapEntity>();
            for (Bitmap bitmap : bitmaps)
            {
                BitmapEntity entity = new BitmapEntity(resource);
                entity.setBitmap(bitmap);
                entities.add(entity);
            }

            handleBitmapList(entities);
        }
    }

    private abstract class SingleBitmapDecoder extends BitmapEntityDecoder
    {

        @Override
        public final void handleBitmapList(@Nonnull List<BitmapEntity> entity){}

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            if (result == null)
            {
                return;
            }
            Bitmap bitmap = result.getParcelable(BitmapDecoderTask.BF_BITMAP);
            if (bitmap == null)
            {
                return;
            }
            int resource = result.getInt(BitmapDecoderTask.BF_RESOURCE_ID);

            BitmapEntity entity = new BitmapEntity(resource);
            entity.setBitmap(bitmap);
            handleBitmap(entity);
        }
    }

    private abstract class BitmapEntityDecoder extends ModelUpdater<IBcTask.BcTaskEnv>
    {

        @Nonnull
        @Override
        protected Class<? extends IBcBaseTask<IBcTask.BcTaskEnv>> getTaskClass()
        {
            return BitmapDecoderTask.class;
        }

        @Nonnull
        @Override
        protected Class<? extends BcBaseService<IBcTask.BcTaskEnv>> getServiceClass()
        {
            return BcService.class;
        }

        @Nonnull
        @Override
        protected IBcConnector getBcConnector()
        {
            return mBcConnector;
        }

        public abstract void handleBitmap(@Nonnull BitmapEntity entity);
        public abstract void handleBitmapList(@Nonnull List<BitmapEntity> entity);
    }
}
