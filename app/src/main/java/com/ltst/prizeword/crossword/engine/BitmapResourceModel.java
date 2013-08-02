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
    public void loadBitmapEntity(final int resource, final IListener<BitmapEntity> handler)
    {
        BitmapEntityDecoder decoder = new SingleBitmapDecoder()
        {
            @Override
            public void handleBitmap(@Nonnull BitmapEntity entity)
            {
                handler.handle(entity);
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
    }

    private abstract class TilesBitmapDecoder extends BitmapEntityDecoder
    {
        @Override
        public void handleBitmap(@Nonnull BitmapEntity entity){}

        @Override
        protected void handleData(@Nullable Bundle result)
        {

        }
    }

    private abstract class SingleBitmapDecoder extends BitmapEntityDecoder
    {

        @Override
        public final void handleBitmapList(@Nonnull List<BitmapEntity> entity){}

        @Override
        protected void handleData(@Nullable Bundle result)
        {
            Log.i("Handling result");
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
            Log.i("Bitmap handled: " + bitmap.getWidth() + " " + bitmap.getHeight());
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
