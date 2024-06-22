package com.example.faceapp.database_manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.faceapp.ObjectData.PersonEmbeddings;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ReadDatabase {
    private DBHelper dbHelper;
    private Gson gson;
    private Context context;

    public ReadDatabase(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        gson = new Gson();

    }

    public void addName(List<PersonEmbeddings> p) {
        SQLiteDatabase _db = dbHelper.getReadableDatabase();
        Cursor cursor= _db.rawQuery("SELECT * FROM " + DBHelper.TABLE_PEOPLE, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int personId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String personName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                p.add(new PersonEmbeddings(personName));
            } while (cursor.moveToNext());

            cursor.close();
        } else {

        }
    }
    public void fetchAllEmbeddings() {
        SQLiteDatabase _db = dbHelper.getReadableDatabase();
        Cursor cursor= _db.rawQuery("SELECT * FROM " + DBHelper.TABLE_EMBEDDINGS, null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String embedding = cursor.getString(cursor.getColumnIndexOrThrow("embedding"));
                Log.d("TAG", "fetchAllEmbeddings: "+ id +" " + gson.fromJson(embedding, float[].class));
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.d("DEX", "No people found in the database.");
        }
    }



    public List<PersonEmbeddings> getAllEmbeddingsWithNames() {
        List<PersonEmbeddings> personWithEmbedding = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        String query = "SELECT p." + DBHelper.COLUMN_PERSON_NAME + ", e." + DBHelper.COLUMN_EMBEDDING +
                " FROM " + DBHelper.TABLE_PEOPLE + " p" +
                " JOIN " + DBHelper.TABLE_EMBEDDINGS + " e" +
                " ON p." + DBHelper.COLUMN_PERSON_ID + " = e." + DBHelper.COLUMN_EMBEDDING_PERSON_ID;

        Cursor cursor = null;
        cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            addName(personWithEmbedding);
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PERSON_NAME));
                String embeddingJson = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_EMBEDDING));
                float[] embedding = gson.fromJson(embeddingJson, float[].class);

                for (PersonEmbeddings p: personWithEmbedding){
                    if (p.getName().equals(name)){
                        p.addEmbeddings(embedding);
                    }
                }


            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return personWithEmbedding;
    }



}
