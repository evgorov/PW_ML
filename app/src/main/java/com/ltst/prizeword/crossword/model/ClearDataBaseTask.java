package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.db.DbService;

import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.cast.NonnullableCasts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 27.08.13.
 */
public class ClearDataBaseTask implements DbService.IDbTask {

    static final public @Nonnull Intent createIntent()
    {
        Intent intent = new Intent();
        return intent;
    }


    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv env) {

        if(!BcTaskHelper.isNetworkAvailable(env.context))
        {
            env.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            env.context.getString(R.string.msg_no_internet)));
        }
        else
        {
            env.dbw.clearDb();
        }

        return null;
    }

}
