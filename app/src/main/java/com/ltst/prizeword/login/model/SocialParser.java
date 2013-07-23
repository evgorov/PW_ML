package com.ltst.prizeword.login.model;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 17.07.13.
 */
public class SocialParser
{

    private static final @Nonnull String LOG_TAG = "vkontakte";

    public static String[] parseVkRedirectUrl(String url) throws Exception
    {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token = extractPattern(url, "access_token=(.*?)&");
        Log.i(LOG_TAG, "access_token=" + access_token);
        String user_id = extractPattern(url, "user_id=(\\d*)");
        Log.i(LOG_TAG, "user_id=" + user_id);
        if (user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0)
            throw new Exception("Failed to parse redirect url " + url);
        return new String[]{access_token, user_id};
    }

    public static String[] parseFbRedirectUrl(String url) throws Exception
    {
        //url is something like http://api.prize-word.com/facebook/authorize#access_token=CAAFitnSsjQ0BADVt65JxZBSprmaNUmxvvC2FJIWfYHWJPSZC8ZAn0e0oNMWakdxCqZCJWDyjbMh644wXs3JWkmA3xo3MnMkgLD5H7zwd7xjaKtmuqyhlVCDrd9BrgiMgV0gjBrUn5RT9v3Jw0J04&expires_in=5178538
        String access_token = extractPattern(url, "access_token=(.*?)&");
        Log.i(LOG_TAG, "access_token=" + access_token);
        String user_id = extractPattern(url, "expires_in=(\\d*)");
        Log.i(LOG_TAG, "expires_in=" + user_id);
        if (user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0)
            throw new Exception("Failed to parse redirect url " + url);
        return new String[]{access_token, user_id};
    }

    private static String extractPattern(String string, String pattern)
    {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }

}
