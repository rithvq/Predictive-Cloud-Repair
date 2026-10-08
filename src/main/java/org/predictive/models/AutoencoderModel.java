package org.predictive.models;


import java.util.Arrays;


public class AutoencoderModel {

    public double score(double[] input) {

        double mean = Arrays.stream(input).average().orElse(0);

        double error = 0;

        for (double v : input) error += Math.abs(v - mean);

        return error / input.length;

    }

}