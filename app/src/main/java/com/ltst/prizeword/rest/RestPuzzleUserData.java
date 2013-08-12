package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestPuzzleUserData
{
    private @JsonProperty("score") int score;
    private @JsonProperty("time_left") int timeLeft;
    private @JsonProperty("solved_questions") List<RestSolvedQuestion> solvedQuestions;

    public RestPuzzleUserData()
    {
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public int getTimeLeft()
    {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft)
    {
        this.timeLeft = timeLeft;
    }

    public List<RestSolvedQuestion> getSolvedQuestions()
    {
        return solvedQuestions;
    }

    public void setSolvedQuestions(List<RestSolvedQuestion> solvedQuestions)
    {
        this.solvedQuestions = solvedQuestions;
    }

    private class RestSolvedQuestion
    {
        private @JsonProperty("id") String id;
        private @JsonProperty("column") int column;
        private @JsonProperty("row") int row;

        private RestSolvedQuestion()
        {
        }

        private String getId()
        {
            return id;
        }

        private void setId(String id)
        {
            this.id = id;
        }

        private int getColumn()
        {
            return column;
        }

        private void setColumn(int column)
        {
            this.column = column;
        }

        private int getRow()
        {
            return row;
        }

        private void setRow(int row)
        {
            this.row = row;
        }
    }
}
