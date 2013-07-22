package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestPuzzle
{
    private @JsonProperty("id") String puzzleId;
    private @JsonProperty("name") String name;
    private @JsonProperty("issuedAt") String issuedAt;
    private @JsonProperty("base_score") int baseScore;
    private @JsonProperty("time_given") int timeGiven;
    private @JsonProperty("height") int height;
    private @JsonProperty("width") int width;
    private @JsonProperty("questions") List<RestPuzzleQuestion> questions;

    public RestPuzzle(){}

    public String getPuzzleId()
    {
        return puzzleId;
    }

    public void setPuzzleId(String puzzleId)
    {
        this.puzzleId = puzzleId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIssuedAt()
    {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt)
    {
        this.issuedAt = issuedAt;
    }

    public int getBaseScore()
    {
        return baseScore;
    }

    public void setBaseScore(int baseScore)
    {
        this.baseScore = baseScore;
    }

    public int getTimeGiven()
    {
        return timeGiven;
    }

    public void setTimeGiven(int timeGiven)
    {
        this.timeGiven = timeGiven;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public List<RestPuzzleQuestion> getQuestions()
    {
        return questions;
    }

    public void setQuestions(List<RestPuzzleQuestion> questions)
    {
        this.questions = questions;
    }
}
