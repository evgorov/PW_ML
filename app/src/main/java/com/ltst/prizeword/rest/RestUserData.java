package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties
public class RestUserData
{
    private @JsonProperty("name") String mName;
    private @JsonProperty("surname") String mSurname;
    private @JsonProperty("userpic_url") String mUserpicUrl;
    private @JsonProperty("email") String mEmail;
    private @JsonProperty("birthdate") String mBirthDate;
    private @JsonProperty("city") String mCity;
    private @JsonProperty("solved") int mSolved;
    private @JsonProperty("position") int mPosition;
    private @JsonProperty("month_score") int mMonthScore;
    private @JsonProperty("high_score") int mHighScore;
    private @JsonProperty("dynamics") int mDynamics;
    private @JsonProperty("hints") int mHints;

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

    @JsonIgnoreProperties
    class RestUserDataHolder
    {
        private @JsonProperty("me") RestUserData mUserData;

        RestUserData getUserData()
        {
            return mUserData;
        }

        void setUserData(RestUserData userData)
        {
            mUserData = userData;
        }
    }
}
