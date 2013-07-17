package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    public static final @Nonnull String SYM_PARAM = "?";
    public static final @Nonnull String SYM_AND_PARAM = "&";

    public static final @Nonnull String SYM_BRACET_LEFT = "{";
    public static final @Nonnull String SYM_BRACET_RIGHT = "}";
    public static final @Nonnull String SYM_PARAM_SETTER = "=";

    public static final @Nonnull String API_URL = "http://api.prize-word.com";
    public static final @Nonnull String PROVIDER_LOGIN = API_URL + "/%s/login";
    public static final @Nonnull String SIGN_UP = API_URL + "/signup";
    public static final @Nonnull String PROVIDER_VK = "vkontakte";
    public static final @Nonnull String PROVIDER_FB = "facebook";


    public static final @Nonnull String SESSION_KEY = "session_key";
    public static final @Nonnull String GET_USER_DATA_URL = API_URL + "/me" + SYM_PARAM + addParam(SESSION_KEY);

    private static @Nonnull String addParam(@Nonnull String name)
    {
        return name + SYM_PARAM_SETTER + SYM_BRACET_LEFT + name + SYM_BRACET_RIGHT;
    }
}
