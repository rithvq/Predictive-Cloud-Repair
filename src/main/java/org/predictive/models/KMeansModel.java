package org.predictive.models;


import java.util.Arrays;


public class KMeansModel {

    public int predict(double[] input) {

        double avg = Arrays.stream(input).average().orElse(0);

        if (avg < 30) return 0;

        if (avg < 70) return 1;

        return 2;

    }

}