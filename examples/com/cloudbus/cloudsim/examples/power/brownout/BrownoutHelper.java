/*
 *
 */
package com.cloudbus.cloudsim.examples.power.brownout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletOptionalComponent;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelDimmer;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.examples.power.Constants;

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
public class BrownoutHelper {

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
	public static List<Cloudlet> createCloudletList(int brokerId, int cloudletsNumber) {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long fileSize = 300;
		long outputSize = 300;
		long seed = BrownoutConstants.CLOUDLET_UTILIZATION_SEED;
		UtilizationModel utilizationModelNull = new UtilizationModelNull();
		List<CloudletOptionalComponent> cloudletOptionalComponentList = new ArrayList<CloudletOptionalComponent>();

		CloudletOptionalComponent coc = new CloudletOptionalComponent(0.2, 0.05, true, "tag0");
		cloudletOptionalComponentList.add(coc);

		CloudletOptionalComponent coc1 = new CloudletOptionalComponent(0.2, 0.04, true, "tag1");
		cloudletOptionalComponentList.add(coc1);

		CloudletOptionalComponent coc2 = new CloudletOptionalComponent(0.21, 0.04, true, "tag2");
		cloudletOptionalComponentList.add(coc2);

		CloudletOptionalComponent coc3 = new CloudletOptionalComponent(0.02, 0.01, true, "tage0");
//		cloudletOptionalComponentList.add(coc3); //Delay the add operation to the later codes
		
//		CloudletOptionalComponent coc4 = new CloudletOptionalComponent(0.15, 0.08, true, "tag3");
//		cloudletOptionalComponentList.add(coc4);

		for (int i = 0; i < cloudletsNumber; i++) {
			Cloudlet cloudlet = null;
			if (seed == -1) {
				cloudlet = new Cloudlet(i, Constants.CLOUDLET_LENGTH, Constants.CLOUDLET_PES, fileSize, outputSize,
						new UtilizationModelDimmer(), utilizationModelNull, utilizationModelNull,
						cloudletOptionalComponentList, true);
			} else {
				//Delay to this segment, whether to add another component
				if (isAddMoreComponent(i)) {
					cloudletOptionalComponentList.add(coc3);
				}

				cloudlet = new Cloudlet(i, Constants.CLOUDLET_LENGTH, Constants.CLOUDLET_PES, fileSize, outputSize,
						new UtilizationModelDimmer(seed * i), utilizationModelNull, utilizationModelNull,
						cloudletOptionalComponentList, true);
			}
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	private static boolean isAddMoreComponent(int seed){
		Random random = new Random(seed);
		double randomProbability = random.nextDouble();
		if(randomProbability > 0.8){
			return true;
		}else{
		return false;
		}

	}

}
