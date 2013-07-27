package com.ltst.prizeword.rest;

import android.graphics.Bitmap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;
import org.omich.velo.net.Network;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        RestUserData.RestUserDataHolder holder = restTemplate.exchange(RestParams.URL_GET_USER_DATA, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables).getBody();
        return holder.getUserData();
    }

//    @Override
//    public RestUserData resetUserPic(@Nonnull String sessionKey, @Nonnull byte[] userPic){
//// Отправка аватарки на сервер для обновления данных;
//// Create and populate a simple object to be used in the request
//
//        String url = "http://api.prize-word.com/me?session_key="+sessionKey;
//
//        // populate the data to post
//        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
////        try {
////            reqEntity.addPart("name", new StringBody("Name"));
////            reqEntity.addPart("Id", new StringBody("ID"));
////            reqEntity.addPart("title",new StringBody("TITLE"));
////            reqEntity.addPart("caption", new StringBody("Caption"));
////        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
////        }
//        try{
//            byte[] data = userPic;
//            ByteArrayBody bab = new ByteArrayBody(data, "somename.png");
//            reqEntity.addPart(RestParams.USERPIC, bab);
//        }
//        catch(Exception e){
//        }
//// Set the Content-Type header
//        HttpHeaders requestHeaders = new HttpHeaders();
////        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
//        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
//        requestHeaders.setAccept(acceptableMediaTypes);
//        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//// Populate the MultiValueMap being serialized and headers in an HttpEntity object to use for the request
//        HttpEntity<MultipartEntity> requestEntity = new HttpEntity<MultipartEntity>(reqEntity, requestHeaders);
//
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
//        restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
//        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
//
//// Make the HTTP POST request, marshaling the request to JSON, and the response to a String
//        ResponseEntity<RestUserData.RestUserDataHolder> entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class);
////        ResponseEntity<RestUserData.RestUserDataHolder> entity = restTemplate.postForEntity(url, requestEntity, RestUserData.RestUserDataHolder.class);
//        RestUserData.RestUserDataHolder result = entity.getBody();
//        result.setStatusCode(entity.getStatusCode());
//        return result.getUserData();
//    }

    @Override
    public RestUserData resetUserPic(@Nonnull String sessionKey, @Nonnull byte[] userPic){

        try{
            String url = "http://api.prize-word.com/me?session_key="+sessionKey;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            reqEntity.addPart("name", new StringBody("Name"));
//            reqEntity.addPart("Id", new StringBody("ID"));
//            reqEntity.addPart("title",new StringBody("TITLE"));
//            reqEntity.addPart("caption", new StringBody("Caption"));
            byte[] data = userPic;
            ByteArrayBody bab = new ByteArrayBody(data, "somename.png");
            reqEntity.addPart(RestParams.USERPIC, bab);

            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            String sResponse;
            StringBuilder s = new StringBuilder();
            while ((sResponse = reader.readLine()) != null) {
                s = s.append(sResponse);
            }

        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new RestUserData();

    }


    // Более или менее рабочий вариант;
//    @Override
//    public RestUserData resetUserPic(@Nonnull String sessionKey, @Nonnull byte[] userPic){
//        // Create and populate a simple object to be used in the request
////        RestUserData.RestUserDataSender message = new RestUserData.RestUserDataSender();
////        message.setSessionKey(token);
////        message.setUserpic(userPic);
//        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
//        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
//        urlVariables.put(RestParams.USERPIC, userPic);
//// Set the Content-Type header
//        HttpHeaders requestHeaders = new HttpHeaders();
////        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//        HttpEntity<RestUserData.RestUserDataSender> requestEntity = new HttpEntity<RestUserData.RestUserDataSender>(requestHeaders);
//
//        String url = RestParams.URL_RESET_USER_PIC;
//
//// Make the HTTP POST request, marshaling the request to JSON, and the response to a String
//        ResponseEntity<RestUserData.RestUserDataHolder> entity = restTemplate.exchange(RestParams.URL_RESET_USER_PIC, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
//        RestUserData.RestUserDataHolder result = entity.getBody();
//        result.setStatusCode(entity.getStatusCode());
//        return result.getUserData();
//    }

//    @Override
//    public RestUserData resetUserPic(@Nonnull String sessionKey, @Nonnull byte[] userPic){
//// Отправка аватарки на сервер для обновления данных;
//// Create and populate a simple object to be used in the request
//        MultipartEntity urlVariables = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
////        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
////        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
////        urlVariables.put(RestParams.USERPIC, userPic);
//        try{
//            ByteArrayBody bab = new ByteArrayBody(userPic, "userpic.jpg");
//            urlVariables.addPart(RestParams.SESSION_KEY, new StringBody(sessionKey));
//            urlVariables.addPart(RestParams.USERPIC, bab);
//        }
//        catch(Exception e){
////            urlVariables.put(RestParams.USERPIC, userPic);
//        }
//
//// Set the Content-Type header
//        HttpHeaders requestHeaders = new HttpHeaders();
////        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
//        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//        HttpEntity<RestUserData.RestUserDataSender> requestEntity = new HttpEntity<RestUserData.RestUserDataSender>(requestHeaders);
//
//// Make the HTTP POST request, marshaling the request to JSON, and the response to a String
//        ResponseEntity<RestUserData.RestUserDataHolder> entity = restTemplate.exchange(RestParams.URL_RESET_USER_PIC, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
//        RestUserData.RestUserDataHolder result = entity.getBody();
//        result.setStatusCode(entity.getStatusCode());
//        return result.getUserData();
//    }

    @Override
    public RestUserData resetUserName(@Nonnull String sessionKey, @Nonnull String userName){
// Отправка имени на сервер для обновления данных;
// Create and populate a simple object to be used in the request
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.NAME, userName);

// Set the Content-Type header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RestUserData.RestUserDataSender> requestEntity = new HttpEntity<RestUserData.RestUserDataSender>(requestHeaders);

// Make the HTTP POST request, marshaling the request to JSON, and the response to a String
        ResponseEntity<RestUserData.RestUserDataHolder> entity = restTemplate.exchange(RestParams.URL_RESET_USER_NAME, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        RestUserData.RestUserDataHolder result = entity.getBody();
        result.setStatusCode(entity.getStatusCode());
        return result.getUserData();
    }

    @Nullable
    @Override
    public RestUserData.RestUserDataHolder getSessionKeyByProvider(@Nonnull String provider, @Nonnull String access_token)
    {
        // Отправка имени на сервер для обновления данных;
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.ACCESS_TOKEN, access_token);
        @Nonnull String url = Strings.EMPTY;
        if(provider.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.URL_VK_AUTORITHE;
        }
        else if(provider.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.URL_FB_AUTORITHE;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestUserData.RestUserDataHolder> holder =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        holder.getBody().setStatusCode(holder.getStatusCode());
        if(!url.equals(Strings.EMPTY))
            return holder.getBody();
        else
            return null;
    }

    @Nullable
    @Override
    public RestUserData.RestUserDataHolder getSessionKeyBySignUp(@Nonnull String email,
                                                                 @Nonnull String name,
                                                                 @Nonnull String surname,
                                                                 @Nonnull String password,
                                                                 @Nullable String birthdate,
                                                                 @Nullable String city,
                                                                 @Nullable byte[] userpic)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.EMAIL, email);
        urlVariables.put(RestParams.NAME, name);
        urlVariables.put(RestParams.SURNAME, surname);
        urlVariables.put(RestParams.PASSWORD, password);
        @Nonnull String url = RestParams.URL_SIGN_UP;
        if(birthdate != null)
        {
            urlVariables.put(RestParams.BIRTHDATE, birthdate);
            url += RestParams.addParam(RestParams.BIRTHDATE, false);
        }
        if(city != null)
        {
            urlVariables.put(RestParams.CITY, city);
            url += RestParams.addParam(RestParams.CITY, false);
        }
        if (userpic != null)
        {
            urlVariables.put(RestParams.USERPIC, userpic);
            url += RestParams.addParam(RestParams.USERPIC, false);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestUserData.RestUserDataHolder> holder = null;
        try
        {
            holder = restTemplate.exchange(url, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        }
        catch (HttpClientErrorException e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            if (holder == null)
            {
                HttpStatus status = HttpStatus.valueOf(403);
                RestUserData.RestUserDataHolder ret = new RestUserData.RestUserDataHolder();
                ret.setStatusCode(status);
                return ret;
            }
        }

        if(holder != null)
        {
            holder.getBody().setStatusCode(holder.getStatusCode());
            return holder.getBody();
        }
        else
            return null;
    }

    @Nullable
    @Override
    public RestUserData.RestUserDataHolder getSessionKeyByLogin(@Nonnull String email, @Nonnull String password)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.EMAIL, email);
        urlVariables.put(RestParams.PASSWORD, password);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestUserData.RestUserDataHolder> holder =
                restTemplate.exchange(RestParams.URL_LOGIN, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        holder.getBody().setStatusCode(holder.getStatusCode());
        return holder.getBody();
    }

    @Override
    public HttpStatus forgotPassword(@Nonnull String email)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.EMAIL, email);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);

        @Nullable ResponseEntity<String> entity = null;
        try
        {
            entity = restTemplate.exchange(RestParams.URL_FORGOT_PASSWORD, HttpMethod.POST, requestEntity, String.class, urlVariables);
        }
        catch (HttpClientErrorException e)
        {

        }
        finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(403);
                return status;
            }
        }
        if (entity != null)
            return entity.getStatusCode();
        else
            return null;
    }

    @Override
    public HttpStatus resetPassword(@Nonnull String token, @Nonnull String newPassword)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.PASSWORD_TOKEN, token);
        urlVariables.put(RestParams.PASSWORD, newPassword);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new StringHttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        @Nullable ResponseEntity<String> entity = null;
        try
        {
            entity = restTemplate.exchange(RestParams.URL_RESET_PASSWORD, HttpMethod.POST, requestEntity, String.class, urlVariables);
        }
        catch (HttpClientErrorException e)
        {
        }
        finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(404);
                return status;
            }
        }
        if (entity != null)
            return entity.getStatusCode();
        else
            return null;
    }

    @Nullable
    @Override
    public RestPuzzleSet.RestPuzzleSetsHolder getPublishedSets(@Nonnull String sessionKey)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestPuzzleSet[]> entity = restTemplate.exchange(RestParams.URL_GET_PUBLISHED_SETS_SHORT, HttpMethod.GET, requestEntity, RestPuzzleSet[].class, urlVariables);
        RestPuzzleSet.RestPuzzleSetsHolder holder = new RestPuzzleSet.RestPuzzleSetsHolder();
        List<RestPuzzleSet> sets = Arrays.asList(entity.getBody());
        holder.setPuzzleSets(sets);
        holder.setHttpStatus(entity.getStatusCode());
        return holder;
    }

    @Nullable
    @Override
    public RestPuzzle getPuzzle(@Nonnull String sessionKey, @Nonnull String puzzleServerId)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.USER_PUZZLE_IDS, puzzleServerId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        RestPuzzle puzzle = restTemplate.exchange(RestParams.URL_GET_USER_PUZZLES, HttpMethod.GET, requestEntity, RestPuzzle.class, urlVariables).getBody();
        return puzzle;
    }
}
