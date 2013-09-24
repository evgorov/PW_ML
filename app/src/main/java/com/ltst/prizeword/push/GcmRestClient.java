package com.ltst.prizeword.push;

import android.content.Context;

import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;

import javax.annotation.Nonnull;

public class GcmRestClient implements IGcmRestClient
{
    private @Nonnull Context mContext;
    public static IGcmRestClient create(@Nonnull Context context)
    {
        return new GcmRestClient(context);
    }

    private @Nonnull RestTemplate restTemplate;

    private GcmRestClient(@Nonnull Context context)
    {
        this.mContext = context;
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
    }

    @Override
    public void sendRegistrationId(@Nonnull String registrationId)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put("device_id", registrationId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<String> entity = null;
        try
        {
            entity = restTemplate.exchange("http://192.168.64.111:4000/api/register?device_id={device_id}", HttpMethod.POST, requestEntity, String.class, urlVariables);
        } catch (Exception e)
        {
            Log.e(e.getMessage());
        }
    }
}
