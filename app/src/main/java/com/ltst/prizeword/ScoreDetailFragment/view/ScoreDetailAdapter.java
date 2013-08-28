package com.ltst.prizeword.ScoreDetailFragment.view;

import android.content.Context;
import android.view.View;

import org.omich.velo.lists.ISlowSource;
import org.omich.velo.lists.SlowSourceAdapter;

import javax.annotation.Nonnull;

public class ScoreDetailAdapter extends SlowSourceAdapter
{

    public ScoreDetailAdapter(@Nonnull Context context, @Nonnull ISlowSource slowSource)
    {
        super(context, slowSource);
    }

    @Override
    protected void appendQuickDataToView(@Nonnull Object o, @Nonnull Object o2, @Deprecated @Nonnull View view, @Deprecated int position)
    {

    }

    @Override
    protected void appendSlowDataToView(@Nonnull Object o, @Nonnull Object o2, @Deprecated @Nonnull View view, @Deprecated int position)
    {

    }

    @Override protected Object createViewHolderOfView(@Nonnull View view)
    {
        return null;
    }

    @Override protected int getItemViewResId()
    {
        return 0;
    }
}
