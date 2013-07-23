package com.ltst.prizeword.tools;

import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nullable;

/**
 * Created by cosic on 23.07.13.
 */
public class LoadImageTask implements DbService.IDbTask {

    @Override
    public Bundle execute(DbService.DbTaskEnv dbTaskEnv) {
        @Nullable Bundle bundle = dbTaskEnv.extras;
        if(bundle == null)
            return null;
        return  null;
    }
}
