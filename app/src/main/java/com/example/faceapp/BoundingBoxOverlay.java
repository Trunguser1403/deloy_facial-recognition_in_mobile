package com.example.faceapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxOverlay extends View {
    private List<Rect> boundingBoxes = new ArrayList<>();
    private Paint paint;

    private int cameraWidth = 640;
    private int cameraheight = 480;
    float scaleW;
    float scaleH;
    public BoundingBoxOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(0xFFFF0000); // Red color for bounding box
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);
    }

    public void setBoundingBoxes(List<Rect> boundingBoxes) {
        scaleW = (float) getWidth() / cameraheight;
        scaleH = (float)  getHeight() /  cameraWidth;
        this.boundingBoxes = boundingBoxes;
        invalidate(); // Request to redraw the view
    }


    private Rect scaleRect(Rect rect) {
        int left = Math.round(rect.left * this.scaleW);
        int top = Math.round(rect.top * this.scaleH);
        int right = Math.round(rect.right * this.scaleW);
        int bottom = Math.round(rect.bottom * this.scaleH);
        return new Rect(left, top, right, bottom);
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (Rect boundingBox : boundingBoxes) {
            canvas.drawRect(scaleRect(boundingBox), paint);
        }



    }
}