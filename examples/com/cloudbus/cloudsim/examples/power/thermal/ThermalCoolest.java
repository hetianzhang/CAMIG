package com.cloudbus.cloudsim.examples.power.thermal;

import java.io.IOException;

public class ThermalCoolest {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        boolean enableOutput = true;
        boolean outputToFile = true;
        String inputFolder =  Thermal.class.getClassLoader().getResource("workload/planetlab").getPath();//"";
        String outputFolder = "output";
        String workload = "20110303"; // "temp";//  "temp";//"random"; // Random workload //
        String vmAllocationPolicy ="thermalCoolest";// "thermal";// "thermalRandom";//;//"thermal";
        String vmSelectionPolicy = "mmt";
        String parameter = "0.8";

        new ThermalRunner(
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
