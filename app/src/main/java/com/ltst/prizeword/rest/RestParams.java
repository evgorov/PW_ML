package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    // == symbols ==
    public static final @Nonnull String VK_API_ID = "3392295";
    public static final @Nonnull String SYM_PARAM = "?";
    public static final @Nonnull String SYM_AND_PARAM = "&";
    public static final @Nonnull String SYM_BRACET_LEFT = "{";
    public static final @Nonnull String SYM_BRACET_RIGHT = "}";
    public static final @Nonnull String SYM_PARAM_SETTER = "=";
    public static final @Nonnull String SYM_SHARP= "#";

    // == API URLS ==

    public static final @Nonnull String URL_API = "http://api.prize-word.com";
    public static final @Nonnull String URL_PROVIDER_LOGIN = URL_API + "/%s/login";
    public static final @Nonnull String URL_PROVIDER_AUTORITHE = URL_API + "/%s/authorize";
    public static final @Nonnull String URL_SIGN_UP = URL_API + "/signup";
    public static final @Nonnull String URL_LOGIN = URL_API + "/login";
    public static final @Nonnull String URL_GET_USER_DATA = URL_API + "/me" + SYM_PARAM + addParam(SESSION_KEY);

    // == SOCIAL URLS ==

    public static final @Nonnull String URL_FB_LOGIN = String.format(URL_PROVIDER_LOGIN, FB_PROVIDER);
    public static final @Nonnull String URL_VK_LOGIN = String.format(URL_PROVIDER_LOGIN, VK_PROVIDER);
    public static final @Nonnull String URL_VK_TOKEN = "https://oauth.vk.com/blank.html";
    public static final @Nonnull String URL_FB_TOKEN = "http://api.prize-word.com/facebook/authorize#";
    public static final @Nonnull String URL_VK_AUTORITHE = String.format(URL_PROVIDER_AUTORITHE,VK_PROVIDER)+ SYM_PARAM + addParam(ACCESS_TOKEN);
    public static final @Nonnull String URL_FB_AUTORITHE = String.format(URL_PROVIDER_AUTORITHE,FB_PROVIDER)+ SYM_PARAM + addParam(ACCESS_TOKEN);

    // == API PARAMS ==

    public static final @Nonnull String FB_PROVIDER = "facebook";
    public static final @Nonnull String VK_PROVIDER = "vkontakte";

    public static final @Nonnull String ACCESS_TOKEN = "access_token";
    public static final @Nonnull String SESSION_KEY = "session_key";

    // ================

    private static @Nonnull String addParam(@Nonnull String name)
    {
        return name + SYM_PARAM_SETTER + SYM_BRACET_LEFT + name + SYM_BRACET_RIGHT;
    }
}
