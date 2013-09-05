package com.ltst.prizeword.manadges;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cosic on 02.09.13.
 */
public class BuySetOnServerTask implements DbService.IDbTask {

    final static public @Nonnull Intent createReloadIntent(){
        @Nonnull Intent intent = new Intent();
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv dbTaskEnv) {
        return null;
    }

}
