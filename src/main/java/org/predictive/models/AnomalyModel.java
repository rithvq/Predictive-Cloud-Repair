package org.predictive.models;


import java.util.Arrays;


public class AnomalyModel {

    public boolean detect(double[] input) {

        double mean = Arrays.stream(input).average().orElse(0);

        double var = 0;

        for (double v : input) var += Math.pow(v - mean, 2);

        return (var / input.length) > 500;

    }
}