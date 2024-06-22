package com.example.faceapp.ObjectData;

import java.util.ArrayList;
import java.util.List;

public class PersonEmbeddings {
    private String name;
    private List<float[]> embeddings;
    public PersonEmbeddings(String name) {
        this.name = name;
        embeddings = new ArrayList<>();
    }

    public void addEmbeddings(float[] embedding){
        embeddings.add(embedding);
    }
    public String getName() {
        return name;
    }

    public List<float[]> getEmbeddings() {
        return embeddings;
    }
}
