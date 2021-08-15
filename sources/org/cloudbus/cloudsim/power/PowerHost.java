/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PowerHost class enables simulation of power-aware hosts.
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
 * @since CloudSim Toolkit 2.0
 */

public class PowerHost extends HostDynamicWorkload {

	/** The power model. */
	private PowerModel powerModel;


	/*
	 *//** The dimmer value*//*
	private double dimmerValue;
	
	*//**The lost revenue*//*
	private double revenueLoss = 0.0f;*/

	/** Store the disabledTagsSet */
	private Set<String> disabledTagsSet = new HashSet<String>();


	//@ Shashi
	private double zoneId;
	public  double temperature = 0;
	private ArrayList<String> tempHostHistory = new ArrayList<String>();
	public double hostDynamicTemperature = 0;
	public double  spatialEffectTemperature = 0;
	public double colddAirTemperature = 0;
	public double inletTemperature = 0;
	public	double idleServerTemperature = ThermalConstants.IDLE_SERVER_TEMPERATURE;

	public double currentScheduleTime = 0;

	double graspGreedyValue = 0;
	/**
	 * Instantiates a new host.
	 *
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the VM scheduler
	 */
	public PowerHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel,
			double zoneId	)
	{
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setPowerModel(powerModel);
		setDataCenterZone(zoneId);
	}
	public PowerHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel
	)
	{
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setPowerModel(powerModel);
		;
	}


	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 *
	 * @return the power
	 */
	public double getPower() {
		return getPower(getUtilizationOfCpu());
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 *
	 * @param utilization the utilization
	 * @return the power
	 */
	protected double getPower(double utilization) {
		double power = 0;
		try {
			power = getPowerModel().getPower(utilization);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the max power that can be consumed by the host.
	 *
	 * @return the max power
	 */
	public double getMaxPower() {
		double power = 0;
		try {
			power = getPowerModel().getPower(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the energy consumption using linear interpolation of the utilization change.
	 *
	 * @param fromUtilization the from utilization
	 * @param toUtilization the to utilization
	 * @param time the time
	 * @return the energy
	 */
	public double getEnergyLinearInterpolation(double fromUtilization, double toUtilization, double time) {
		if (fromUtilization == 0) {
			return 0;
		}
		double fromPower = getPower(fromUtilization);
		double toPower = getPower(toUtilization);
		return (fromPower + (toPower - fromPower) / 2) * time;
	}

	/**
	 * Sets the power model.
	 *
	 * @param powerModel the new power model
	 */
	protected void setPowerModel(PowerModel powerModel) {
		this.powerModel = powerModel;
	}

	/**
	 * Gets the power model.
	 *
	 * @return the power model
	 */
	public PowerModel getPowerModel() {
		return powerModel;
	}
	
/*	public void setDimmerValue(double dimmerValue){
		this.dimmerValue = dimmerValue;
	}

	public double getDimmerValue(){
		return dimmerValue;
	}
	
	public double getRevenueLoss(){
		return revenueLoss;
	}
	
	public void setRevenueLoss(double revenueLoss){
		this.revenueLoss = revenueLoss;
	}*/

	/**
	 * Store the disabled tags
	 * @return
	 */
	public Set<String> getDisabaledTagsSet(){
		return disabledTagsSet;
	}


	//@Shashi  - overridden from Host  // add optimization functionality here, we can create a class,
	// which can be called in PowerVmAllocationPolicyThermalGRASP.optimzeallocation also
	/*@Override
	public boolean isSuitableForVm(Vm vm) {
		return (getVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
				&& getVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForVm(vm, vm.getCurrentRequestedRam()) && getBwProvisioner()
				.isSuitableForVm(vm, vm.getCurrentRequestedBw()));
	}*/



	//@Shashi - sets the zone of the host
	public void setDataCenterZone(double zoneId)
	{
		this.zoneId = zoneId;
//		Log.printLine("@PowerHost @ setDataCenterZone- HostId- " + getId() +  "ZoneID- " + getHostZone() );
	}

	public double getHostZone(){


		return zoneId;
	}

	public  void setHostTemperature(double temperature){
		this.temperature = temperature;
	}

	public  double  getHostTemperature(){
		return this.temperature;
	}



	//@Shashi - called in PDCUCPWSFEF
	public void addHostTemperatureHistory(String history){
		this.tempHostHistory.add(history);

	}

	//@Shashi - Called in helper print
	public ArrayList<String> getHostTemperatureHistory(){
		return  this.tempHostHistory;
	}

	//@Shashi @ VMAPThermal
	public  double getDynamicTemperatureAtHost(){

		return temperature;
	}

	public double getDynamicTemperatureAtHost(double hostPower){


		//T(cpu) = PR + Tin + (T0 -PR -Tin)*e^(-t/RC);
		double PR = hostPower * ThermalConstants.THERMAL_RESISTANCE;
		double inletTemperatureInKelvin = inletTemperature + 273.5;

		double initialSetting = (ThermalConstants.INITIAL_CPU_TEMPERATURE - PR - inletTemperatureInKelvin );
		double exponentFraction = 0.5;//Math.pow(2.718281, (-1)/(ThermalConstants.THERMAL_RESISTANCE* ThermalConstants.HEAT_CAPACITY));

		double delta = initialSetting * exponentFraction;

		double dynamicTemperature =  PR +  inletTemperatureInKelvin + delta;

		return  (dynamicTemperature -273.5);  // Converting in celcius

	}



	/**
	 * @Author-Shashi
	 */
	protected double getHostPower() {

		double hostPower = getPowerModel().getPower(getCurrentUtilization());

		return hostPower;

	}
	/**
	 * @Author-Shashi
	 */
	protected double getCurrentUtilization() {
		double totalRequestedMips = 0;
		for (Vm vm : getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
		}
		double utilization = totalRequestedMips / getTotalMips();  // This method instead getutilize() is used,
		// a vm is temporarily created at host in after allocation method

		return utilization;

	}

	/**
	 * @Author-Shashi
	 */
	protected double getCurrentTemperature() {

//		return getDynamicTemperatureAtHost(getHostPower());
		return  getPredictedTemperature();
	}




	/**
	 * @Author-Shashi
	 */
	// HTTP call to a python flask application where inference model is hosted
	protected double getPredictedTemperature() {
		return 70;
//		PredictTemperatureHttpCall predictTemperature = new PredictTemperatureHttpCall();
//
////        double hostId, double cpuload, double power, double network_rx, double network_tx
//		double hostId = getId();
////		(CloudSim.clock() > 10000
//		double cpuLoad = getCurrentUtilization();
//		double ramUsed = getUtilizationOfRam();
//		double networkUsage = getUtilizationOfBw();
//		double cpuPower = getHostPower();
//		double numberofVms =0;
//
////		TODO ----------
////		double numberofCPUCoresUsed = getNumberOfPes() - getNumberOfFreePes();
//
//		double numberofCPUCoresUsed = getNumberOfPes();
//		double cores = getNumberOfPes();
//
//		double coresfree = getNumberOfFreePes();
//		double coressize = getPeList().size();
//		double busyPes = getNumberOfBusyPes();
//		double vmPesScheduler = getVmScheduler().getPeList().size();
//		for (Vm vm: getVmList()){
//			if(vm!=null){
//				double vmPes = vm.getNumberOfPes();
//				numberofVms ++;
//			}
//		}
//		double clock = CloudSim.clock();
//
//		double temperature = predictTemperature.getPredictedTemperature(hostId, cpuLoad, ramUsed, networkUsage, cpuPower, numberofVms, numberofCPUCoresUsed);
//		Log.printLine( PowerVmAllocationPolicyThermalMinimizePeakTemperature.class.getName() + " @getPredictedTemperature" +
//				"Predicted Temperature: " + temperature + " Host: "+ hostId + " Clock: " + CloudSim.clock() + " CPULoad: " + cpuLoad
//				+ " cpuPower: " +cpuPower  + " numberofVMforloop: " + numberofVms + " numberofVMslist: "+  getVmList());
//
//
//		return  temperature;

	}

}