package org.cloudbus.cloudsim.examples.power.planetlab;

/**
 * Generating concurrency-aware multiple migration requests during dynamic resource management for a better multiple migration scheduling performance.
 * @author TianZhang He
 */

import java.io.IOException;

public class CamigMmt {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        boolean enableOutput = true;
        boolean outputToFile = false;
        String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
        String outputFolder = "output";
        String workload = "20110303"; // PlanetLab workload
        String vmAllocationPolicy = "camig"; // Local Regression (LR) VM allocation policy
        String vmSelectionPolicy = "mmt"; // Minimum Migration Time (MMT) VM selection policy
        String parameter = "1.2"; // the safety parameter of the LR policy  // 1.0- 1.4

        //set singleVMViewSelection in PowerVmAllocationPolicyMigrationAbstract for SingleVMView and hostHitPolicy for HostHits baseline algorithm

        new PlanetLabRunner(
                enableOutput,
                outputToFile,
                inputFolder,
                outputFolder,
                workload,
                vmAllocationPolicy,
                vmSelectionPolicy,
                parameter);
    }
}
