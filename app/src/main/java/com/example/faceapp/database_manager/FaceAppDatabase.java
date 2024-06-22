package com.example.faceapp.database_manager;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceAppDatabase implements EmbeddingCallback{
    private Context context;
    private DBHelper dbHelper;
    private Gson gson;
    private AppDirectory fileManager;

    public FaceAppDatabase(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        gson = new Gson();
        fileManager = new AppDirectory(context);

    }

    public void save(ArrayList<String> folderList){
        dbHelper.clearAllData();
        for (String name : folderList){
            addID(name);
        }
        for (String name : folderList){
            fileManager.extractEmbeddingsFromImages(name, this);
        }

    }



    @Override
    public void onEmbeddingExtracted(float[] embedding, String name) {
        int id = getIDbyName(name);

        this.addEmbedding(embedding, id);

        Log.d("onEmbeddingExtracted", "Embedding extracted: " + id + Arrays.toString(embedding));
    }
    @Override
    public void onExtractionFailed(Exception e) {
        Log.e("onExtractionFailed", "Failed to extract embedding", e);
    }



    public void addID(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_PERSON_NAME, name);
        db.insert(DBHelper.TABLE_PEOPLE, null, values);
        db.close();
    }

    public int getIDbyName(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBHelper.COLUMN_PERSON_ID + " FROM " + DBHelper.TABLE_PEOPLE + " WHERE " + DBHelper.COLUMN_PERSON_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        int personId = -1;
        if (cursor.moveToFirst()) {
            personId = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PERSON_ID));
        }
        cursor.close();
        db.close();
        return personId;
    }


    public void addEmbedding(float[] embedding, int personId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        String embeddingJson = gson.toJson(embedding);
        values.put(DBHelper.COLUMN_EMBEDDING_PERSON_ID, personId);
        values.put(DBHelper.COLUMN_EMBEDDING, embeddingJson);
        db.insert(DBHelper.TABLE_EMBEDDINGS, null, values);
        db.close();
    }



    public Cursor getAllPeople() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.TABLE_PEOPLE;
        Cursor result = db.rawQuery(query, null);
        db.close();
        return result;
    }


    public Cursor _getEmbeddingsByPersonId(SQLiteDatabase db, long personId){
        String query = "SELECT * FROM " + DBHelper.TABLE_EMBEDDINGS + " WHERE " + DBHelper.COLUMN_EMBEDDING_PERSON_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(personId)});
    }
    public Cursor getEmbeddingsByPersonId(long personId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor result = _getEmbeddingsByPersonId(db, personId);
        db.close();
        return result;
    }

    public int deletePerson(long personId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DBHelper.TABLE_EMBEDDINGS, DBHelper.COLUMN_EMBEDDING_PERSON_ID + " = ?", new String[]{String.valueOf(personId)});
        int result = db.delete(DBHelper.TABLE_PEOPLE, DBHelper.COLUMN_PERSON_ID + " = ?", new String[]{String.valueOf(personId)});
        db.close();
        return result;
    }

    public int deleteEmbedding(long embeddingId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int result = db.delete(DBHelper.TABLE_EMBEDDINGS, DBHelper.COLUMN_EMBEDDING_ID + " = ?", new String[]{String.valueOf(embeddingId)});
        db.close();
        return result;
    }
}
