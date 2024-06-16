package com.example.faceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    ImageButton camera_btn, setting_btn, id_btn;

    private void get_palettes(){
        camera_btn = findViewById(R.id.camera_id);
        id_btn = findViewById(R.id.id_btn);
        setting_btn = findViewById(R.id.setting_btn);
    }

    private void click_camera_btn(){
        this.camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Camara_View.class);
                startActivity(intent);
            }
        });
    }

    private void click_camera_id_btn(){
        this.id_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ID_Saving.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        get_palettes();
        click_camera_btn();
        click_camera_id_btn();
    }
}