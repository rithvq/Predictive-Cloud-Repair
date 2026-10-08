package org.predictive;

import org.predictive.agent.PredictiveRepairAgent;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainSimulation {

    public static void main(String[] args) {
        try {
            Class.forName("org.predictive.CSVLogger");
            Class.forName("org.predictive.OutageHistoryLogger");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        CloudSim sim = new CloudSim();

        // Host PEs must cover: (a) each VM's PE count, (b) MIPS per PE >= each VM's requested MIPS/PE.
        // VmSimple(2000, 2) needs 2 PEs at >= 2000 MIPS; a single PeSimple(1000) host cannot fit any 2-PE VM.
        List<Pe> peList = new ArrayList<>();
        final int hostPes = 8;
        final double mipsPerPe = 3000;
        for (int i = 0; i < hostPes; i++) {
            peList.add(new PeSimple(mipsPerPe));
        }
        Host host = new HostSimple(16000, 10000, 8000, peList);

        // Datacenter
        Datacenter dc = new DatacenterSimple(sim, Arrays.asList(host));

        // Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(sim);

        // VMs
        List<Vm> vmList = Arrays.asList(
                new VmSimple(1000, 2),
                new VmSimple(2000, 2)
        );
        broker.submitVmList(vmList);

        // Cloudlets drive the simulation; without them the clock may not advance and no metrics are logged.
        UtilizationModelDynamic cpuUse = new UtilizationModelDynamic(0.55);
        List<Cloudlet> cloudletList =
                Arrays.asList(
                        new CloudletSimple(80_000L, 2, cpuUse),
                        new CloudletSimple(60_000L, 2, cpuUse));
        broker.submitCloudletList(cloudletList);

        // Agent
        PredictiveRepairAgent agent = new PredictiveRepairAgent(broker);

        // Simulation Listener
        sim.addOnClockTickListener(event -> {

            List<double[]> metrics = new ArrayList<>();
            List<Vm> vms = broker.getVmCreatedList();

            for (Vm vm : vms) {
                double[] m = MetricMonitor.collect(vm);
                metrics.add(m);

                double score = agent.predictor.predict(m);

                CSVLogger.log((int) sim.clock(), vm.getId(), m[0], m[1], m[2], score);
            }

            agent.decide(metrics, vms, (int) sim.clock());
        });

        sim.start();
    }
}