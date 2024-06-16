package com.example.faceapp;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Camara_View extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private FaceDetector faceDetector;
    private ExecutorService cameraExecutor;
    private Interpreter interpreter;
    private BoundingBoxOverlay boundingBoxOverlay;
    private PreviewView previewView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara_view);

        previewView = findViewById(R.id.preview_view);
        boundingBoxOverlay = findViewById(R.id.boundingBoxOverlay);
        textView = findViewById(R.id.textView3);




        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .build();
        this.faceDetector = FaceDetection.getClient(options);


        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        loadModel();


    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including cancellation) here.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new Recognize_In_Image());

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }


    private void loadModel() {
        try {
            InputStream inputStream = getAssets().open("mobilefacenet_model.tflite");
            ByteBuffer buffer = ByteBuffer.allocateDirect(inputStream.available());
            buffer.order(ByteOrder.nativeOrder());

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            buffer.put(bytes);
            inputStream.close();

            interpreter = new Interpreter(buffer);
            textView.setText("load ok");
        } catch (IOException e) {
            textView.setText("load fail");
            e.printStackTrace();
        }
    }


    public class Recognize_In_Image implements ImageAnalysis.Analyzer {
        int imageWidth;
        int imageHeight;
        Image.Plane[] planes;

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError")
            Image mediaImage = imageProxy.getImage();

            if (mediaImage != null) {
                imageWidth = mediaImage.getWidth();
                imageHeight = mediaImage.getHeight();
                planes = mediaImage.getPlanes();

                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                FaceDetector detector = FaceDetection.getClient();
                detector.process(image)
                        .addOnSuccessListener(faces -> {
                            List<Rect> boundingBoxes = new ArrayList<>();
                            List<String> names = new ArrayList<>();
                            for (Face face : faces) {
                                Rect boundingBox = face.getBoundingBox();
                                Bitmap faceBitmap = cropToBoundingBox(mediaImage, boundingBox);
                                if (faceBitmap != null) {
                                    float[] faceEmbeddings = getFaceEmbeddings(faceBitmap);
                                    textView.setText("emb" + faceEmbeddings[0]);
//                                String recognizedPerson = recognizeFace(faceEmbeddings);
                                    boundingBoxes.add(boundingBox);

                                }
                            }
                            drawBoundingBox(boundingBoxes);
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            }
        }

        private Bitmap cropToBoundingBox(Image mediaImage, Rect boundingBox) {
            if (mediaImage == null) {
                return null;
            }

            if (boundingBox.left < 0 || boundingBox.top < 0 ||
                    boundingBox.right > imageWidth || boundingBox.bottom > imageHeight) {
                return null; // Handle out-of-bounds bounding box
            }
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            yBuffer.get(nv21, 0, ySize);
            uBuffer.get(nv21, ySize, uSize);
            vBuffer.get(nv21, ySize + uSize, vSize);

            // Convert NV21 byte array to Bitmap (YUV to RGB)
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageWidth, imageHeight, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, imageWidth, imageHeight), 100, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            return Bitmap.createBitmap(bitmap, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height());
        }
        private float[] getFaceEmbeddings(Bitmap faceBitmap) {
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

        private String recognizeFace(float[] faceEmbeddings) {
            // Compare embeddings with your database and return the recognized person's name
            // This is a placeholder for demonstration
            return "Person Name";
        }

        private void drawBoundingBox(List<Rect> boundingBoxes) {
            runOnUiThread(() -> {
                boundingBoxOverlay.setBoundingBoxes(boundingBoxes);
            });


        }
    }

}