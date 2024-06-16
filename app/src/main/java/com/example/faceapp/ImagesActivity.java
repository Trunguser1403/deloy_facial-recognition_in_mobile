package com.example.faceapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImagesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_IMAGE = 1;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 2;

    private GridView gridView;
    private ImageAdapter imageAdapter;
    private ArrayList<String> imagePaths;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        gridView = findViewById(R.id.grid_view);
        imagePaths = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            folderName = intent.getStringExtra("FOLDER_NAME");
            if (folderName != null) {
                loadImagesFromFolder(folderName);
            }
        }

        imageAdapter = new ImageAdapter(this, imagePaths);
        gridView.setAdapter(imageAdapter);

        Button addButton = findViewById(R.id.add_image);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ImagesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ImagesActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle image click if needed
            }
        });
    }

    private void loadImagesFromFolder(String folderName) {

        File folder = new File(getFilesDir(), folderName);

        Log.d("loadImagesFromFolder: ", folder.getAbsolutePath());

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isImageFile(file.getAbsolutePath())) {
                        imagePaths.add(file.getAbsolutePath());
                    }
                }
            }
            if (imageAdapter == null) {
                imageAdapter = new ImageAdapter(this, imagePaths);
                gridView.setAdapter(imageAdapter);
            } else {
                imageAdapter.notifyDataSetChanged();
            }


            imageAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Folder does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isImageFile(String filePath) {
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif"};
        for (String extension : imageExtensions) {
            if (filePath.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_ADD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                try {
                    String imagePath = getRealPathFromURI(selectedImage);
                    saveImageToFolder(imagePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to add image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    private void saveImageToFolder(String imagePath) {
        try {
            File folder = new File(getFilesDir(), folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }


            File sourceFile = new File(imagePath);
            File destinationFile = new File(folder, sourceFile.getName());
            try (InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(sourceFile));
                 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                imagePaths.add(destinationFile.getAbsolutePath());
                imageAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
