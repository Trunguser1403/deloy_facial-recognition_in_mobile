package com.example.faceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.faceapp.database_manager.FaceAppDatabase;
import com.example.faceapp.database_manager.ReadDatabase;

import java.io.File;
import java.util.ArrayList;

public class ID_Saving extends AppCompatActivity {

    private ListView folderListView;
    private Button addFolderButton, saveButton, btn_test;
    private ArrayList<String> folderList;
    private FolderAdapter folderAdapter;
    private FaceAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_saving);

        btn_test = findViewById(R.id.button_test);


        db = new FaceAppDatabase(this);

        folderListView = findViewById(R.id.id_list);
        addFolderButton = findViewById(R.id.add_id);
        saveButton = findViewById(R.id.saveButton);

        folderList = new ArrayList<>();

        folderAdapter = new FolderAdapter(this, folderList);

        folderListView.setAdapter(folderAdapter);


        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFolderDialog();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.save(folderList);
            }
        });
        ReadDatabase rb = new ReadDatabase(this);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb.getAllEmbeddingsWithNames();
            }
        });


        loadFolders();
    }













    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Folder");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = input.getText().toString().trim();
                if (!folderName.isEmpty()) {
                    addFolder(folderName);
                } else {
                    Toast.makeText(ID_Saving.this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addFolder(String folderName) {
        File folder = new File(getFilesDir(), folderName);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                folderList.add(folderName);
                folderAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Folder added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFolders() {
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {

                if (file.isDirectory()) {
                    folderList.add(file.getName());
                }
            }
            folderAdapter.notifyDataSetChanged();
        }
    }

    private void deleteFolder(String folderName) {
        File folder = new File(getFilesDir(), folderName);
        if (folder.exists()) {
            if (folder.delete()) {
                folderList.remove(folderName);
                folderAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Folder deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    // Trong lớp FolderAdapter
    private class FolderAdapter extends ArrayAdapter<String> {

        private ArrayList<String> folders;

        public FolderAdapter(ID_Saving context, ArrayList<String> folders) {
            super(context, 0, folders);
            this.folders = folders;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.folder_list_item, parent, false);
            }

            TextView folderNameTextView = convertView.findViewById(R.id.folder_name);
            Button deleteButton = convertView.findViewById(R.id.delete_button);

            final String folderName = getItem(position);
            folderNameTextView.setText(folderName);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteFolder(folderName);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ID_Saving.this, ImagesActivity.class);

                    intent.putExtra("FOLDER_NAME", folderName);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

}
