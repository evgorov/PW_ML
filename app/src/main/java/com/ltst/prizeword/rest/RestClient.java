package com.ltst.prizeword.rest;

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
}
