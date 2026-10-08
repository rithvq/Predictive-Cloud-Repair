package org.predictive;


import org.cloudbus.cloudsim.vms.Vm;


public class MetricMonitor {

    public static double[] collect(Vm vm) {

        return new double[] {

                vm.getCpuPercentUtilization(),

                vm.getRam().getPercentUtilization(),

                vm.getBw().getPercentUtilization()

        };

    }

}