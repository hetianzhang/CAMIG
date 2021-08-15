package com.cloudbus.cloudsim.examples.power.thermal;
import java.nio.file.Paths;


import java.io.IOException;

public class Thermal {

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
		String vmAllocationPolicy = "thermal";//;"thermalCoolest";//  "thermalRandom";//;//"thermal";
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
