package com.example.faceapp.database_manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FaceAppDatabase.db";
    private static final int DATABASE_VERSION = 1;


    // Bảng People
    public static final String TABLE_PEOPLE = "People";
    public static final String COLUMN_PERSON_ID = "id";
    public static final String COLUMN_PERSON_NAME = "name";

    // Bảng Embeddings
    public static final String TABLE_EMBEDDINGS = "Embeddings";
    public static final String COLUMN_EMBEDDING_ID = "embedding_id";
    public static final String COLUMN_EMBEDDING_PERSON_ID = "id";
    public static final String COLUMN_EMBEDDING = "embedding";


    private static final String SQL_CREATE_NAMES_TABLE =
            "CREATE TABLE " + TABLE_PEOPLE + " (" +
                    COLUMN_PERSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PERSON_NAME + " TEXT)";

    private static final String SQL_CREATE_EMBEDDINGS_TABLE =
            "CREATE TABLE " + TABLE_EMBEDDINGS + " (" +
                    COLUMN_EMBEDDING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMBEDDING_PERSON_ID + " INTEGER, " +
                    COLUMN_EMBEDDING + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_EMBEDDING_PERSON_ID + ") REFERENCES " + TABLE_PEOPLE + "(" + COLUMN_PERSON_ID + "))";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NAMES_TABLE);
        db.execSQL(SQL_CREATE_EMBEDDINGS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMBEDDINGS);
        onCreate(db);
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_PEOPLE);
            db.execSQL("DELETE FROM " + TABLE_EMBEDDINGS);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

}
