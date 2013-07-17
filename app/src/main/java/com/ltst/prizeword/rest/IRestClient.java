package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IRestClient
{
    @Nullable RestUserData getUserData(@Nonnull String sessionToken);
}
