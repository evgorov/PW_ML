package com.ltst.prizeword.crossword.wordcheck;

import android.graphics.Point;
import android.util.Pair;
import android.util.SparseBooleanArray;

import com.ltst.prizeword.crossword.engine.AnswerLetterPointIterator;
import com.ltst.prizeword.crossword.engine.PuzzleResources;
import com.ltst.prizeword.crossword.engine.PuzzleTileState;
import com.ltst.prizeword.crossword.model.PuzzleQuestion;

import org.omich.velo.events.PairListeners;
import org.omich.velo.handlers.IListenerInt;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WordCompletenessChecker
{

    public static void checkWords(int sourceQuestionIndex,
                                  @Nonnull PuzzleResources resources,
                                  @Nonnull IListenerInt crossingQuestionCorrectHandler)
    {
        WordsGraph graph = resources.getWordsGraph();
        if(graph == null)
            return;
        LinkedList<Integer> nodesQueue = new LinkedList<Integer>();
        SparseBooleanArray processedNodes = new SparseBooleanArray();
        for (Integer node : graph.nodes)
        {
            processedNodes.append(node, false);
        }

        nodesQueue.addFirst(sourceQuestionIndex);

        while (!nodesQueue.isEmpty())
        {
            Integer node = nodesQueue.getLast();
            for (Pair<Integer,Integer> edge : graph.edges)
            {
                if(edge.first.equals(node))
                {
                    int crossingQuestionIndex = edge.second;
                    if(!processedNodes.get(crossingQuestionIndex))
                    {
                        if(checkQuestion(crossingQuestionIndex, resources))
                        {
                            crossingQuestionCorrectHandler.handle(crossingQuestionIndex);
                            nodesQueue.addFirst(edge.second);
                            processedNodes.append(edge.second, true);
                        }
                    }
                }
                else if(edge.second.equals(node))
                {
                    int crossingQuestionIndex = edge.first;
                    if(!processedNodes.get(crossingQuestionIndex))
                    {
                        if(checkQuestion(crossingQuestionIndex, resources))
                        {
                            crossingQuestionCorrectHandler.handle(crossingQuestionIndex);
                            nodesQueue.addFirst(crossingQuestionIndex);
                            processedNodes.append(crossingQuestionIndex, true);
                        }
                    }
                }
            }
        }
    }

    private static boolean checkQuestion(int index, @Nonnull PuzzleResources resources)
    {
        List<PuzzleQuestion> questions = resources.getPuzzleQuestions();
        if (questions == null)
        {
            return false;
        }
        PuzzleQuestion q = questions.get(index);
        if(q.isAnswered)
            return false;
        Point answerStart = PuzzleTileState.ArrowType.positionToPoint(q.getAnswerPosition(), q.column - 1, q.row - 1);
        AnswerLetterPointIterator iter = null;
        if (answerStart != null)
        {
            PuzzleTileState arrowTileState = resources.getPuzzleState(answerStart.x, answerStart.y);
            if (arrowTileState == null)
            {
                return false;
            }
            int arrowType = arrowTileState.getArrowByQuestionIndex(index);
            if(arrowType == PuzzleTileState.ArrowType.NO_ARROW)
                return false;

            iter = new AnswerLetterPointIterator(answerStart,
                    PuzzleTileState.AnswerDirection.getDirectionByArrow(arrowType), q.answer);
        }
        if (iter == null)
            return false;

        while (iter.hasNext())
        {
            Point next = iter.next();
            @Nullable PuzzleTileState state = resources.getPuzzleState(next.x, next.y);
            if (state == null)
            {
                return false;
            }
            if(state.getLetterState() != PuzzleTileState.LetterState.LETTER_CORRECT)
                return false;
        }
        return true;
    }

    public static class CrossingQuestionsPair
    {
        public int firstQuestionIndex = -1;
        public int secondQuestionIndex = -1;

        public CrossingQuestionsPair(){}

        public void putIndex(int index)
        {
            if(firstQuestionIndex == -1)
                firstQuestionIndex = index;
            else if(secondQuestionIndex == -1)
                secondQuestionIndex = index;
        }
    }

    public static class LetterCell extends Pair<Integer, Integer>
    {
        public LetterCell(int column, int row)
        {
            super(column, row);
        }
    }
}
