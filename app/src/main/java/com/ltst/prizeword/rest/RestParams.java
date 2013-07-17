package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    public static final @Nonnull String VK_API_ID = "3392295";
    public static final @Nonnull String SYM_PARAM = "?";
    public static final @Nonnull String SYM_AND_PARAM = "&";

    public static final @Nonnull String SYM_BRACET_LEFT = "{";
    public static final @Nonnull String SYM_BRACET_RIGHT = "}";
    public static final @Nonnull String SYM_PARAM_SETTER = "=";
    public static final @Nonnull String SYM_SHARP= "#";

    public static final @Nonnull String API_URL = "http://api.prize-word.com";
    public static final @Nonnull String PROVIDER_LOGIN = API_URL + "/%s/login";
    public static final @Nonnull String PROVIDER_AUTORITHE = API_URL + "/%s/authorize";
    public static final @Nonnull String SIGN_UP = API_URL + "/signup";

    public static final @Nonnull String SIGNUP_PROVIDER = "signup";

    public static final @Nonnull String FB_PROVIDER = "facebook";
    public static final @Nonnull String FB_LOGIN_URL = String.format(PROVIDER_LOGIN, FB_PROVIDER);

    public static final @Nonnull String VK_PROVIDER = "vkontakte";
    public static final @Nonnull String VK_LOGIN_URL = String.format(PROVIDER_LOGIN, VK_PROVIDER);


    public static final @Nonnull String VK_TOKEN_URL = "https://oauth.vk.com/blank.html";
    public static final @Nonnull String FB_TOKEN_URL = "http://api.prize-word.com/facebook/authorize#";

    public static final @Nonnull String ACCESS_TOKEN = "access_token"
    public static final @Nonnull String VK_AUTORITHE_URL = String.format(PROVIDER_AUTORITHE,VK_PROVIDER)+ SYM_PARAM + addParam(ACCESS_TOKEN);
    public static final @Nonnull String FB_AUTORITHE_URL = String.format(PROVIDER_AUTORITHE,FB_PROVIDER)+ SYM_PARAM + addParam(ACCESS_TOKEN);

    public static final @Nonnull String SESSION_KEY = "session_key";
    public static final @Nonnull String GET_USER_DATA_URL = API_URL + "/me" + SYM_PARAM + addParam(SESSION_KEY);

    private static @Nonnull String addParam(@Nonnull String name)
    {
        return name + SYM_PARAM_SETTER + SYM_BRACET_LEFT + name + SYM_BRACET_RIGHT;
    }
}
