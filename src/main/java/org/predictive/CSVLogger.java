package org.predictive;

import java.io.FileWriter;
import java.io.IOException;

public class CSVLogger {

    private static FileWriter writer;

    static {
        try {
            writer = new FileWriter(SimulationPaths.baseDir().resolve("metrics.csv").toFile());
            writer.write("Time,VM,CPU,RAM,BW,EnsembleScore\n");
            writer.flush();
            Runtime.getRuntime().addShutdownHook(new Thread(CSVLogger::closeQuietly));
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

    public static void log(int time, long vm, double cpu, double ram, double bw, double score) {
        try {
            writer.write(time + "," + vm + "," + cpu + "," + ram + "," + bw + "," + score + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}