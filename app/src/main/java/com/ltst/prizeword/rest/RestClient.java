package com.ltst.prizeword.rest;

import org.omich.velo.constants.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RestClient implements IRestClient
{
    public static IRestClient create()
    {
        return new RestClient();
    }

    private @Nonnull RestTemplate restTemplate;

    private RestClient()
    {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
    }

    @Nullable
    @Override
    public RestUserData getUserData(@Nonnull String sessionKey)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        RestUserData.RestUserDataHolder holder = restTemplate.exchange(RestParams.GET_USER_DATA_URL, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables).getBody();
        return holder.getUserData();
    }

    @Nullable
    @Override
    public RestUserData.RestUserDataHolder getSessionKey(@Nonnull String provider, @Nonnull String access_token)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.ACCESS_TOKEN, access_token);
        @Nonnull String url = Strings.EMPTY;
        if(provider.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.VK_AUTORITHE_URL;
        }
        else if(provider.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.FB_AUTORITHE_URL;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        if(!url.equals(Strings.EMPTY))
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables).getBody();
        else
            return null;
    }
}
