package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestPuzzleUsers
{
    private @JsonProperty("me") RestUserData mUserData;
    private @JsonProperty("users") List<RestUserData> mUsers;

    public RestPuzzleUsers(){}

    public RestUserData getUserData()
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
