package com.ltst.prizeword.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.prizeword.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetQuestionAnsweredTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_QUESTION_ID = "SetQuestionAnsweredTask.questionId";
    public static final @Nonnull String BF_ANSWERED = "SetQuestionAnsweredTask.answered";

    public static final @Nonnull
    Intent createIntent(long questionId, boolean answered)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_QUESTION_ID, questionId);
        intent.putExtra(BF_ANSWERED, answered);
        return intent;
    }

    @Nullable
    @Override
    public Bundle execute(@Nonnull DbService.DbTaskEnv dbTaskEnv)
    {
        Bundle extras = dbTaskEnv.extras;
        if (extras == null)
        {
            return null;
        }
        long questionId = extras.getLong(BF_QUESTION_ID);
        boolean answered = extras.getBoolean(BF_ANSWERED);
        dbTaskEnv.dbw.setQuestionAnswered(questionId, answered);
        return null;
    }
}
