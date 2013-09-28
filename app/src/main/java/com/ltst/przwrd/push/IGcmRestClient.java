package com.ltst.przwrd.push;

import javax.annotation.Nonnull;

public interface IGcmRestClient
{
    public void sendRegistrationId(@Nonnull String registrationId);
}
