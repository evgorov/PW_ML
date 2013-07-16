package com.ltst.prizeword.db;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.BcEnvException;
import org.omich.velo.bcops.BcToaster;
import org.omich.velo.bcops.ByNameTaskCreator;
import org.omich.velo.bcops.IBcToaster;
import org.omich.velo.bcops.ICancelledInfo;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.handlers.IListenerInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DbService extends BcBaseService<DbService.DbTaskEnv>
{
    public class DbTaskEnv extends IBcTask.BcTaskEnv
    {
        public DbTaskEnv(@Nullable Bundle extras,
                         @Nonnull Context context,
                         @Nonnull IBcToaster bcToaster,
                         @Nullable IListenerInt ph,
                         @Nullable ICancelledInfo ci)
        {
            super(extras, context, bcToaster, ph, ci);
        }
    }

    private final @Nonnull SQLiteHelper mHelper;

    public DbService()
    {
        super(NonnullableCasts.classGetCanonicalName(DbService.class),
                new ByNameTaskCreator());
        mHelper = new SQLiteHelper(this, false);
        mBcToaster = new BcToaster(this, new Handler());
    }

    @Override
    @Nonnull
    protected DbTaskEnv createTaskEnv(@Nullable Bundle extras,
                                    @Nonnull Context context, @Nonnull IBcToaster bcToaster,
                                    @Nullable IListenerInt ph, @Nullable ICancelledInfo ci)
                                    throws BcEnvException
    {
        return new DbTaskEnv(extras, context, bcToaster, ph, ci);
    }
}
