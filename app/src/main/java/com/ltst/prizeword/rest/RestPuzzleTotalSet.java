package com.ltst.prizeword.rest;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.http.HttpStatus;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestPuzzleTotalSet
{
    private @JsonProperty("id") String id;
    private @JsonProperty("name") String name;
    private @JsonProperty("bought") boolean isBought;
    private @JsonProperty("type") String type;
    private @JsonProperty("month") int month;
    private @JsonProperty("year") int year;
    private @JsonProperty("created_at") String createdAt;
    private @JsonProperty("published") boolean isPublished;
    private @JsonProperty("puzzles") List<RestPuzzle> puzzles;

    public RestPuzzleTotalSet()
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

    public int getMonth()
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

    public List<RestPuzzle> getPuzzles()
    {
        return puzzles;
    }

    public void setPuzzles(List<RestPuzzle> puzzles)
    {
        this.puzzles = puzzles;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestPuzzleSetsHolder
    {
        private @JsonProperty() List<RestPuzzleTotalSet> mPuzzleSets;
        private @JsonIgnore HttpStatus mHttpStatus;

        public RestPuzzleSetsHolder(){}

        public List<RestPuzzleTotalSet> getPuzzleSets()
        {
            return mPuzzleSets;
        }

        public void setPuzzleSets(List<RestPuzzleTotalSet> puzzleSets)
        {
            mPuzzleSets = puzzleSets;
        }

        public void addPuzzleSet(RestPuzzleTotalSet set)
        {
            mPuzzleSets.add(set);
        }


        public HttpStatus getHttpStatus()
        {
            return mHttpStatus;
        }

        public void setHttpStatus(HttpStatus httpStatus)
        {
            mHttpStatus = httpStatus;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RestPuzzleOneSetHolder
    {
        private @JsonProperty() RestPuzzleTotalSet mPuzzleSet;
        private @JsonIgnore HttpStatus mHttpStatus;

        public RestPuzzleOneSetHolder(){}

        public RestPuzzleTotalSet getPuzzleSet()
        {
            return mPuzzleSet;
        }

        public void setPuzzleSet(RestPuzzleTotalSet puzzleSet)
        {
            mPuzzleSet = puzzleSet;
        }

        public HttpStatus getHttpStatus()
        {
            return mHttpStatus;
        }

        public void setHttpStatus(HttpStatus httpStatus)
        {
            mHttpStatus = httpStatus;
        }
    }
}
