package com.ltst.prizeword.crossword.wordcheck;

import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nonnull;

import static com.ltst.prizeword.crossword.wordcheck.WordCompletenessChecker.*;

public class WordsGraph
{
    public @Nonnull HashSet<Integer> nodes; // индексы слов в списке
    public @Nonnull HashSet<Pair<Integer, Integer>> edges; // пересекающиеся слова

    public WordsGraph(@Nonnull HashMap<LetterCell, CrossingQuestionsPair> questionsMap)
    {
        nodes = new HashSet<Integer>();
        edges = new HashSet<Pair<Integer, Integer>>(questionsMap.size());

        for (Map.Entry<LetterCell, CrossingQuestionsPair> entry : questionsMap.entrySet())
        {
            CrossingQuestionsPair pair = entry.getValue();
            if(pair.firstQuestionIndex < 0 || pair.secondQuestionIndex < 0)
                continue; 
            nodes.add(pair.firstQuestionIndex);
            nodes.add(pair.secondQuestionIndex);
            edges.add(new Pair<Integer, Integer>(pair.firstQuestionIndex, pair.secondQuestionIndex));
        }
    }
}
