package com.cloudbus.cloudsim.examples.power.thermal;

import java.io.IOException;

public class ThermalLPSolverMinimizePeak {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
//    public static void main(String[] args) throws IOException {
//        boolean enableOutput = true;
//        boolean outputToFile = true;
////        String inputFolder =  Thermal.class.getClassLoader().getResource("workload/planetlab").getPath();//"";
//        String inputFolder =  ThermalMinimizePeakTemperature.class.getClassLoader().getResource("workload/bitbrain").getPath();//"";
//        String outputFolder = "output";
//
//        String workload = "fastStorage"; // "temp";//  "temp";//"random"; // Random workload //
//        String vmAllocationPolicy = "thermalLPSolverMinPeak";//;"thermalCoolest";//  "thermalRandom";//;//"thermal";
//        String vmSelectionPolicy = "mmt";//mu";
//        String parameter = "200";
//        System.out.println();
//        new BitBrainRunner(
//                enableOutput,
//                outputToFile,
//                inputFolder,
//                outputFolder,
//                workload,
//                vmAllocationPolicy,
//                vmSelectionPolicy,
//                parameter);
//    }


    public static void main(String[] args) throws IOException {
        boolean enableOutput = true;
        boolean outputToFile = true;
        String inputFolder =  Thermal.class.getClassLoader().getResource("workload/planetlab").getPath();//"";
        String outputFolder = "output";
        String workload = "20110303"; // "temp";//  "temp";//"random"; // Random workload //
        String vmAllocationPolicy = "thermalLPSolverMinPeak";//;"thermalCoolest";//  "thermalRandom";//;//"thermal";
        String vmSelectionPolicy = "mmt";//mu";
        String parameter = "200";
        System.out.println();
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
