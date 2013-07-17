package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    public static final @Nonnull String VK_API_ID = "3392295";

    public static final @Nonnull String API_URL = "http://api.prize-word.com";
    public static final @Nonnull String PROVIDER_LOGIN = API_URL + "/%s/login";
    public static final @Nonnull String PROVIDER_AUTORITHE = API_URL + "/%s/authorize";
    public static final @Nonnull String SIGN_UP = API_URL + "/signup";

    public static final @Nonnull String FB_PROVIDER = "facebook";
    public static final @Nonnull String FB_LOGIN_URL = String.format(PROVIDER_LOGIN, FB_PROVIDER);

    public static final @Nonnull String VK_PROVIDER = "vkontakte";
    public static final @Nonnull String VK_LOGIN_URL = String.format(PROVIDER_LOGIN, VK_PROVIDER);

    public static final @Nonnull String VK_TOKEN_URL = "https://oauth.vk.com/blank.html";
    public static final @Nonnull String FB_TOKEN_URL = "http://api.prize-word.com/facebook/authorize";

    public static final @Nonnull String VK_AUTORITHE_URL = String.format(PROVIDER_AUTORITHE,VK_PROVIDER);
    public static final @Nonnull String FB_AUTORITHE_URL = String.format(PROVIDER_AUTORITHE,FB_PROVIDER);

}
