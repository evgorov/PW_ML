package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestPuzzleQuestion
{
    private @JsonProperty("column") int column;
    private @JsonProperty("row") int row;
    private @JsonProperty("question_text") String questionText;
    private @JsonProperty("answer") String answer;
    private @JsonProperty("answer_position") String answerPosition;

    public RestPuzzleQuestion (){}

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }

    public int getRow()
    {
        return row;
    }

    public void setRow(int row)
    {
        this.row = row;
    }

    public String getQuestionText()
    {
        return questionText;
    }

    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getAnswerPosition()
    {
        return answerPosition;
    }

    public void setAnswerPosition(String answerPosition)
    {
        this.answerPosition = answerPosition;
    }

}
