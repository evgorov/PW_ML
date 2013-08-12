package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestInviteFriend
{
    private @JsonProperty("provider") String provider;
    private @JsonProperty("id") String friendsId;
    private @JsonProperty("name") String name;
    private @JsonProperty("email") String email;
    private @JsonProperty("invite_send") boolean inviteSend;
    private @JsonProperty("invite_used") boolean inviteUsed;
    private @JsonProperty("invited_at") String invitedAt;
    private @JsonProperty("userpic_url") String userpicUrl;

    public RestInviteFriend(){}

    public String getProvider()
    {
        return provider;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public String getFriendsId()
    {
        return friendsId;
    }

    public void setFriendsId(String friendsId)
    {
        this.friendsId = friendsId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public boolean isInviteSend()
    {
        return inviteSend;
    }

    public void setInviteSend(boolean inviteSend)
    {
        this.inviteSend = inviteSend;
    }

    public boolean isInviteUsed()
    {
        return inviteUsed;
    }

    public void setInviteUsed(boolean inviteUsed)
    {
        this.inviteUsed = inviteUsed;
    }

    public String getInvitedAt()
    {
        return invitedAt;
    }

    public void setInvitedAt(String invitedAt)
    {
        this.invitedAt = invitedAt;
    }

    public String getUserpicUrl()
    {
        return userpicUrl;
    }

    public void setUserpicUrl(String userpicUrl)
    {
        this.userpicUrl = userpicUrl;
    }
}
