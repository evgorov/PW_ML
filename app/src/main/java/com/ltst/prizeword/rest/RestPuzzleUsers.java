package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestPuzzleUsers
{
    private @JsonProperty("me") RestUserData mUserData;
    private @JsonProperty("users") List<RestUserData> mUsers;

    public RestPuzzleUsers(){}

    public RestUserData getMe()
    {
        return mUserData;
    }

    public void setUserData(RestUserData userData)
    {
        mUserData = userData;
    }

    public List<RestUserData> getUsers()
    {
        return mUsers;
    }

    public void setUsers(List<RestUserData> users)
    {
        mUsers = users;
    }
}
