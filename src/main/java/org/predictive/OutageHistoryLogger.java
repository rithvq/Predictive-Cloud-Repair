package org.predictive;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OutageHistoryLogger {

    private static FileWriter writer;

    private static final Map<Long, String> lastComponentMap = new HashMap<>();

    static {
        try {
            writer = new FileWriter(SimulationPaths.baseDir().resolve("outage_history.csv").toFile());
            writer.write("Time,ComponentID,Layer,LastFailure,RepairProbability\n");
            writer.flush();
            Runtime.getRuntime().addShutdownHook(new Thread(OutageHistoryLogger::closeQuietly));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeQuietly() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static void log(int time, long compId, String layer, String lastFailure, double repairProb) {
        try {
            writer.write(time + "," + compId + "," + layer + "," + lastFailure + "," + repairProb + "\n");
            writer.flush();
            lastComponentMap.put(compId, lastFailure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String identifyComponent(double[] metrics) {

        String[] layers = {"Hypervisor", "VM", "Network", "Storage", "Container"};

        Random r = new Random();

        return layers[r.nextInt(layers.length)];

    }

}