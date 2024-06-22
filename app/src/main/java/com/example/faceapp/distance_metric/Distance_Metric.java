package com.example.faceapp.distance_metric;

public class Distance_Metric {
    public static float cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector dimensions must be the same");
        }

        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0f || normB == 0.0f) {
            return 0.0f; // To handle edge cases where a vector is all zeros
        }

        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
    public static float euclideanDistance(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector dimensions must be the same");
        }

        float sum = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            sum += Math.pow(vectorA[i] - vectorB[i], 2);
        }

        return (float) Math.sqrt(sum);
    }
    public static float manhattanDistance(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector dimensions must be the same");
        }

        float sum = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            sum += Math.abs(vectorA[i] - vectorB[i]);
        }

        return sum;
    }
    public static double mahalanobisDistance(double[] vectorA, double[] vectorB, double[][] covarianceMatrix) {
        if (vectorA.length != vectorB.length || vectorA.length != covarianceMatrix.length || covarianceMatrix.length != covarianceMatrix[0].length) {
            throw new IllegalArgumentException("Vector dimensions and covariance matrix dimensions must match");
        }

        double[] diff = new double[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            diff[i] = vectorA[i] - vectorB[i];
        }

        double[] invCovarianceMatrixTimesDiff = new double[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            for (int j = 0; j < vectorA.length; j++) {
                invCovarianceMatrixTimesDiff[i] += covarianceMatrix[i][j] * diff[j];
            }
        }

        double mahalanobisDistance = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            mahalanobisDistance += diff[i] * invCovarianceMatrixTimesDiff[i];
        }
        return Math.sqrt(mahalanobisDistance);
    }

}
