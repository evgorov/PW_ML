package com.ltst.prizeword.db;

import com.ltst.prizeword.login.model.UserData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbReader
{
    @Nullable UserData getUserByEmail(@Nonnull String email);
}
