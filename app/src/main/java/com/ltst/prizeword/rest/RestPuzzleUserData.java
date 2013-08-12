package com.ltst.prizeword.rest;

import android.util.SparseArray;

import com.ltst.prizeword.crossword.model.PuzzleQuestion;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    public static class RestSolvedQuestion
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

    public static class RestPuzzleUserDataHolder
    {
        private @Nullable RestPuzzleUserData mPuzzleUserData;
        private @JsonIgnore HttpStatus status;

        @Nullable
        public RestPuzzleUserData getPuzzleUserData()
        {
            return mPuzzleUserData;
        }

        public void setPuzzleUserData(@Nullable RestPuzzleUserData puzzleUserData)
        {
            mPuzzleUserData = puzzleUserData;
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

    public static @Nonnull
    HashSet<String> prepareQuestionIdsSet(@Nonnull List<RestSolvedQuestion> solvedQuestionsList)
    {
        @Nonnull HashSet<String> set = new HashSet<String>(solvedQuestionsList.size());
        for (RestSolvedQuestion question : solvedQuestionsList)
        {
            int col = question.column + 1;
            int row = question.row + 1;
            set.add(getQuestionCRID(col, row));
        }
        return set;
    }
    
    // get question id by its column/row
    public static @Nonnull String getQuestionCRID(int col, int row)
    {
        return  col + "_" + row;
    }

    public static void checkQuestionOnAnswered(@Nonnull PuzzleQuestion q, @Nullable HashSet<String> solvedQuestionIdsSet)
    {
        if (solvedQuestionIdsSet == null)
        {
            return;
        }
        String id = getQuestionCRID(q.column, q.row);
        if(solvedQuestionIdsSet.contains(id))
        {
            q.isAnswered = true;
        }
    }
}
