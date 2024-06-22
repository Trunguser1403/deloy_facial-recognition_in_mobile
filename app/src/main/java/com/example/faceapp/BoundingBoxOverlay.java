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
    private List<String> names = new ArrayList<>();
    private Paint paint;
    private Paint textPaint; // Paint object for drawing text


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

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // White color for text
        textPaint.setTextSize(24); // Text size
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setBoundingBoxes(List<Rect> boundingBoxes, List<String> names) {
        scaleW = (float) getWidth() / cameraheight;
        scaleH = (float)  getHeight() /  cameraWidth;
        this.boundingBoxes = boundingBoxes;
        this.names = names;
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
        for (int i = 0; i < boundingBoxes.size(); i++) {
            Rect rect_bbx = scaleRect(boundingBoxes.get(i));
            canvas.drawRect(rect_bbx, paint);

            String name = names.get(i);
            float textWidth = textPaint.measureText(name);
            float textX = rect_bbx.left + (rect_bbx.width() - textWidth) / 2;
            float textY = rect_bbx.bottom + 24; // Adjust this offset as needed

            canvas.drawText(name, textX, textY, textPaint);
        }



    }
}