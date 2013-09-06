package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

public class RestNews
{
    private @JsonProperty("message1") String message1;
    private @JsonProperty("message2") String message2;
    private @JsonProperty("message3") String message3;

    public RestNews()
    {
    }

    public String getMessage1()
    {
        return message1;
    }

    public void setMessage1(String message1)
    {
        this.message1 = message1;
    }

    public String getMessage2()
    {
        return message2;
    }

    public void setMessage2(String message2)
    {
        this.message2 = message2;
    }

    public String getMessage3()
    {
        return message3;
    }

    public void setMessage3(String message3)
    {
        this.message3 = message3;
    }

}
