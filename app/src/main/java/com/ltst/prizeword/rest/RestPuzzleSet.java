package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class RestPuzzleSet
{
    private @JsonProperty("id") String id;
    private @JsonProperty("name") String name;
    private @JsonProperty("bought") boolean isBought;
    private @JsonProperty("type") String type;
    private @JsonProperty("month") int month;
    private @JsonProperty("year") int year;
    private @JsonProperty("created_at") String createdAt;
    private @JsonProperty("published") boolean isPublished;
    private @JsonProperty("puzzles") List<String> puzzles;

    public RestPuzzleSet()
    {
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
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isBought()
    {
        return isBought;
    }

    public void setBought(boolean bought)
    {
        isBought = bought;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getOnth()
    {
        return month;
    }

    public void setOnth(int onth)
    {
        month = onth;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public boolean isPublished()
    {
        return isPublished;
    }

    public void setPublished(boolean published)
    {
        isPublished = published;
    }

    public List<String> getPuzzles()
    {
        return puzzles;
    }

    public void setPuzzles(List<String> puzzles)
    {
        this.puzzles = puzzles;
    }
}
