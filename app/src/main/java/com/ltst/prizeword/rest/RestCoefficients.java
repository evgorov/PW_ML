package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCoefficients
{
    private @JsonProperty("time-bonus") int timeBonus;
    private @JsonProperty("friend-bonus") int friendBonus;
    private @JsonProperty("free-base-score") int freeBaseScore;
    private @JsonProperty("gold-base-score") int goldBaseScore;
    private @JsonProperty("brilliant-base-score") int brilliantBaseScore;
    private @JsonProperty("silver1-base-score") int silver1BaseScore;
    private @JsonProperty("silver2-base-score") int silver2BaseScore;

    public RestCoefficients(){}

    public int getTimeBonus()
    {
        return timeBonus;
    }

    public void setTimeBonus(int timeBonus)
    {
        this.timeBonus = timeBonus;
    }

    public int getFriendBonus()
    {
        return friendBonus;
    }

    public void setFriendBonus(int friendBonus)
    {
        this.friendBonus = friendBonus;
    }

    public int getFreeBaseScore()
    {
        return freeBaseScore;
    }

    public void setFreeBaseScore(int freeBaseScore)
    {
        this.freeBaseScore = freeBaseScore;
    }

    public int getGoldBaseScore()
    {
        return goldBaseScore;
    }

    public void setGoldBaseScore(int goldBaseScore)
    {
        this.goldBaseScore = goldBaseScore;
    }

    public int getBrilliantBaseScore()
    {
        return brilliantBaseScore;
    }

    public void setBrilliantBaseScore(int brilliantBaseScore)
    {
        this.brilliantBaseScore = brilliantBaseScore;
    }

    public int getSilver1BaseScore()
    {
        return silver1BaseScore;
    }

    public void setSilver1BaseScore(int silver1BaseScore)
    {
        this.silver1BaseScore = silver1BaseScore;
    }

    public int getSilver2BaseScore()
    {
        return silver2BaseScore;
    }

    public void setSilver2BaseScore(int silver2BaseScore)
    {
        this.silver2BaseScore = silver2BaseScore;
    }
}
