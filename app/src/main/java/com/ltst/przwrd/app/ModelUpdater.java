package com.ltst.przwrd.app;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.client.IBcConnector;
import org.omich.velo.handlers.IListener;
import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.log.Log;

import android.content.Intent;
import android.os.Bundle;

/**
 * Загрузчик модели. Обновляет данные модели по требованию извне.
 * Следит за тем, чтобы
 */
abstract public class ModelUpdater<TaskEnv> implements IListener<Bundle>, Closeable
{
    private @Nullable String mTaskId;
    private @Nonnull List<IListenerVoid> mListeners = new ArrayList<IListenerVoid>();

    @Override
    public void close()
    {
        String taskId = this.mTaskId;
        IBcConnector connector = getBcConnector();
        if(taskId != null)
        {
            connector.cancelTask(taskId);
            connector.unsubscribeTask(taskId);
            Log.i("task closed: " + taskId);
            this.mTaskId = null;
        }
    }

    public void update (@Nullable IListenerVoid updateHandler)
    {
        IBcConnector connector = getBcConnector();

        if (updateHandler != null)
        {
            mListeners.add(updateHandler);
        }

        if(mTaskId != null)
            return;
        @Nullable Intent intent = createIntent();

        if(intent == null)
            return;
        mTaskId = connector.startTask(getServiceClass(),
                getTaskClass(),
                intent, this);
    }

    @Override
    public void handle (@Nullable Bundle result)
    {
        mTaskId = null;

        handleData(result);

        List<IListenerVoid> handlers = mListeners;
        mListeners = new ArrayList<IListenerVoid>();

        for(IListenerVoid handler : handlers)
        {
            handler.handle();
        }
    }

    abstract protected @Nonnull IBcConnector getBcConnector();
    abstract protected @Nullable Intent createIntent();
    abstract protected @Nonnull Class<? extends IBcBaseTask<TaskEnv>> getTaskClass();
    abstract protected @Nonnull Class<? extends BcBaseService<TaskEnv>> getServiceClass();
    abstract protected void handleData (@Nullable Bundle result);
}

