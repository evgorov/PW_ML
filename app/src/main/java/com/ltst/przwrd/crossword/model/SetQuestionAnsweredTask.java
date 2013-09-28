package com.ltst.przwrd.crossword.model;

import android.content.Intent;
import android.os.Bundle;

import com.ltst.przwrd.db.DbService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetQuestionAnsweredTask implements DbService.IDbTask
{
    public static final @Nonnull String BF_QUESTION_ID = "SetQuestionAnsweredTask.questionId";
    public static final @Nonnull String BF_ANSWERED = "SetQuestionAnsweredTask.answered";
    private static final @Nonnull String BF_INTENT_TYPE = "SetQuestionAnsweredTask.intentType";
    private static final @Nonnull String BF_INTENT_TYPE_SINGLE = "SetQuestionAnsweredTask.single";
    private static final @Nonnull String BF_INTENT_TYPE_MULTIPLE = "SetQuestionAnsweredTask.multiple";

    public static final @Nonnull
    Intent createSingleIntent(long questionId, boolean answered)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_QUESTION_ID, questionId);
        intent.putExtra(BF_ANSWERED, answered);
        intent.putExtra(BF_INTENT_TYPE, BF_INTENT_TYPE_SINGLE);
        return intent;
    }

    public static final @Nonnull
    Intent createMultipleIntent(long[] questionsIds, boolean answered)
    {
        Intent intent = new Intent();
        intent.putExtra(BF_QUESTION_ID, questionsIds);
        intent.putExtra(BF_ANSWERED, answered);
        intent.putExtra(BF_INTENT_TYPE, BF_INTENT_TYPE_MULTIPLE);
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
        @Nullable String intentType = extras.getString(BF_INTENT_TYPE);
        if (intentType == null)
        {
            return null;
        }

        if(intentType.equals(BF_INTENT_TYPE_SINGLE))
        {
            long questionId = extras.getLong(BF_QUESTION_ID);
            boolean answered = extras.getBoolean(BF_ANSWERED);
            dbTaskEnv.dbw.setQuestionAnswered(questionId, answered);
        }
        if(intentType.equals(BF_INTENT_TYPE_MULTIPLE))
        {
            long[] questionId = extras.getLongArray(BF_QUESTION_ID);
            boolean answered = extras.getBoolean(BF_ANSWERED);
            dbTaskEnv.dbw.setQuestionAnswered(questionId, answered);
        }
        return null;
    }
}
