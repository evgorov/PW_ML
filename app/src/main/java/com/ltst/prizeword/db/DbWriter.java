package com.ltst.prizeword.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ltst.prizeword.login.model.UserData;

import org.omich.velo.log.Log;

import javax.annotation.Nonnull;

import static com.ltst.prizeword.db.SQLiteHelper.*;

public class DbWriter implements IDbWriter
{
    public final @Nonnull SQLiteDatabase mDb;

    public DbWriter(@Nonnull SQLiteHelper helper) throws DbException
    {
        mDb = helper.createWritableSQLiteDatabase();
        SQLiteHelper.configureSQLiteDatabase(mDb);
    }

    @Override
    public void putUser(@Nonnull UserData user)
    {
        mDb.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put(ColsUsers.NAME, user.name);
        cv.put(ColsUsers.SURNAME, user.surname);
        cv.put(ColsUsers.EMAIL, user.email);
        cv.put(ColsUsers.PROVIDER, user.provider);
        cv.put(ColsUsers.BIRTHDATE, user.bithdate);
        cv.put(ColsUsers.CITY, user.city);
        cv.put(ColsUsers.SOLVED, user.solved);
        cv.put(ColsUsers.POSITION, user.position);
        cv.put(ColsUsers.MONTH_SCORE, user.monthScore);
        cv.put(ColsUsers.HIGH_SCORE, user.highScore);
        cv.put(ColsUsers.DYNAMICS, user.dynamics);
        cv.put(ColsUsers.HINTS, user.hints);
        cv.put(ColsUsers.PREVIEW_URL, user.previewUrl);
        cv.put(ColsUsers.PREVIEW_KEY, user.previewUrl);

        try
        {
            mDb.insert(TNAME_USERS, null, cv);
        }
        catch(Throwable e)
        {
            Log.e(e);
        }
        finally
        {
            mDb.endTransaction();
        }
    }
}
