package com.ltst.prizeword.tools.decoding;

import android.content.Context;
import android.os.Bundle;

import org.omich.velo.bcops.BcBaseService;
import org.omich.velo.bcops.BcEnvException;
import org.omich.velo.bcops.ByNameTaskCreator;
import org.omich.velo.bcops.IBcBaseTask;
import org.omich.velo.bcops.IBcToaster;
import org.omich.velo.bcops.ICancelledInfo;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BitmapDecodingService extends BcBaseService<BitmapDecodingService.DecodeTaskEnv>
{
    public interface IDecodeTask extends IBcBaseTask<DecodeTaskEnv>
    {
        //Just interface with concrete param.
    }

    public class DecodeTaskEnv extends IBcTask.BcTaskEnv
    {
        public DecodeTaskEnv(@Nullable Bundle extras,
                             @Nonnull Context context,
                             @Nonnull IBcToaster bcToaster,
                             @Nullable IListenerInt ph,
                             @Nullable ICancelledInfo ci)
        {
            super(extras, context, bcToaster, ph, ci);
        }
    }

    public BitmapDecodingService()
    {
        super(NonnullableCasts.classGetCanonicalName(BitmapDecodingService.class),
                new ByNameTaskCreator());
        Log.i("Create decoding service");
    }

    @Nonnull
    @Override
    protected DecodeTaskEnv createTaskEnv(@Nullable Bundle extras,
                                          @Nonnull Context context,
                                          @Nonnull IBcToaster bcToaster,
                                          @Nullable IListenerInt ph,
                                          @Nullable ICancelledInfo ci) throws BcEnvException
    {
        return new DecodeTaskEnv(extras, context, bcToaster, ph, ci);
    }

    @Override
    public void onDestroy()
    {
        Log.i("Destroy decoding service");
        super.onDestroy();
    }
}
