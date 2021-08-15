package com.cloudbus.cloudsim.examples.power.brownout;

/**
 * The DimmerConstants class is difining some constants of dimmer to configure host utilization
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class DimmerConstants {

	public final static double DimmerValue = 1.0;

	public final static double DimmerUpThreshold = 0.78;
	
	public final static double DimmerLowThreshold = 0.3;
	
	public final static double DimmerComponentLowerThreshold = 0.5;


}
