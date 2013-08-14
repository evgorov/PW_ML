package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.R;
import com.ltst.prizeword.rest.IRestClient;
import com.ltst.prizeword.rest.RestClient;
import com.ltst.prizeword.rest.RestPuzzleQuestion;
import com.ltst.prizeword.rest.RestPuzzleUserData;

import org.codehaus.jackson.map.ObjectMapper;
import org.omich.velo.bcops.BcTaskHelper;
import org.omich.velo.bcops.simple.IBcTask;
import org.omich.velo.cast.NonnullableCasts;
import org.omich.velo.log.Log;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdatePuzzleUserDataOnServerTask implements IBcTask
{
    public static final @Nonnull String BF_SESSION_KEY = "UpdatePuzzleUserDataOnServerTask.sessionKey";
    public static final @Nonnull String BF_PUZZLE_ID = "UpdatePuzzleUserDataOnServerTask.puzzleId";
    public static final @Nonnull String BF_QUESTIONS = "UpdatePuzzleUserDataOnServerTask.questions";
    public static final @Nonnull String BF_TIMELEFT = "UpdatePuzzleUserDataOnServerTask.timeLeft";
    public static final @Nonnull String BF_SCORE = "UpdatePuzzleUserDataOnServerTask.score";
    public static final @Nonnull String BF_STATUS = "UpdatePuzzleUserDataOnServerTask.status";

    public static final @Nonnull Intent createIntent(@Nonnull String sessionKey, @Nonnull String puzzleId,
                                                     int timeLeft, int score,
                                                     @Nullable ArrayList<PuzzleQuestion> questions)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_SESSION_KEY, sessionKey);
        intent.putExtra(BF_PUZZLE_ID, puzzleId);
        intent.putExtra(BF_TIMELEFT, timeLeft);
        intent.putExtra(BF_SCORE, score);
        if (questions != null)
        {
            intent.putParcelableArrayListExtra(BF_QUESTIONS, questions);
        }
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull BcTaskEnv bcTaskEnv)
    {
        Bundle extras = bcTaskEnv.extras;
        if (extras == null)
        {
            return null;
        }
        @Nullable String sessionKey = extras.getString(BF_SESSION_KEY);
        @Nullable String puzzleId = extras.getString(BF_PUZZLE_ID);
        @Nullable ArrayList<PuzzleQuestion> questions = extras.getParcelableArrayList(BF_QUESTIONS);
        int timeLeft = extras.getInt(BF_TIMELEFT);
        int score = extras.getInt(BF_SCORE);

        if(!BcTaskHelper.isNetworkAvailable(bcTaskEnv.context))
        {
            bcTaskEnv.bcToaster.showToast(
                    NonnullableCasts.getStringOrEmpty(
                            bcTaskEnv.context.getString(R.string.msg_no_internet)));
        }
        else
        if (sessionKey!= null && puzzleId!= null && questions != null)
        {
            String jsonPuzzleUserData = parseJsonUserData(questions, puzzleId, timeLeft, score);
            try
            {
                IRestClient client = RestClient.create();
                HttpStatus status = client.putPuzzleUserData(sessionKey, puzzleId, jsonPuzzleUserData);
                Bundle bundle = new Bundle();
                bundle.putInt(BF_STATUS, status.value());
                return bundle;
            }
            catch (Throwable e)
            {
                Log.e(e.getMessage());
                Log.i("Can't load data from internet"); //$NON-NLS-1$
                return null;
            }
        }
        return null;
    }

    private @Nonnull String parseJsonUserData(@Nonnull ArrayList<PuzzleQuestion> questions,
                                              @Nonnull String puzzleId,
                                              int timeleft, int score)
    {
        List<RestPuzzleUserData.RestSolvedQuestion> restList = new ArrayList<RestPuzzleUserData.RestSolvedQuestion>(questions.size());
        for (PuzzleQuestion question : questions)
        {
            if (question.isAnswered)
            {
                RestPuzzleUserData.RestSolvedQuestion solved = new RestPuzzleUserData.RestSolvedQuestion();
                solved.setColumn(question.column - 1);
                solved.setRow(question.row - 1);
                solved.setId(RestPuzzleUserData.getSolvedQuestionId(puzzleId, question.column, question.row));
                restList.add(solved);
            }
        }
        RestPuzzleUserData data = new RestPuzzleUserData();
        data.setScore(score);
        data.setTimeLeft(timeleft);
        data.setSolvedQuestions(restList);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            mapper.writeValue(out, data);
        } catch (IOException e)
        {
            Log.i(e.getMessage());
        }
        return new String(out.toByteArray());
    }
}
