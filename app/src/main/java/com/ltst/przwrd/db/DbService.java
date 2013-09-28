package com.ltst.przwrd.db;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.BcEnvException;
import org.omich.velo.bcops.BcToaster;
import org.omich.velo.bcops.ByNameTaskCreator;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.IBcToaster;
import org.omich.velo.bcops.ICancelledInfo;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.handlers.IListenerInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DbService extends BcBaseService<DbService.DbTaskEnv>
{
    public interface IDbTask extends IBcBaseTask<DbTaskEnv>
    {
        //Just interface with concrete param.
    }

    public class DbTaskEnv extends IBcTask.BcTaskEnv
    {
        public final IDbWriter dbw;
        public DbTaskEnv(@Nonnull IDbWriter dbw,
                        @Nullable Bundle extras,
                         @Nonnull Context context,
                         @Nonnull IBcToaster bcToaster,
                         @Nullable IListenerInt ph,
                         @Nullable ICancelledInfo ci)
        {
            super(extras, context, bcToaster, ph, ci);
            this.dbw = dbw;
        }
    }

    private final @Nonnull SQLiteHelper mHelper;
    private @Nullable DbWriter mDbWriter;

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
        DbWriter db = mDbWriter;
        if(db == null)
        {
            try
            {
                db = mHelper.createDbWriter();
                mDbWriter = db;
            }
            catch(DbException e)
            {
                throw new BcEnvException("Can't create Writeable Db", e); //$NON-NLS-1$
            }
        }
        return new DbTaskEnv(db , extras, context, bcToaster, ph, ci);
    }

    @Override
    public void onDestroy()
    {
        DbWriter db = mDbWriter;
        if(db != null)
        {
            db.close();
        }
        super.onDestroy();
    }

}
