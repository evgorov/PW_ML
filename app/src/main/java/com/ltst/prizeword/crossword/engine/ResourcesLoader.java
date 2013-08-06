package com.ltst.prizeword.crossword.engine;

import org.omich.velo.handlers.IListenerVoid;

import javax.annotation.Nonnull;

// класс обертка для контроля загрузки ресурсов
public abstract class ResourcesLoader
{
    private boolean mLoaded;
    private boolean mLoading;

    protected ResourcesLoader()
    {
        mLoaded = false;
        mLoading = false;
    }

    public boolean isLoaded()
    {
        return mLoaded;
    }

    public boolean isLoading()
    {
        return mLoading;
    }

    // при переопределении нужно обязательно вызвать loadingFinishedHandler по завершению загрузки
    public abstract void loadResource(final @Nonnull IListenerVoid loadingFinishedHandler);

    public void load()
    {
        if(!isLoading() && !isLoaded())
        {
            loadingStart();
            loadResource(new IListenerVoid()
            {
                @Override
                public void handle()
                {
                    loadingFinished();
                }
            });
        }
    }

    private void loadingStart()
    {
        mLoading = true;
    }

    private void loadingFinished()
    {
        mLoading = false;
        mLoaded = true;
    }
}
