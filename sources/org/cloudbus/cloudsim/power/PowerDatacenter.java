/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.DatacenterConstants;
import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import com.cloudbus.cloudsim.examples.power.thermal.ThermalHelper;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Shashi-d


/**
 * PowerDatacenter is a class that enables simulation of power-aware data
 * centers.
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
 * @since CloudSim Toolkit 2.0
 */
public class PowerDatacenter extends Datacenter {

	/** The power. */
	private double power;

	/** Cooling Power */
	private double coolingPower;

	/** The disable migrations. */
	private boolean disableMigrations;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The migration count. */
	private int migrationCount;
	private List<String> migrationHistory = new ArrayList<>();
	private List<String> vmCandidateHistory = new ArrayList<>();

	private int numberofTemperatureThreshHoldViolation;

	/** The number of times that the dimmer is triggered*/
//	private int dimmerTimes = 0; // @extra

	/** A set that records how many times dimmer maybe triggered*/
//	private Set<Double> timeFrameMayTriggeredDimmer = new HashSet<Double>(); // @extra


	private ArrayList<String> tempHistory = new ArrayList<String>();
	//@Shashi
	//private double temperature;

	private double[][] spatialTemperatureMatrix;

	private  double coldAirSupplyTemperature;
	private  double count =0;
	private  double temperatureThreshold = 0;
	/** A lined hashmap records how many active hosts at each time interval*/
	private LinkedHashMap<Double, Integer> numberOfActiveHostMap = new LinkedHashMap<Double, Integer>();

	/** A List of all VMs placement*/
	private List<String> vmPlacementHistory = new ArrayList<>();


	/** A lined hashmap records how many hosts have hotspotsl*/
	private LinkedHashMap<Double, Integer> numberOfActiveHotspotsMap = new LinkedHashMap<Double, Integer>();
	private LinkedHashMap<Double, Double> peakTemperatureMap = new LinkedHashMap<Double, Double>();

	private LinkedHashMap<Double, Double> averageTemperatureMap = new LinkedHashMap<Double, Double>();

	private LinkedHashMap<Double,  String> activeHistory = new LinkedHashMap<Double, String>();


	/**
	 * Instantiates a new datacenter.
	 *
	 * @param name
	 *            the name
	 * @param characteristics
	 *            the res config
	 * @param schedulingInterval
	 *            the scheduling interval
	 * //@param utilizationBound
	 *            the utilization bound
	 * @param vmAllocationPolicy
	 *            the vm provisioner
	 * @param storageList
	 *            the storage list
	 * @throws Exception
	 *             the exception
	 */
	public PowerDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
					throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPower(0.0);

		setCoolingrPower(0.0);

		setDisableMigrations(false); //@Shashi // it was false
		setCloudletSubmitted(-1);
		setMigrationCount(0);
		this.spatialTemperatureMatrix = new double[50][50];
		setSpatialTemperatureMatrix();
		setCRACColdAirSupplyTemperature();
		setTemperatureThreshold();

