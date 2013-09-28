package com.ltst.przwrd.rest;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestInviteFriend
{
    private @JsonProperty("first_name") String firstName;
    private @JsonProperty("last_name") String lastName;
    private @JsonProperty("deactivated") String deactivated;
    private @JsonProperty("online") int online;
    private @JsonProperty("user_id") long userId;
    private @JsonProperty("lists") int[] lists;
    private @JsonProperty("id") String id;
    private @JsonProperty("userpic") String userpic;
    private @JsonProperty("status") String status;

    public RestInviteFriend(){}

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getDeactivated()
    {
        return deactivated;
    }

    public void setDeactivated(String deactivated)
    {
        this.deactivated = deactivated;
    }

    public int getOnline()
    {
        return online;
    }

    public void setOnline(int online)
    {
        this.online = online;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public int[] getLists()
    {
        return lists;
    }

    public void setLists(int[] lists)
    {
        this.lists = lists;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUserpic()
    {
        return userpic;
    }

    public void setUserpic(String userpic)
    {
        this.userpic = userpic;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestInviteFriendHolder
    {
        private @JsonProperty() List<RestInviteFriend> friends;
        private @JsonIgnore HttpStatus status;

        public RestInviteFriendHolder(){}

         public List<RestInviteFriend> getFriends()
        {
            return friends;
        }

        public void setFriends(List<RestInviteFriend> friends)
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
