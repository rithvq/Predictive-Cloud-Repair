package org.predictive;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base directory for simulation outputs (CSVs). Override with {@code -Dsim.output.dir=...}.
 */
public final class SimulationPaths {

    private static final Path BASE =
            Paths.get(System.getProperty("sim.output.dir", System.getProperty("user.dir")));

    private SimulationPaths() {}

    public static Path baseDir() {
        return BASE;
    }
}
