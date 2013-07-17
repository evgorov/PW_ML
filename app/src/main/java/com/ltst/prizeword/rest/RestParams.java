package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    public static final @Nonnull String API_URL = "http://api.prize-word.com";
    public static final @Nonnull String PROVIDER_LOGIN = API_URL + "/%s/login";
    public static final @Nonnull String SIGN_UP = API_URL + "/signup";
    public static final @Nonnull String PROVIDER_VK = "vkontakte";
    public static final @Nonnull String PROVIDER_FB = "facebook";
}
