package com.ltst.przwrd.rest;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.List;

import javax.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestPuzzleHolder
    {
        private @Nullable RestPuzzle puzzles;
        private @JsonIgnore HttpStatus status;

        public RestPuzzleHolder(){}

        public RestPuzzle getPuzzle()
        {
            return puzzles;
        }

        public void setPuzzles(@Nullable RestPuzzle puzzles)
        {
            this.puzzles = puzzles;
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
