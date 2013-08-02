package com.ltst.prizeword.tools.decoding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Build;

import org.omich.velo.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapDecoder
{
    @Nullable
    public static Bitmap decode(@Nonnull Context context, int resourceId)
    {
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }

    @Nullable
    public static ArrayList<Bitmap> decodeTiles(@Nonnull Context context, int resourceId, @Nonnull Rect rect)
    {
        InputStream stream = context.getResources().openRawResource(resourceId);
        @Nullable BitmapRegionDecoder mRegionDecoder = null;

        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
            {
                mRegionDecoder = BitmapRegionDecoder.newInstance(stream, false);
            }
            else
            {
                // @TODO тут надо решить что делать с загрузкой тайлов?
                // либо отказаться от версий < 2.3.3
                // либо писать загрузку полной картинки, а потом кроп, что может плохо сказатся на памяти
                // либо писать свою нативную обертку как описано тут http://stackoverflow.com/questions/10035169/how-can-i-use-bitmapregiondecoder-code-in-android-2-2-2-froyo
            }
        }
        catch (IOException e)
        {
            Log.e(e.getMessage());
        }

        int imageWidth = 0;
        int imageHeight = 0;
        int tileCols = 0;
        int tileRows = 0;
        if (mRegionDecoder != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
            {
                imageWidth = mRegionDecoder.getWidth();
                imageHeight = mRegionDecoder.getHeight();
            }
            else
            {
                //@TODO
            }
            tileCols = imageWidth/rect.width();
            tileRows = imageHeight/rect.height();
        }

        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(tileCols * tileRows);
        Rect tileRect = new Rect(rect);

        while (tileRect.bottom < imageHeight)
        {
            while (tileRect.right < imageWidth)
            {
                if (mRegionDecoder != null)
                {
                    @Nullable Bitmap bm = null;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1)
                    {
                        bm = mRegionDecoder.decodeRegion(tileRect, null);
                    }
                    else
                    {
                        //@TODO
                    }

                    if (bm != null)
                    {
                        bitmaps.add(bm);
                    }
                }
                tileRect.left += rect.width();
                tileRect.right += rect.width();
            }
            tileRect.left = rect.left;
            tileRect.right = rect.width();

            tileRect.top += rect.height();
            tileRect.bottom += rect.height();
        }

        return bitmaps;
    }
}
