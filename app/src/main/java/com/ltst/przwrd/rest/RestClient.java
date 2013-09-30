package com.ltst.przwrd.rest;

import android.content.Context;

import com.ltst.przwrd.app.SharedPreferencesHelper;
import com.ltst.przwrd.app.SharedPreferencesValues;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RestClient implements IRestClient
{
    private @Nonnull Context mContext;

    public static IRestClient create(@Nonnull Context context)
    {
        return new RestClient(context);
    }

    private @Nonnull RestTemplate restTemplate;

    private RestClient(@Nonnull Context context)
    {
        this.mContext = context;
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
        ResponseEntity<RestUserData.RestUserDataHolder> responseEntity = restTemplate.exchange(RestParams.URL_GET_USER_DATA, HttpMethod.GET, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);

        long currentDate = responseEntity.getHeaders().getDate();
        SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(mContext);
        helper.putLong(SharedPreferencesValues.SP_CURRENT_DATE, currentDate).commit();

        RestUserData.RestUserDataHolder holder = responseEntity.getBody();
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
            Log.e(e.getMessage());
        } catch (IOException e)
        {
            Log.e(e.getMessage());
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
            Log.e(e.getMessage());
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
            Log.e(e.getMessage());
        } finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(RestParams.SC_ERROR);
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
    public RestPuzzleTotalSet.RestPuzzleOneSetHolder postBuySet(@Nonnull String sessionKey, @Nonnull String serverSetId, @Nonnull String receiptData, @Nonnull String signature)
    {
        @Nonnull String url = String.format(RestParams.URL_POST_BUY_PUZZLE_SET, serverSetId);
        @Nonnull String param = receiptData.replace("\"", "\\\"");

        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.RECEIPT_DATA, param);
        urlVariables.put(RestParams.SIGNATURE, signature);

//        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
//        messageConverters.add(new FormHttpMessageConverter());
//        messageConverters.add(new StringHttpMessageConverter());
//        restTemplate.setMessageConverters(messageConverters);

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
//        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        @Nullable ResponseEntity<RestPuzzleTotalSet> entity = null;
        @Nullable RestPuzzleTotalSet.RestPuzzleOneSetHolder holder = new RestPuzzleTotalSet.RestPuzzleOneSetHolder();
        try
        {
            entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, RestPuzzleTotalSet.class, urlVariables);
            holder.setPuzzleSet(entity.getBody());
            holder.setHttpStatus(entity.getStatusCode());
            return holder;
        } catch (HttpClientErrorException e)
        {
            Log.e(e.getMessage());
        } catch (Exception e)
        {
            Log.e(e.getMessage());
        }
        holder = new RestPuzzleTotalSet.RestPuzzleOneSetHolder();
        holder.setHttpStatus(HttpStatus.valueOf(RestParams.SC_ERROR));
        return holder;
    }

    @Nullable
    @Override
    public RestNews getNews(@Nonnull String sessionKey)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestNews> holder =
                restTemplate.exchange(RestParams.URL_GET_NEWS, HttpMethod.GET, requestEntity, RestNews.class, urlVariables);
        return holder.getBody();
    }

    @Nullable
    @Override
    public RestPuzzleTotalSet.RestPuzzleSetsHolder getTotalPublishedSets(@Nonnull String sessionKey, int year, int month)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.MODE, RestParams.MODE_LONG
        );
        urlVariables.put(RestParams.YEAR, year);
        urlVariables.put(RestParams.MONTH, month);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestPuzzleTotalSet[]> entity = restTemplate.exchange(RestParams.URL_GET_PUBLISHED_SETS_SHORT, HttpMethod.GET, requestEntity, RestPuzzleTotalSet[].class, urlVariables);
        RestPuzzleTotalSet.RestPuzzleSetsHolder holder = new RestPuzzleTotalSet.RestPuzzleSetsHolder();
        List<RestPuzzleTotalSet> sets = Arrays.asList(entity.getBody());
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


    @Nullable
    @Override
    public RestInviteFriend.RestInviteFriendHolder getFriendsData(@Nonnull String sessionKey, @Nonnull String providerName)
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
        ResponseEntity<RestInviteFriend[]> entity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestInviteFriend[].class, urlVariables);
        RestInviteFriend.RestInviteFriendHolder holder = new RestInviteFriend.RestInviteFriendHolder();
        List<RestInviteFriend> friends = Arrays.asList(entity.getBody());
        holder.setFriends(friends);
        holder.setStatus(entity.getStatusCode());
        return holder;
    }

    @Override
    public RestInviteFriend.RestInviteFriendHolder sendInviteToFriends(@Nonnull String sessionKey, @Nonnull String providerName, @Nonnull String ids)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.USER_PUZZLE_IDS, ids);
        @Nonnull String url = Strings.EMPTY;
        if (providerName.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.URL_POST_VK_FRIEND_INVITE;
        } else if (providerName.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.URL_POST_FB_FRIEND_INVITE;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        //HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        //ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,String.class, urlVariables);
        URI uri = restTemplate.postForLocation(url, null, urlVariables);

        RestInviteFriend.RestInviteFriendHolder holder = new RestInviteFriend.RestInviteFriendHolder();
        //holder.setStatus(entity.getStatusCode());
        return holder;
    }


    @Nullable
    @Override
    public RestPuzzleUserData.RestPuzzleUserDataHolder getPuzzleUserData(@Nonnull String sessionKey, @Nonnull String puzzleId)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        String url = String.format(RestParams.URL_GET_PUZZLE_USERDATA, puzzleId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<String> entity = null;
        RestPuzzleUserData.RestPuzzleUserDataHolder holder = new RestPuzzleUserData.RestPuzzleUserDataHolder();
        try
        {
            entity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class, urlVariables);
        } catch (HttpClientErrorException e)
        {
            Log.e(e.getMessage());
        } finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(404);
                holder.setStatus(status);
                return holder;
            }
        }
        @Nullable RestPuzzleUserData data = null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory();
        try
        {
            JsonParser jsonParser = jsonFactory.createJsonParser(entity.getBody());
            data = objectMapper.readValue(jsonParser, RestPuzzleUserData.class);
        } catch (IOException e)
        {
            Log.e(e.getMessage());
        }

        holder.setPuzzleUserData(data);
        holder.setStatus(entity.getStatusCode());
        return holder;
    }

    @Override
    public HttpStatus putPuzzleUserData(@Nonnull String sessionKey,
                                        @Nonnull String puzzleId,
                                        @Nonnull String puzzleUserData)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.PUZZLE_DATA, puzzleUserData);
        String url = String.format(RestParams.URL_PUT_PUZZLE_USERDATA, puzzleId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        @Nullable ResponseEntity<RestUserData.RestAnswerMessageHolder> entity = null;
        try
        {
            entity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, RestUserData.RestAnswerMessageHolder.class, urlVariables);
        } catch (Exception e)
        {
            Log.e(e.getMessage());
        } finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(RestParams.SC_ERROR);
                return status;
            }
        }
        return entity.getStatusCode();
    }

    @Nullable
    @Override
    public RestUserData.RestUserDataHolder addOrRemoveHints(@Nonnull String sessionKey, int hintsToChange)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.HINTS_CHANGE, hintsToChange);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestUserData.RestUserDataHolder> holder =
                restTemplate.exchange(RestParams.URL_ADD_REMOVE_HINTS, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        holder.getBody().setStatusCode(holder.getStatusCode());
        return holder.getBody();
    }

    @Nullable
    @Override
    public RestPuzzleUsers getUsers(@Nonnull String sessionKey)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestPuzzleUsers> holder =
                restTemplate.exchange(RestParams.URL_GET_USERS, HttpMethod.GET, requestEntity, RestPuzzleUsers.class, urlVariables);
        return holder.getBody();
    }

    @Nullable
    @Override
    public RestCoefficients getCoefficients(@Nonnull String sessionKey)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestCoefficients> holder =
                restTemplate.exchange(RestParams.URL_GET_COEFFICIENTS, HttpMethod.GET, requestEntity, RestCoefficients.class, urlVariables);
        return holder.getBody();
    }

    @Override
    public HttpStatus postPuzzleScore(@Nonnull String sessionKey, @Nonnull String puzzleId, int score)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.SOURCE, puzzleId);
        urlVariables.put(RestParams.SCORE, score);
        urlVariables.put(RestParams.SOLVED, 1);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestUserData.RestUserDataHolder> entity = null;

        try
        {
            entity = restTemplate.exchange(RestParams.URL_POST_PUZZLE_SCORE, HttpMethod.POST, requestEntity, RestUserData.RestUserDataHolder.class, urlVariables);
        } catch (Exception e)
        {
            Log.e(e.getMessage());
        } finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(RestParams.SC_ERROR);
                return status;
            }
        }
        return entity.getStatusCode();
    }

    @Nullable
    @Override
    public RestInviteFriend.RestInviteFriendHolder getFriendsScoreData(@Nonnull String sessionKey, @Nonnull String providerName)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        @Nonnull String url = Strings.EMPTY;
        if (providerName.equals(RestParams.VK_PROVIDER))
        {
            url = RestParams.URL_GET_VK_INVITED_FRIEND_DATA;
        } else if (providerName.equals(RestParams.FB_PROVIDER))
        {
            url = RestParams.URL_GET_FB_INVITED_FRIEND_DATA;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        ResponseEntity<RestInviteFriend[]> entity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, RestInviteFriend[].class, urlVariables);
        RestInviteFriend.RestInviteFriendHolder holder = new RestInviteFriend.RestInviteFriendHolder();
        List<RestInviteFriend> friends = Arrays.asList(entity.getBody());
        holder.setFriends(friends);
        holder.setStatus(entity.getStatusCode());
        return holder;
    }

    @Override
    public HttpStatus shareMessageToVk(@Nonnull String sessionKey, @Nonnull String message)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.MESSAGE, message);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        try
        {
            restTemplate.exchange(RestParams.URL_SHARE_VK, HttpMethod.POST, requestEntity, String.class, urlVariables);
        } catch (Exception e)
        {
            Log.e(e.getMessage());
        }

        return null;
    }

    @Override
    public HttpStatus sendRegistrationId(@Nonnull String sessionKey, @Nonnull String registrationId)
    {
        HashMap<String, Object> urlVariables = new HashMap<String, Object>();
        urlVariables.put(RestParams.SESSION_KEY, sessionKey);
        urlVariables.put(RestParams.GCM_DEVICE_ID, registrationId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("application/json")));
        httpHeaders.set("Connection", "Close");
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(httpHeaders);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        ResponseEntity<RestInfoMessage> entity = null;
        try
        {
            entity = restTemplate.exchange(RestParams.URL_REGISTER_DEVICE, HttpMethod.POST, requestEntity, RestInfoMessage.class, urlVariables);
        }
        catch (Exception e)
        {
            Log.e(e.getMessage());
        }
        finally
        {
            if (entity == null)
            {
                HttpStatus status = HttpStatus.valueOf(RestParams.SC_ERROR);
                return status;
            }
        }
        return entity.getStatusCode();
    }
}
