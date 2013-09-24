package com.ltst.prizeword.push;

import javax.annotation.Nonnull;

public interface IGcmRestClient
{
    public void sendRegistrationId(@Nonnull String registrationId);
}
