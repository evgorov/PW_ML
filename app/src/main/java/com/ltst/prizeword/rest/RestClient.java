package com.ltst.prizeword.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.omich.velo.constants.Strings;
import org.omich.velo.log.Log;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
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

    @Override
    public RestUserData resetUserPic(@Nonnull String sessionKey, @Nonnull byte[] userPic)
    {
        // Рабочий вариант;
        try
        {
//            String url = "http://api.prize-word.com/me?session_key="+sessionKey;
            String url = RestParams.URL_RESET_USER_PIC + sessionKey;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(url);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            byte[] data = userPic;
            ByteArrayBody bab = new ByteArrayBody(data, "somename.png");
            reqEntity.addPart(RestParams.USERPIC, bab);

            postRequest.setEntity(reqEntity);
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            String sResponse;
            StringBuilder s = new StringBuilder();
            while ((sResponse = reader.readLine()) != null)
            {
                s = s.append(sResponse);
            }

        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return getUserData(sessionKey);
    }

    @Override
    public RestUserData resetUserName(@Nonnull String sessionKey, @Nonnull String userName)
    {
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
        if (provider.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.URL_VK_AUTORITHE;
        } else if (provider.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.URL_FB_AUTORITHE;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestUserData.RestUserDataHolder> holder =
                restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        holder.getBody().setStatusCode(holder.getStatusCode());
        if (!url.equals(Strings.EMPTY))
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
        if (birthdate != null)
        {
            urlVariables.put(RestParams.BIRTHDATE, birthdate);
            url += RestParams.addParam(RestParams.BIRTHDATE, false);
        }
        if (city != null)
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
        } catch (HttpClientErrorException e)
        {
            Log.e(e.getMessage());
        } finally
        {
            if (holder == null)
            {
                HttpStatus status = HttpStatus.valueOf(403);
                RestUserData.RestUserDataHolder ret = new RestUserData.RestUserDataHolder();
                ret.setStatusCode(status);
                return ret;
            }
        }

        if (holder != null)
        {
            holder.getBody().setStatusCode(holder.getStatusCode());
            return holder.getBody();
        } else
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
        } catch (HttpClientErrorException e)
        {

        } finally
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
        } catch (HttpClientErrorException e)
        {
        } finally
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
        urlVariables.put(RestParams.MODE, RestParams.MODE_SHORT);
        urlVariables.put(RestParams.YEAR, 2013);
        urlVariables.put(RestParams.MONTH, 7);
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
    public RestPuzzle.RestPuzzleHolder getPuzzle(@Nonnull String sessionKey, @Nonnull String puzzleServerId)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.USER_PUZZLE_IDS, puzzleServerId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestPuzzle[]> entity = restTemplate.exchange(RestParams.URL_GET_USER_PUZZLES, HttpMethod.GET, requestEntity, RestPuzzle[].class, urlVariables);
        RestPuzzle.RestPuzzleHolder holder = new RestPuzzle.RestPuzzleHolder();
        RestPuzzle[] puzzles = entity.getBody();
        if (puzzles.length == 0)
        {
            return null;
        }
        if (puzzles[0] != null)
        {
            holder.setPuzzles(puzzles[0]);
        }
        holder.setStatus(entity.getStatusCode());
        return holder;
    }

    @Nullable
    @Override
    public RestUserData.RestAnswerMessageHolder mergeAccounts(@Nonnull String sessionKey1, @Nonnull String sessionKey2)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY1, sessionKey1);
        urlVariables.put(RestParams.SESSION_KEY2, sessionKey2);

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);

        @Nonnull RestUserData.RestAnswerMessageHolder answer = null;
        @Nullable ResponseEntity<RestUserData.RestAnswerMessageHolder> response = null;
        try
        {
            response = restTemplate.exchange(RestParams.URL_POST_LINK_ACCOUNTS, HttpMethod.POST, requestEntity, RestUserData.RestAnswerMessageHolder.class, urlVariables);
        } catch (HttpClientErrorException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            if (response == null)
            {
                answer = new RestUserData.RestAnswerMessageHolder();
                answer.setStatusCode(HttpStatus.valueOf(RestParams.SC_FORBIDDEN));
            } else
            {
                answer = response.getBody();
                answer.setStatusCode(response.getStatusCode());
            }
        }
        return answer;
    }

    @Nullable @Override
    public RestInvite.RestInviteHolder getFriendsData(@Nonnull String sessionKey, @Nonnull String providerName)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        @Nonnull String url = Strings.EMPTY;
        if (providerName.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.URL_GET_VK_FRIEND_DATA;
        } else if (providerName.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.URL_GET_FB_FRIEND_DATA;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestInvite[]> entity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestInvite[].class, urlVariables);
        RestInvite.RestInviteHolder holder = new RestInvite.RestInviteHolder();
        RestInvite[] friends = entity.getBody();
        if (friends.length == 0)
        {
            return null;
        }
        if (friends[0] != null)
        {
            holder.setFriends(friends[0]);
        }
        holder.setStatus(entity.getStatusCode());
        return holder;

    }

}
