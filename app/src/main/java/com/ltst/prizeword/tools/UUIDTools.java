package com.ltst.prizeword.tools;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 29.08.13.
 */
public class UUIDTools {

    static public @Nonnull String generateStringUUID()
    {
        return UUID.randomUUID().toString();
    }

}
