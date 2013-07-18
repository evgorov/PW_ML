package com.ltst.prizeword.db;

import com.ltst.prizeword.login.model.UserData;
import com.ltst.prizeword.login.model.UserProvider;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDbWriter
{
    void putUser(@Nonnull UserData user, @Nullable List<UserProvider> providers);
}
