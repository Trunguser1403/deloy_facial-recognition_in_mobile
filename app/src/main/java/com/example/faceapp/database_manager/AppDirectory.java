package com.example.faceapp.database_manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AppDirectory {
    private Context context;
    private FaceDetector faceDetector;
    private Interpreter interpreter;



    public AppDirectory(Context context) {
        this.context = context;
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build();
        faceDetector = FaceDetection.getClient(options);
        loadModel();
    }

    private void loadModel() {
        try {
            InputStream inputStream = context.getAssets().open("mobilefacenet_model.tflite");
            ByteBuffer buffer = ByteBuffer.allocateDirect(inputStream.available());
            buffer.order(ByteOrder.nativeOrder());

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            buffer.put(bytes);
            inputStream.close();
            interpreter = new Interpreter(buffer);
            Log.d("load model", "loadModel: ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void extractEmbeddingsFromImages(String folderName, EmbeddingCallback callback) {
        File folder = new File(context.getFilesDir(), folderName);

        if (folder.exists() && folder.isDirectory()) {
            File[] imageFiles = folder.listFiles();
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    if (imageFile.isFile() && isImageFile(imageFile.getAbsolutePath())) {
                        Bitmap image_bitmap = loadImageFromFile(imageFile);
                        if (image_bitmap != null) {
                            extractFaceEmbedding(image_bitmap, callback, folderName);
                        }

                    }
                }
            }
        }
    }

    private void extractFaceEmbedding(Bitmap bitmap, EmbeddingCallback callback, String name) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    float[] embedding = null;
                    for (Face face : faces) {
                        Rect boundingBox = face.getBoundingBox();
                        Bitmap faceBitmap = cropToBoundingBox(bitmap, boundingBox);
                        embedding = getFaceEmbedding(faceBitmap);
                        if (embedding != null) {
                            callback.onEmbeddingExtracted(embedding, name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AppDirectory", "Failed to detect faces", e);
                    callback.onExtractionFailed(e);
                });
    }


    private Bitmap loadImageFromFile(File imageFile) {
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap cropToBoundingBox(Bitmap mediaImage, Rect boundingBox) {
        int width = boundingBox.width();
        int height = boundingBox.height();

        // Create a new Bitmap with the desired width and height
        Bitmap croppedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Calculate the x and y offsets for cropping
        int offsetX = boundingBox.left;
        int offsetY = boundingBox.top;

        // Create a canvas with the new Bitmap
        Canvas canvas = new Canvas(croppedBitmap);

        // Draw the cropped region of the original Bitmap onto the new Bitmap
        canvas.drawBitmap(mediaImage, -offsetX, -offsetY, null);

        return croppedBitmap;
    }

    private float[] getFaceEmbedding(Bitmap faceBitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(faceBitmap, 112, 112, false);

        // Create a ByteBuffer to hold the input data
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 112 * 112 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[112 * 112];
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        // Convert the pixel values to float and normalize them
        int pixel = 0;
        for (int i = 0; i < 112; ++i) {
            for (int j = 0; j < 112; ++j) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);  // R
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);   // G
                byteBuffer.putFloat((val & 0xFF) / 255.0f);          // B
            }
        }

        float[][] faceEmbeddings = new float[1][128];
        interpreter.run(byteBuffer, faceEmbeddings);

        return faceEmbeddings[0];
    }

    private boolean isImageFile(String filePath) {
        String[] extensions = {".jpg", ".jpeg", ".png"};
        for (String extension : extensions) {
            if (filePath.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
