package com.ltst.prizeword.tools;

import org.omich.velo.constants.Strings;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParcelableTools
{
    public static @Nonnull
    String getNonnullString(@Nullable String string)
    {
        return string != null ? string : Strings.EMPTY;
    }

    public static boolean getBooleanFromInt(int value)
    {
        return (value == 1) ? true : false;
    }
}
