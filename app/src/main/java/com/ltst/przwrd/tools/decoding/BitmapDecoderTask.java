package com.ltst.przwrd.tools.decoding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;

import org.omich.velo.bcops.simple.IBcTask;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapDecoderTask implements IBcTask
{
    public static final @Nonnull String BF_RESOURCE_ID = "BitmapDecoderTask.resourceId";
    public static final @Nonnull String BF_DECODE_RECT = "BitmapDecoderTask.decodeRect";

    public static final @Nonnull String BF_BITMAP_TILE_LIST = "BitmapDecoderTask.bitmapList";
    public static final @Nonnull String BF_BITMAP = "BitmapDecoderTask.bitmap";

    public static final @Nonnull
    Intent createIntent(int resourceId, @Nullable Rect decodeRect)
    {
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
    public Bundle execute(@Nonnull IBcTask.BcTaskEnv decodeTaskEnv)
    {
        Bundle extras = decodeTaskEnv.extras;
        if (extras == null)
        {
            return null;
        }
        int resource = extras.getInt(BF_RESOURCE_ID);
        @Nullable Rect rect = extras.getParcelable(BF_DECODE_RECT);
        if (rect != null)
        {
            ArrayList<Bitmap> bitmaps = BitmapDecoder.decodeTiles(decodeTaskEnv.context, resource, rect);
            if (bitmaps != null)
            {
                return packToBundle(resource, bitmaps);
            }
        }
        else
        {
            Bitmap bm = BitmapDecoder.decode(decodeTaskEnv.context, resource);
            if (bm != null)
            {
                return packToBundle(resource, bm);
            }
        }
        return null;
    }

    private static Bundle packToBundle(int resourceId, @Nonnull Bitmap bitmap)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BF_BITMAP, bitmap);
        bundle.putInt(BF_RESOURCE_ID, resourceId);
        return bundle;
    }

    private static Bundle packToBundle(int resourceId, @Nonnull ArrayList<Bitmap> bitmaps)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(BF_BITMAP_TILE_LIST, bitmaps);
        bundle.putInt(BF_RESOURCE_ID, resourceId);
        return bundle;
    }
}
