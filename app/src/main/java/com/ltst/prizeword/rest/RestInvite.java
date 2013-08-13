package com.ltst.prizeword.rest;

import org.springframework.http.HttpStatus;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import javax.annotation.Nullable;

public class RestInvite
{
    private @JsonProperty("friends") List<RestInviteFriend> friends;

    public RestInvite()
    {
    }

    public List<RestInviteFriend> getFriends()
    {
        return friends;
    }

    public void setFriends(List<RestInviteFriend> friends)
    {
        this.friends = friends;
    }

    public static class RestInviteHolder
    {
        private @Nullable RestInvite friends;
        private @JsonProperty HttpStatus status;

        public RestInviteHolder(){}

        @Nullable public RestInvite getFriends()
        {
            return friends;
        }

        public void setFriends(RestInvite friends)
        {
            this.friends = friends;
        }

        public HttpStatus getStatus()
        {
            return status;
        }

        public void setStatus(HttpStatus status)
        {
            this.status = status;
        }
    }
}
