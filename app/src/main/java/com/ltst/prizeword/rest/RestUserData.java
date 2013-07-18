package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class RestUserData
{
    private @JsonProperty("name") String mName;
    private @JsonProperty("surname") String mSurname;
    private @JsonProperty("userpic") String mUserpicUrl;
    private @JsonProperty("email") String mEmail;
    private @JsonProperty("birthdate") String mBirthDate;
    private @JsonProperty("city") String mCity;
    private @JsonProperty("solved") int mSolved;
    private @JsonProperty("position") int mPosition;
    private @JsonProperty("month_score") int mMonthScore;
    private @JsonProperty("high_score") int mHighScore;
    private @JsonProperty("dynamics") int mDynamics;
    private @JsonProperty("hints") int mHints;
    private @JsonProperty("providers") List<RestUserProvider> mProviders;
    private @JsonProperty("created_at") String createdAt;
    private @JsonProperty("id") String id;

    public  RestUserData(){}

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getSurname()
    {
        return mSurname;
    }

    public void setSurname(String surname)
    {
        mSurname = surname;
    }

    public String getUserpicUrl()
    {
        return mUserpicUrl;
    }

    public void setUserpicUrl(String userpicUrl)
    {
        mUserpicUrl = userpicUrl;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String email)
    {
        mEmail = email;
    }

    public String getBirthDate()
    {
        return mBirthDate;
    }

    public void setBirthDate(String birthDate)
    {
        mBirthDate = birthDate;
    }

    public String getCity()
    {
        return mCity;
    }

    public void setCity(String city)
    {
        mCity = city;
    }

    public int getSolved()
    {
        return mSolved;
    }

    public void setSolved(int solved)
    {
        mSolved = solved;
    }

    public int getPosition()
    {
        return mPosition;
    }

    public void setPosition(int position)
    {
        mPosition = position;
    }

    public int getMonthScore()
    {
        return mMonthScore;
    }

    public void setMonthScore(int monthScore)
    {
        mMonthScore = monthScore;
    }

    public int getHighScore()
    {
        return mHighScore;
    }

    public void setHighScore(int highScore)
    {
        mHighScore = highScore;
    }

    public int getDynamics()
    {
        return mDynamics;
    }

    public void setDynamics(int dynamics)
    {
        mDynamics = dynamics;
    }

    public int getHints()
    {
        return mHints;
    }

    public void setHints(int hints)
    {
        mHints = hints;
    }

    public List<RestUserProvider> getProviders()
    {
        return mProviders;
    }

    public void setProviders(List<RestUserProvider> providers)
    {
        mProviders = providers;
    }



    @JsonIgnoreProperties
    public static class RestUserProvider
    {
        private @JsonProperty("provider_id") String id;
        private @JsonProperty("provider_name") String name;
        private @JsonProperty("provider_token") String token;

        public RestUserProvider(){}

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getToken()
        {
            return token;
        }

        public void setToken(String token)
        {
            this.token = token;
        }
    }

    @JsonIgnoreProperties
    public static class RestUserDataHolder
    {
        private @JsonProperty("session_key") String mSessionKey;
        private @JsonProperty("me") RestUserData mUserData;
        private String StatusCode;

        public RestUserDataHolder()
        {
        }

        public RestUserData getUserData()
        {
            return mUserData;
        }

        public void setUserData(RestUserData userData)
        {
            mUserData = userData;
        }

        public String getSessionKey()
        {
            return mSessionKey;
        }

        public void setSessionKey(String sessionKey)
        {
            mSessionKey = sessionKey;
        }

        public String getStatusCode() {
            return StatusCode;
        }

        public void setStatusCode(String statusCode) {
            StatusCode = statusCode;
        }
    }
}
