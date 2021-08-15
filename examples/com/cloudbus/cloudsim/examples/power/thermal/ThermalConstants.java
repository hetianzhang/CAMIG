package com.cloudbus.cloudsim.examples.power.thermal;

/**
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
public class ThermalConstants {

	public final static int NUMBER_OF_VMS = 100;

	public final static int NUMBER_OF_HOSTS = 1200;

	public final static long CLOUDLET_UTILIZATION_SEED = 1;

	public final static long SPECIFIC_HEAT_OF_AIR = 1005;  //J/Kg/Kelvin

	//public final static double AIR_DENSITY = 1.19;  // Kg/m^3

	//public final static double  AIR_FLOW_RATE = 0.2454;  // m^3/sec

	//public  final static double THERMODYNAMIC_CONSTANT = SPECIFIC_HEAT_OF_AIR * AIR_DENSITY * AIR_FLOW_RATE;

	public final static long IDLE_SERVER_TEMPERATURE = 15; // 15 degree celcius

	public final static long CRAC_COLDAIR_SUPPLY_TEMPERATURE = 25;

	public final static long MAXIMUM_THRESHHOLD_TEMPERATURE = 105; //73 before

	public final static double HEAT_CAPACITY = 340; // Joules/Kelvin

	public  final  static  double THERMAL_RESISTANCE = 0.34; // Kelvin/Watt

	public final  static  double INITIAL_CPU_TEMPERATURE = 318; // Kelvin

	public  final  static  String SPATIAL_TEMPERATURE_MATRIX_FILE_PATH = "examples/com/cloudbus/cloudsim/examples/power/thermal/Data/SpatialTemperatureMatrix";

}
