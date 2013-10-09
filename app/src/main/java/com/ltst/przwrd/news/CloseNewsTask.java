package com.ltst.przwrd.news;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CloseNewsTask implements DbService.IDbTask
{
    public static final @Nonnull
    Intent createIntent()
    {
        return new Intent();
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv dbTaskEnv)
    {
        @Nullable News news = dbTaskEnv.dbw.getNews();
        if (news != null)
        {
            news.closed = true;
            dbTaskEnv.dbw.updateNews(news);
        }
        return null;
    }
}
