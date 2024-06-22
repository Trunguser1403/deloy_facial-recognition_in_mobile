package com.example.faceapp.database_manager;

public interface EmbeddingCallback {
    void onEmbeddingExtracted(float[] embedding, String name);
    void onExtractionFailed(Exception e);
}
