package org.predictive;


import java.util.*;


public class FeatureBuilder {

    public static double[] build(List<double[]> metrics) {

        List<Double> list = new ArrayList<>();

        for (double[] arr : metrics)

            for (double v : arr) list.add(v);

        return list.stream().mapToDouble(Double::doubleValue).toArray();

    }

}