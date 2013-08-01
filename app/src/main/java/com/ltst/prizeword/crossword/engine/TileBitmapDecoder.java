package com.ltst.prizeword.crossword.engine;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;

import org.omich.velo.log.Log;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileBitmapDecoder
{
    protected int resourceId;
    protected int tileWidth;
    protected int tileHeight;
    protected @Nonnull Context mContext;
    protected @Nullable BitmapRegionDecoder mRegionDecoder;
    protected int tileColumns = 0;
    protected int tileRows = 0;

    public TileBitmapDecoder(@Nonnull Context context, int resourceId, int tileWidth, int tileHeight)
    {
        this.mContext = context;
        this.resourceId = resourceId;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        InputStream stream = context.getResources().openRawResource(resourceId);
        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
            {
                mRegionDecoder = BitmapRegionDecoder.newInstance(stream, false);
            }
            else
            {
                // @TODO тут надо решить что делать?
                // либо отказаться от версий < 2.3.3
                // либо писать загрузку полной картинки, а потом кроп, что может плохо сказатся на памяти
                // либо писать свою нативную обертку как описано тут http://stackoverflow.com/questions/10035169/how-can-i-use-bitmapregiondecoder-code-in-android-2-2-2-froyo
            }
        }
        catch (IOException e)
        {
            Log.e(e.getMessage());
        }
        computeDimensions();

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    private void computeDimensions()
    {
        if (mRegionDecoder != null)
        {
            tileColumns = mRegionDecoder.getWidth()/tileWidth;
            tileRows =  mRegionDecoder.getHeight()/tileHeight;
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void computeDimension()
    {

    }

    public int getTileColumns()
    {
        return tileColumns;
    }

    public int getTileRows()
    {
        return tileRows;
    }

    public int getResourceId()
    {
        return resourceId;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public @Nullable Bitmap decodeTile(int column, int row)
    {
        if (mRegionDecoder == null)
        {
            return null;
        }
        Rect regionRect = new Rect(column * tileWidth, row * tileHeight,
                (column + 1) * tileWidth, (row + 1) * tileHeight);
        return mRegionDecoder.decodeRegion(regionRect, null);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public void recycle()
    {
        if (mRegionDecoder != null)
        {
            mRegionDecoder.recycle();
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }
}
