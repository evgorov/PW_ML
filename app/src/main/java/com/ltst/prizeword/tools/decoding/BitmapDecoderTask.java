package com.ltst.prizeword.tools.decoding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;

import org.omich.velo.log.Log;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapDecoderTask implements BitmapDecodingService.IDecodeTask
{
    public static final @Nonnull String BF_RESOURCE_ID = "BitmapDecoderTask.resourceId";
    public static final @Nonnull String BF_DECODE_RECT = "BitmapDecoderTask.decodeRect";

    public static final @Nonnull String BF_BITMAP_TILE_LIST = "BitmapDecoderTask.bitmapList";
    public static final @Nonnull String BF_BITMAP = "BitmapDecoderTask.bitmap";

    public static final @Nonnull
    Intent createIntent(int resourceId, @Nullable Rect decodeRect)
    {
        Log.i("Creating intent..");
        Intent intent = new Intent();
        intent.putExtra(BF_RESOURCE_ID, resourceId);
        if (decodeRect != null)
        {
            intent.putExtra(BF_DECODE_RECT, decodeRect);
        }
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull BitmapDecodingService.DecodeTaskEnv decodeTaskEnv)
    {
        Bundle extras = decodeTaskEnv.extras;
        if (extras == null)
        {
            return null;
        }
        int resource = extras.getInt(BF_RESOURCE_ID);
        Log.i("Resource decoding: " + resource);
        @Nullable Rect rect = extras.getParcelable(BF_DECODE_RECT);
        if (rect != null)
        {
            ArrayList<Bitmap> bitmaps = BitmapDecoder.decodeTiles(decodeTaskEnv.context, resource, rect);
            if (bitmaps != null)
            {
                return packToBundle(bitmaps);
            }
        }
        else
        {
            Bitmap bm = BitmapDecoder.decode(decodeTaskEnv.context, resource);
            if (bm != null)
            {
                Log.i("Bitmap loaded: " + bm.getWidth() + " " + bm.getHeight());
                return packToBundle(bm);
            }
        }
        return null;
    }

    private @Nonnull Bundle packToBundle(@Nonnull Bitmap bitmap)
    {
        Log.i("Packing bitmap");
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_BITMAP, bitmap);
        return bundle;
    }

    private @Nonnull Bundle packToBundle(@Nonnull ArrayList<Bitmap> bitmaps)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_BITMAP_TILE_LIST, bitmaps);
        return bundle;
    }
}
