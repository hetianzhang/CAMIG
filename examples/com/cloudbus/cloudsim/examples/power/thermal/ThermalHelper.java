/*
 *
 */
package com.cloudbus.cloudsim.examples.power.thermal;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.examples.power.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Helper class for the random workload.
 * 
 * If you are using any algorithms, policies or workload included in the power
 * package please cite the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic
 * Algorithms and Adaptive Heuristics for Energy and Performance Efficient
 * Dynamic Consolidation of Virtual Machines in Cloud Data Centers", Concurrency
 * and Computation: Practice and Experience (CCPE), Volume 24, Issue 13, Pages:
 * 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class ThermalHelper {

	/**
	 * Creates the cloudlet list.
	 * 
	 * @param brokerId
	 *            the broker id
	 * @param cloudletsNumber
	 *            the cloudlets number
	 * 
	 * @return the list< cloudlet>
	 */
	//This method has not been used. Planterlab Helper class method  is used
	public static List<Cloudlet> createCloudletList(int brokerId, int cloudletsNumber) throws IOException {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long fileSize = 300;
		long outputSize = 300;
		long seed = ThermalConstants.CLOUDLET_UTILIZATION_SEED;
		UtilizationModel utilizationModelNull = new UtilizationModelNull();

//		cloudletOptionalComponentList.add(coc4);
		//@SuppressWarnings("Duplicates");*/
		String path = "cloudsim-3.0.3/examples/workload/planetlab/20110303/75-130-96-12_static_oxfr_ma_charter_com_irisaple_wup";
		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = null;
			//if (seed == -1) {
				cloudlet = new Cloudlet(i, Constants.CLOUDLET_LENGTH, Constants.CLOUDLET_PES, fileSize, outputSize,
						new UtilizationModelPlanetLabInMemory(path, Constants.SCHEDULING_INTERVAL), utilizationModelNull, utilizationModelNull
						, true);

			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	public static double[][] getSpatialTemperatureMatrix() throws IOException{

		System.out.println("#########################################################################");
		return  new  SpatialTemperatureMatrix().getSpatialTemperatureMatrix();

	}


}