		setNumberofTemperatureThreshHoldViolation(0);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It
	 * is necessary because Hosts and VirtualMachines are simple objects, not
	 * entities. So, they don't receive events and updating cloudlets inside
	 * them must be called from the outside.
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessing() {

		//@Shashi


		Log.printDebugMessages(" Hostlist size " + getHostList().size());
		// when no cloudletsubmitted yet, then we start scheduling
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
		Log.printLine("currentTime:-> "+ currentTime +  " Count:  "+ count);

		// if some time passed  since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.print(currentTime + " ");

			// in this called method, energy related calculation is done
			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			// If migration exist, it calls for optimize allocation method of corresponding Vmallocationpolicy
			if (!isDisableMigrations()) {
				if(getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract){
					//global view potential destinations
					//((PowerVmAllocationPolicyMigrationAbstract) getVmAllocationPolicy()).optimizeVmAllocationCandidates(this.getVmList());
				}
				//getting vm from over-utilized and under-utilized hosts
				//update src host for all active VMs
				/*for(Host h: this.getHostList()) {
					for(Vm vm:h.getVmList()) {
						//vm.setHost(h);
						int index = this.getVmList().indexOf(vm);
						this.getVmList().get(index).setHost(h);
					}
				}*/

				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());
				if(getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
					List<Map<Integer, List<? extends Host>>> dstCandiateMatrix = ((PowerVmAllocationPolicyMigrationAbstract) getVmAllocationPolicy()).getAllocationMatrix();
					for (Map<Integer, List<? extends Host>> candidateList : dstCandiateMatrix) {
						for (Map.Entry<Integer, List<? extends Host>> en : candidateList.entrySet()) {
							Integer vmId = en.getKey();
							List<? extends Host> hostlist = en.getValue();
							String s = CloudSim.clock() + "," + String.valueOf(vmId);
							for (Host h : hostlist) {
								s += "," + String.valueOf(h.getId());
							}
							this.vmCandidateHistory.add(s + '\n');
						}
					}
				}

				if (migrationMap != null) {

					for (Map<String, Object> migrate : migrationMap) {


						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if(oldHost ==null) {
							String miginfo = String.valueOf(vm.getId()) +',' + String.valueOf(CloudSim.clock()) + ',' + " " + "," + String.valueOf(targetHost.getId()+","+vm.getRam());
							this.migrationHistory.add(miginfo);
						}else{
							String miginfo = String.valueOf(vm.getId()) +',' + String.valueOf(CloudSim.clock()) + ',' + String.valueOf(oldHost.getId()) + "," + String.valueOf(targetHost.getId()+","+
									vm.getRam());
							this.getNumberOfActiveHostsMap();
							this.migrationHistory.add(miginfo);
						}

						if (oldHost == null) {
							Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", currentTime, vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime, vm.getId(), oldHost.getId(), targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration
						// purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(getId(), vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)), CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);


			//@Debug Shashi
//			File test = new File("testpowerDc");
//			test.mkdir();



		}
	}

	/**
	 * Update cloudlet processing without scheduling future events.
	 *
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime())
		{
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 *
	 * @return the double
	 */

	//called in previous method and also in updatecloudletprocessing(which is called in everytimeframe)
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		count ++;
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0; // energy consumed in this timeframe

		double timeframeDatacenterCoolingPower = 0.0;
		int numberOfActiveHostPerTimeframe = 0; //@extra
		int numberOfHotSpotsPerTimeFrame = 0;
		double maxPeakTemperature = -1;
		double hottestNode = -1;

		double totalTemperature = 0;
		double totalActivehosts = 0;
//
//		Log.printLine("\n\n--------------------------------------------------------------\n\n");
//		Log.formatLine("@PowerDC @UCPWSFEV New resource usage for the time frame starting at %.2f:", currentTime);
//		double dimmerValue = getDimmerValue(); @extra

		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			// Dimmer is triggered
//			triggerDimmer(host, currentTime, dimmerValue); // @extra

			double time = host.updateVmsProcessing(currentTime); // inform VMs
																	// to update
																	// processing
			if (time < minTime) {
				minTime = time;
			}
		//	Log.formatLine("@PowerDC @UCPWSFEV %.2f: [Host #%d] utilization is %.2f%%", currentTime, host.getId(),
			//		host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
		//	Log.formatLine("\n @PowerDC @UCPWSFEV Energy consumption for the last time frame from %.2f to %.2f:", getLastProcessTime(),
		//			currentTime);


		// All energy related calculation is done here - > Add temperature related calculation here
			for (PowerHost host : this.<PowerHost> getHostList()) {

				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(previousUtilizationOfCpu,
						utilizationOfCpu, timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;


				Log.printLine("@" + PowerDatacenter.class.getName() +   " clock: " + CloudSim.clock()+ " host Id- > " + host.getId() + " putil -> " + previousUtilizationOfCpu + " cutil-> " + utilizationOfCpu
						+" timeDiff-> " + timeDiff + " TimeframeframeHEnergy-> " + timeFrameHostEnergy);

				// @Shashi
//				double timeFrameHostTemperature = getTemperature(timeFrameHostEnergy, host, timeDiff);
				//new predictive method to get the temperature
				double timeFrameHostTemperature = host.getPredictedTemperature();

				String delimeter = ",";
				String temperatureHistory = host.getId()+ delimeter +currentTime + delimeter + utilizationOfCpu + delimeter + timeFrameHostTemperature;
				// to calculate highest peak temperature
				if(timeFrameHostTemperature > maxPeakTemperature){
					maxPeakTemperature = timeFrameHostTemperature;
					hottestNode = host.getId();

				}

				// to calculate the average temperature
				if (host.getUtilizationOfCpu() > 0){
					totalTemperature += timeFrameHostTemperature;
					totalActivehosts ++;
				}

				tempHistory.add(temperatureHistory);

				host.setHostTemperature(timeFrameHostTemperature);
				host.addHostTemperatureHistory(temperatureHistory);


				if (timeFrameHostTemperature > getTemperatureThreshhold()){
					//System.out.println("$$$$$$$$$$$$$$$ Hostid: " + host.getId() + " Temperature: " + timeFrameHostTemperature )  ;
					numberofTemperatureThreshHoldViolation++;

					numberOfHotSpotsPerTimeFrame++;
				}

			//	Log.printLine();
			//	Log.formatLine("@PowerDC @UCPWSFEV %.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
			//			host.getId(), getLastProcessTime(), previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
			//	Log.formatLine("@PowerDC @UCPWSFEV %.2f: [Host #%d] energy is %.2f W*sec", currentTime, host.getId(), timeFrameHostEnergy);

				if(host.getUtilizationOfCpu() != 0){ //Compute the total active number of host at each time interval  // @extra
					numberOfActiveHostPerTimeframe++;// @extra
				}// @extra*/

			}

			Log.formatLine("\n@PowerDC @UCPWSFEV %.2f: Data center's energy is %.2f W*sec \n  ", currentTime, timeFrameDatacenterEnergy);

		}

		if(0 == (currentTime - 0.1) % 300){  // @extra
			numberOfActiveHostMap.put(currentTime - 0.1, numberOfActiveHostPerTimeframe); //Record the active number of host into a map // @extra
			numberOfActiveHotspotsMap.put(currentTime -0.1, numberOfHotSpotsPerTimeFrame );
			activeHistory.put(currentTime - 0.1, String.format("%d,%d", numberOfActiveHostPerTimeframe, numberOfHotSpotsPerTimeFrame));
			peakTemperatureMap.put(currentTime, maxPeakTemperature);
			averageTemperatureMap.put(currentTime, totalTemperature/totalActivehosts);
			for(Host h: getVmAllocationPolicy().getHostList()) {
				for(Vm vm: h.getVmList()) {
					this.vmPlacementHistory.add(String.valueOf(currentTime - 0.1) + ',' + vm.getId() + ',' + h.getId());
				}
			}
		}


		setPower(getPower() + timeFrameDatacenterEnergy);
		//@Shashi
		timeframeDatacenterCoolingPower = getTimeFrameCoolingPower(timeFrameDatacenterEnergy);

		setCoolingrPower(getCoolingPower() + timeframeDatacenterCoolingPower);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
		//		Log.printLine("@PowerDC @UCPWSFEV  VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.
	 * core.SimEvent, boolean)
	 */
	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent,
	 * boolean)
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 *
	 * @return the power
	 */
	// @ Shashi - This method was called in helper print result method to get energy
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 *
	 * @param power
	 *            the new power
	 */
	// this method is called in - > updateCloudetProcessingWithoutSchedulingFutureEventsForce which is called in every timeframe of scheduling interval
	protected void setPower(double power) {
		this.power = power;
		Log.printLine("@PDC@SetPower  Power- > " +  this.power);

	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 *
	 * @return true, if PowerDatacenter is in migration
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if is disable migrations.
	 *
	 * @return true, if is disable migrations
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Sets the disable migrations.
	 *
	 * @param disableMigrations
	 *            the new disable migrations
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 *
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submited.
	 *
	 * @param cloudletSubmitted
	 *            the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 *
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	public List<String> getMigrationHistory() {
		return this.migrationHistory;
	}

	public List<String> getVmCandidateHistory()	{
		return this.vmCandidateHistory;
	}

	/**
	 * Sets the migration count.
	 *
	 * @param migrationCount
	 *            the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}


	/**@author Shashi*/

	/** @author Shashi
	 *  Includes computing and cooling power*/
	public void setCoolingrPower(double coolingPower)
	{
			this.coolingPower = coolingPower;

	}


	public  double  getCoolingPower()
	{
		return coolingPower;
	}

	/** @author Shashi
	 *  Includes computing and cooling power*/

	public  double  getTotalDataCenterPower()
	{
		return power + coolingPower;
	}


	 protected   double getTimeFrameCoolingPower(double timeFrameDatacenterEnergy)
	{
		double CRACcoolingEnergy;
		// CoP = 0.0068*T^2 + 0.0008* T + 0.458
		double CoP = (0.0068* ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE *
				ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE
				+ 0.0008*ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE + 0.458);

		CRACcoolingEnergy = timeFrameDatacenterEnergy / CoP;

		return  CRACcoolingEnergy;
	 }

	/*
	* @author Shashi
	* */

//
//	/**
//	 * Gets the current host temperature
//	 * @return
//	 * @author Shashi
//	 */
//	// TODO- move it to host, this is per host operation
//	public double getTemperature(double timeFrameHostEnergy, PowerHost host, double timeDiff){
//
//
//
//		double  spatialEffectTemperature = getTemperatureDueToSpatialEffect(timeFrameHostEnergy, host, timeDiff);
//
//		double colddAirTemperature = getCRACColdAirSupplyTemperature();
//
//		double inletTemperature = spatialEffectTemperature +   colddAirTemperature;
//
//		//@NewtemperatureModel
//		//double hostDynamicTemperature = getDynamicHostTemperature(host, timeFrameHostEnergy/timeDiff, inletTemperature);
//		double hostDynamicTemperature = host.getPredictedTemperature();
//
//		/*Log.printLine("@PDC@GTCHECK HostId- > " + host.getId() + " hostPower-> " + timeFrameHostEnergy/timeDiff + " inlettemp-> "+ inletTemperature
//		+ " DynamicTemp-> " + hostDynamicTemperature);*/
//		// Todo setmethods
//		setHostTemperatureData(host,  spatialEffectTemperature, hostDynamicTemperature, colddAirTemperature, inletTemperature);
//
//
////	 Log.printDebugMessages(String.format("@PDC @GT hostid->  %d  cpuutil- >  %.2f     SpatialTemperature->   %.2f + " +
////					 "inletTemperature->  %.2f  TotalDynamictemperature->  %.2f " ,
////				host.getId(), host.getUtilizationOfCpu() ,  spatialEffectTemperature, inletTemperature , hostDynamicTemperature
////				  ));
//
//		return  hostDynamicTemperature;
//	}

//	//Used in analyutical model prediction
//	public double getDynamicHostTemperature(PowerHost host, double hostPower, double inletTemperature) {
//
//		//T(cpu) = PR + Tin + (T0 -PR -Tin)*e^(-t/RC);
//		double PR = hostPower * ThermalConstants.THERMAL_RESISTANCE;
//		double inletTemperatureInKelvin = inletTemperature + 273.5;
//
//		double initialSetting = (ThermalConstants.INITIAL_CPU_TEMPERATURE - PR - inletTemperatureInKelvin );
//		double exponentFraction = 0.5;//Math.pow(2.718281, (-1)/(ThermalConstants.THERMAL_RESISTANCE* ThermalConstants.HEAT_CAPACITY));
//
//		double delta = initialSetting * exponentFraction;
//
//		double dynamicTemperature =  PR +  inletTemperatureInKelvin + delta;
//
//
//		return  (dynamicTemperature -273.5);  // Converting in celcius
//
//	}



	//TO DO . Dynamic supply temperature optimization
	public double getCRACColdAirSupplyTemperature(){
		return  this.coldAirSupplyTemperature;
	}

	public void setCRACColdAirSupplyTemperature(){

		this.coldAirSupplyTemperature = ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE;
	}



	public double getTemperatureDueToSpatialEffect(double timeFrameHostEnergy, PowerHost host, double timeDiff){

		// get the matrix of all servers of same zone and calculate temperature
		double hostTemperaturedueToSpatialEffect = 0;

		int	relativeHostId = (int)( host.getId()% DatacenterConstants.NUMBER_OF_HOSTS_PER_ZONE);

		List <PowerHost> hostList =  getHostList();

		int baseIdOfHostZone = host.getId() - relativeHostId;


		for (int i = baseIdOfHostZone; i < baseIdOfHostZone + DatacenterConstants.NUMBER_OF_HOSTS_PER_ZONE ; i++){

			PowerHost _host = hostList.get(i);
			double prevCPUUtil = _host.getPreviousUtilizationOfCpu();
			double cpuutil = _host.getUtilizationOfCpu();
 			double hostEnergy =  _host.getEnergyLinearInterpolation(prevCPUUtil,
					cpuutil, timeDiff);

			//TODO donot multiply with time interval energy, it should be with instant energy
			hostTemperaturedueToSpatialEffect += spatialTemperatureMatrix[(int)(_host.getId() %	DatacenterConstants.NUMBER_OF_HOSTS_PER_ZONE)][relativeHostId]
					* (hostEnergy/timeDiff);

			/*Log.printDebugMessages( " @PDCGTDTSEXYZ  i-> " + ((int)(_host.getId() %	DatacenterConstants.NUMBER_OF_HOSTS_PER_ZONE)) + " j-> " + relativeHostId
										+ " DValue ->" + spatialTemperatureMatrix[(int)(_host.getId() % DatacenterConstants.NUMBER_OF_HOSTS_PER_ZONE)][relativeHostId]
					+  " HostEnergy-> " + hostEnergy +  " timeFrameEnrgy->" + timeFrameHostEnergy);*/


		}
			return  hostTemperaturedueToSpatialEffect;
		}


	// TO DO, pass this as constructor parameter
	public void setSpatialTemperatureMatrix() throws IOException{
		this.spatialTemperatureMatrix = ThermalHelper.getSpatialTemperatureMatrix();

	}

	public  void setHostTemperatureData(PowerHost host,  double spatialEffectTemperature,double  hostDynamicTemperature,
										double colddAirTemperature, double inletTemperature){
	host.temperature = hostDynamicTemperature;
	host.spatialEffectTemperature = spatialEffectTemperature;
	host.inletTemperature = inletTemperature;
	host.colddAirTemperature = colddAirTemperature;

	}

	/**
	 * Get the linked hash map that records the number of active hosts at each interval
	 * @return
	*/

	public LinkedHashMap<Double, Integer> getNumberOfActiveHostsMap(){
		return numberOfActiveHostMap;
	}

	public LinkedHashMap<Double, Integer> getNumberOfActiveHotspotsMap() {
		return numberOfActiveHotspotsMap;
	}

	public LinkedHashMap<Double, Double> getpeakTemperatureMap() {
		return peakTemperatureMap;
	}
	public LinkedHashMap<Double, Double> getAverageTemperatureMap() {
		return averageTemperatureMap;
	}

	public List<String> getVmPlacementHistory() {
		return vmPlacementHistory;
	}

	public LinkedHashMap<Double, String> getActiveHistory() {
		return activeHistory;
	}

	public ArrayList<String> getTempHistory() {
		return tempHistory;
	}


	public void setNumberofTemperatureThreshHoldViolation(int numberofTemperatureThreshHoldViolation){
		this.numberofTemperatureThreshHoldViolation = numberofTemperatureThreshHoldViolation;
	}

	public void  setTemperatureThreshold(){
		this.temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
	}
	public  int  getNumberofTemperatureThreshHoldViolation(){
		return  this.numberofTemperatureThreshHoldViolation;
	}

	/**
	 * @Author-Shashi
	 */
	public double getTemperatureThreshhold() {
		return this.temperatureThreshold;
	}

}
