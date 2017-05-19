package org.iem.vocabulary_trainer.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import org.iem.vocabulary_trainer.data.BasicVocabData;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper implements UtilsContract.Database {
    private static final String LOG_TAG = "VT_" + Database.class.getSimpleName();

    // database settings
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "vocabulary.db";

    // database tables
    private static final class Vocabs implements BaseColumns {
        private static final String TABLE_NAME = "vocabulary";
        private static final String ORIGIN_FIELD = "origin";
        private static final String TRANSLATION_FIELD = "translation";
        private static final String CREATED_AT_FIELD = "created_at";
        private static final String BOX_FIELD = "actual_box";
        private static final String LAST_LEARNED_FIELD = "last_learned";
    }

    // create-table commands
    private static final String USER_TABLE_CREATE =
            "CREATE TABLE " + Vocabs.TABLE_NAME + " (" +
                    Vocabs._ID + " INTEGER PRIMARY KEY, " +
                    Vocabs.ORIGIN_FIELD + " TEXT NOT NULL, " +
                    Vocabs.TRANSLATION_FIELD + " TEXT NOT NULL, " +
                    Vocabs.CREATED_AT_FIELD + " INTEGER, " +
                    Vocabs.BOX_FIELD + " INTEGER, " +
                    Vocabs.LAST_LEARNED_FIELD + " INTEGER)";

    // init
    Database(Context applicationContext) {
        super(applicationContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER_TABLE_CREATE);
    }

    // if the version number increases
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading Database. Dropping all data.");
        db.execSQL("DROP TABLE IF EXISTS " + Vocabs.TABLE_NAME);
        onCreate(db);
    }

    // returns all vocabulary data from the database
    @Override
    public List<BasicVocabData> getAllBasicVocabData() {
        SQLiteDatabase db = this.getReadableDatabase();
        // get entries
        Cursor vocabEntry = db.query(
                Vocabs.TABLE_NAME,
                new String[] {Vocabs._ID, Vocabs.ORIGIN_FIELD, Vocabs.TRANSLATION_FIELD,
                        Vocabs.CREATED_AT_FIELD, Vocabs.BOX_FIELD, Vocabs.LAST_LEARNED_FIELD},
                null, null, null, null, null
        );
        // extract data from entries
        List<BasicVocabData> result = new ArrayList<>();
        if (vocabEntry.getCount() != 0) {
            for (vocabEntry.moveToFirst(); !vocabEntry.isAfterLast(); vocabEntry.moveToNext()) {
                try {
                    BasicVocabData newEntry = new BasicVocabData();
                    int index = vocabEntry.getColumnIndexOrThrow(Vocabs._ID);
                    newEntry.id = vocabEntry.getInt(index);
                    index = vocabEntry.getColumnIndexOrThrow(Vocabs.ORIGIN_FIELD);
                    newEntry.origin = vocabEntry.getString(index);
                    index = vocabEntry.getColumnIndexOrThrow(Vocabs.TRANSLATION_FIELD);
                    newEntry.translation = vocabEntry.getString(index);
                    result.add(newEntry);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, "Vocabulary column missing", e);
                }
            }
        }
        vocabEntry.close();
        return result;
    }

    // saves/updates the entries in the database (updates, if id matches)
    @Override
    public void saveBasicEntries(List<BasicVocabData> entries) {
        SQLiteDatabase db = this.getWritableDatabase();
        // get saved entries
        String[] givenIds = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            givenIds[i] = Integer.toString(entries.get(i).id);
        }
        Cursor existingEntry = null;
        try {
            existingEntry = db.query(
                    Vocabs.TABLE_NAME,
                    new String[]{Vocabs._ID},
                    Vocabs._ID + " = ?",
                    givenIds,
                    null, null, null
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Couldn't load existing entries", e);
        }
        // make a list of those ids
        List<Integer> savedIds = new ArrayList<>();
        if (existingEntry != null && existingEntry.getCount() != 0) {
            for (existingEntry.moveToFirst(); !existingEntry.isAfterLast(); existingEntry.moveToNext()) {
                try {
                    int index = existingEntry.getColumnIndexOrThrow(Vocabs._ID);
                    savedIds.add(existingEntry.getInt(index));
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, "ID column from existing entries missing", e);
                }
            }
        }
        if (existingEntry != null) existingEntry.close();
        // delete entry, that has -1 as ID (just in case - otherwise it might not distinguish
        // correctly between create and update)
        if (savedIds.contains(-1)) {
            db.delete(Vocabs.TABLE_NAME,
                    Vocabs._ID + " = ?",
                    new String[] {Integer.toString(-1)});
        }
        // save or update all entries
        for (BasicVocabData entry : entries) {
            ContentValues values = new ContentValues();
            if (entry.origin != null) values.put(Vocabs.ORIGIN_FIELD, entry.origin);
            if (entry.translation != null) values.put(Vocabs.TRANSLATION_FIELD, entry.translation);

            try {
                int entryId = entry.id;
                if (savedIds.contains(entryId)) {
                    // This entry already exists in storage, so use update.
                    Log.d(LOG_TAG, "Updating entry: " + entryId);
                    db.update(
                            Vocabs.TABLE_NAME,
                            values,
                            Vocabs._ID + " = ?",
                            new String[]{Integer.toString(entryId)}
                    );
                } else {
                    // This entry isn't in storage yet so use insert
                    if (entryId != -1) values.put(Vocabs._ID, entryId);
                    values.put(Vocabs.CREATED_AT_FIELD, GlobalData.getTimestamp());
                    entryId = (int) db.insertOrThrow(Vocabs.TABLE_NAME, null, values);
                    Log.d(LOG_TAG, "Adding entry: " + entryId);
                }
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Couldn't save entry", e);
            }
        }
    }
}
