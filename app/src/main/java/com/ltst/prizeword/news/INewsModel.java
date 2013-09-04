package com.ltst.prizeword.news;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface INewsModel
{
    void updateFromInternet(@Nonnull IListenerVoid handler);
    @Nullable News getNews();
}
