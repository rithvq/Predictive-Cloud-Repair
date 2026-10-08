package org.predictive.agent;


import org.predictive.models.EnsemblePredictor;

import org.predictive.OutageHistoryLogger;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;


import java.util.List;


public class PredictiveRepairAgent {

    DatacenterBroker broker;

    public EnsemblePredictor predictor = new EnsemblePredictor();

    public PredictiveRepairAgent(DatacenterBroker broker) {

        this.broker = broker;

    }


    public void decide(List<double[]> metricsList, List<Vm> vms, int currentTime) {

        double[] combinedFeatures = new double[metricsList.size() * 3];

        int idx = 0;


        for (int i = 0; i < metricsList.size(); i++) {

            double[] metrics = metricsList.get(i);

            Vm vm = vms.get(i);


            double score = predictor.predict(metrics);

            String layer = OutageHistoryLogger.identifyComponent(metrics);

            double repairProb = score / 100.0; // simplistic probability mapping

            OutageHistoryLogger.log(currentTime, vm.getId(), layer, "PreviousOutage", repairProb);


            for (double v : metrics) combinedFeatures[idx++] = v;

            System.out.println("VM-" + vm.getId() + " Layer: " + layer + " RepairProb: " + repairProb);

        }


        double ensembleScore = predictor.predict(combinedFeatures);

        if (ensembleScore > 60) {

            broker.submitVm(new VmSimple(2000, 2));

            System.out.println("Agent: PREVENTIVE ACTION - Scale out or migrate resources");

        }

    }

}