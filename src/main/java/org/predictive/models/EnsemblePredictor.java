package org.predictive.models;


public class EnsemblePredictor {

    AutoencoderModel ae = new AutoencoderModel();

    KMeansModel km = new KMeansModel();

    AnomalyModel am = new AnomalyModel();


    public double predict(double[] features) {

        double score = 0;

        score += ae.score(features) * 0.4;

        score += km.predict(features) * 20 * 0.4;

        score += am.detect(features) ? 30 : 0;

        return score;

    }

}