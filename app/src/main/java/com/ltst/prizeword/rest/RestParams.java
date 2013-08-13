package com.ltst.prizeword.rest;

import javax.annotation.Nonnull;

public class RestParams
{
    // == Status codes ==

    public static final int SC_SUCCESS = 200;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_ERROR = 404;
    public static final int SC_FORBIDDEN = 403;

    // == symbols ==
    public static final @Nonnull String VK_API_ID = "3392295";
    public static final @Nonnull String SYM_PARAM = "?";
    public static final @Nonnull String SYM_AND_PARAM = "&";
    public static final @Nonnull String SYM_BRACET_LEFT = "{";
    public static final @Nonnull String SYM_BRACET_RIGHT = "}";
    public static final @Nonnull String SYM_PARAM_SETTER = "=";
    public static final @Nonnull String SYM_SHARP= "#";

    // == API PARAMS ==

    public static final @Nonnull String FB_PROVIDER = "facebook";
    public static final @Nonnull String VK_PROVIDER = "vkontakte";

    public static final @Nonnull String ACCESS_TOKEN = "access_token";
    public static final @Nonnull String SESSION_KEY = "session_key";
    public static final @Nonnull String SESSION_KEY1 = "session_key1";
    public static final @Nonnull String SESSION_KEY2 = "session_key2";
    public static final @Nonnull String EMAIL = "email";
    public static final @Nonnull String PASSWORD = "password";
    public static final @Nonnull String NAME = "name";
    public static final @Nonnull String SURNAME = "surname";
    public static final @Nonnull String BIRTHDATE = "birthdate";
    public static final @Nonnull String USERPIC = "userpic";
    public static final @Nonnull String CITY = "city";
    public static final @Nonnull String PASSWORD_TOKEN = "token";
    public static final @Nonnull String USER_PUZZLE_IDS = "ids";
    public static final @Nonnull String YEAR = "year";
    public static final @Nonnull String MONTH = "month";
    public static final @Nonnull String MODE = "mode";
    public static final @Nonnull String MODE_SHORT = "short";

    // == API URLS ==

    // == login/auth

    public static final @Nonnull String URL_API = "http://api.prize-word.com";
    public static final @Nonnull String URL_PROVIDER_LOGIN = URL_API + "/%s/login";
    public static final @Nonnull String URL_PROVIDER_AUTORITHE = URL_API + "/%s/authorize";
    public static final @Nonnull String URL_SIGN_UP = URL_API + "/signup"
                                                            + addParam(EMAIL, true)
                                                            + addParam(NAME, false)
                                                            + addParam(SURNAME, false)
                                                            + addParam(PASSWORD, false);

    public static final @Nonnull String URL_LOGIN = URL_API + "/login"
                                                            + addParam(EMAIL, true)
                                                            + addParam(PASSWORD, false);
    public static final @Nonnull String URL_POST_LINK_ACCOUNTS = URL_API + "/link_accounts" + addParam(SESSION_KEY1, true) + addParam(SESSION_KEY2, false);

    public static final @Nonnull String URL_GET_USER_DATA = URL_API + "/me" + addParam(SESSION_KEY, true);
    public static final @Nonnull String URL_RESET_USER_PIC = URL_API + "/me"+SYM_PARAM+SESSION_KEY+SYM_PARAM_SETTER;
    public static final @Nonnull String URL_RESET_USER_NAME = URL_GET_USER_DATA + addParam(NAME, false);

    public static final @Nonnull String URL_FORGOT_PASSWORD = URL_API + "/forgot_password" + addParam(EMAIL, true);
    public static final @Nonnull String URL_RESET_PASSWORD = URL_API + "/password_reset" + addParam(PASSWORD_TOKEN, true) + addParam(PASSWORD, false);

    // == SOCIAL URLS ==

    public static final @Nonnull String URL_FB_LOGIN = String.format(URL_PROVIDER_LOGIN, FB_PROVIDER);
    public static final @Nonnull String URL_VK_LOGIN = String.format(URL_PROVIDER_LOGIN, VK_PROVIDER);
    public static final @Nonnull String URL_VK_TOKEN = "https://oauth.vk.com/blank.html";
    public static final @Nonnull String URL_FB_TOKEN = "http://api.prize-word.com/facebook/authorize#";
    public static final @Nonnull String URL_VK_AUTORITHE = String.format(URL_PROVIDER_AUTORITHE,VK_PROVIDER) + addParam(ACCESS_TOKEN, true);
    public static final @Nonnull String URL_FB_AUTORITHE = String.format(URL_PROVIDER_AUTORITHE,FB_PROVIDER) + addParam(ACCESS_TOKEN, true);
    public static final @Nonnull String URL_GET_FRIEND_DATA= URL_API + "/%s/friends" ;
    public static final @Nonnull String URL_GET_FB_FRIEND_DATA = String.format(URL_GET_FRIEND_DATA, FB_PROVIDER)+ addParam(SESSION_KEY, true);
    public static final @Nonnull String URL_GET_VK_FRIEND_DATA = String.format(URL_GET_FRIEND_DATA, VK_PROVIDER)+ addParam(SESSION_KEY, true);

    // === URL parsing for launching app via http link (forgot password case)

    public static final @Nonnull String PARAM_PARSE_URL = "url";
    public static final @Nonnull String PARAM_PARSE_TOKEN = "token";

    // ==== Puzzle URLS ====

    public static final @Nonnull String URL_GET_PUBLISHED_SETS_SHORT = URL_API + "/published_sets"
            + addParam(SESSION_KEY, true)
            + addParam(MODE, false)
            + addParam(MONTH, false)
            + addParam(YEAR, false);
    public static final @Nonnull String URL_GET_USER_PUZZLES = URL_API + "/user_puzzles" + addParam(SESSION_KEY, true) + addParam(USER_PUZZLE_IDS, false);

    // ================

    public static @Nonnull String addParam(@Nonnull String name, boolean firstParam)
    {
        return ((firstParam) ? SYM_PARAM : SYM_AND_PARAM) + name + SYM_PARAM_SETTER + SYM_BRACET_LEFT + name + SYM_BRACET_RIGHT;
    }
}
