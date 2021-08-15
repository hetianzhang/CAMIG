package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.BitBrainConstants;
import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import com.cloudbus.cloudsim.examples.power.thermal.inference.PredictTemperatureHttpCall;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.List;
import java.util.Set;

public class PowerVmAllocationPolicyThermalMinimizePeakTemperature extends PowerVmAllocationPolicyMigrationAbstract {


    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
    double utilizationThreshold = BitBrainConstants.cpuUtilizationThreshold;


    /**
     * Constructor that instantiates the object and sets up the class member
     */
    public PowerVmAllocationPolicyThermalMinimizePeakTemperature(List<? extends Host> hostList,
                                                                 PowerVmSelectionPolicy vmSelectionPolicy,
                                                                 double parameter) {
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalMinimizePeakTemperature(List<? extends Host> hostList,
                                                                 PowerVmSelectionPolicy vmSelectionPolicy
    ) {
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        PowerHost allocatedHost = null;

        double coolestServerTemperature = 99999999;
        int id = -1;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {  // acts as candidate list
                continue;
            }
            if (host.isSuitableForVm(vm)) {
                //          if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                if (isHostOverUtilizedAfterAllocation(host, vm)) {
                    System.out.print("Continue:  id: " + host.getId() + "\n");
                    continue;
                }

                try {
//
                    double hostTemperature = host.getPredictedTemperature();//
                    // with host.getuti, it takes too time @ second interval and consumes less energy
                    if (hostTemperature < coolestServerTemperature) {
                        coolestServerTemperature = hostTemperature;
                        allocatedHost = host;
                        id = allocatedHost.getId();
//                        System.out.println("++++++++++++  hosttemp- >  " + hostTemperature + " Id-> " + host.getId()  + " ++++++++++++++++++++");
                    }
                } catch (Exception e) {
                    Log.printLine("exception @CMAllocation@Coolest@ FindHostForVm-> " + e.toString());
                }
            }
        }


//        Log.printLine("-@PVAMiminizePeakTemp------------ -> Temp->  " + coolestServerTemperature + " Host ID- > " + id + " ---------------------------");
        if (id != -1)
            Log.printLine("@ " + PowerVmAllocationPolicyThermalMinimizePeakTemperature.class.getName() +
                    " Utilization Level: Clock: " + CloudSim.clock() + " Allocated Host: " + id + " CPU- " + allocatedHost.getUtilizationOfCpu()
                    + " Ram allocated: " + allocatedHost.getRam() + " Ram Used: " + allocatedHost.getUtilizationOfRam() + " Bandwidth:" + allocatedHost.getBw()
                    + " Bandwidth Used: " + allocatedHost.getUtilizationOfBw()
                    + " Number of VM: " + allocatedHost.getVmList().size());


        return allocatedHost;
    }


    public void setsafetyparameter(double parameter) {
        this.parameter = parameter;
    }




//***********************************

    @Override
    protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
        boolean isHostOverUtilizedAfterAllocation = true; // default value is true
        if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host); // updates the boolean flag
            host.vmDestroy(vm);
            // Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
        }

        return isHostOverUtilizedAfterAllocation;
    }

    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
        //  addHistoryEntry(host, getUtilizationThreshold());


        // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
//        if (getCurrentTemperature(host) > temperatureThreshold || getCurrentUtilization(host) > getUtilizationThreshold()) {
        double predictedTemperature = host.getPredictedTemperature();
//        || predictedTemperature < 0
        if( predictedTemperature > getTemperatureThreshhold() || host.getCurrentUtilization() > getUtilizationThreshold()
        ){
            return true;
        } else {
            return false;
        }
    }


    /**
     * @Author-Shashi
     */
    protected double getTemperatureAfterAllocation(PowerHost host, Vm vm) {
        double temperature = 0;
        if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
            // updates the boolean flag
            temperature = host.getCurrentTemperature();
            host.vmDestroy(vm);
            // Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
        }

        return temperature;
    }

    /**
     * @Author-Shashi
     */
    protected double getUtilizationThreshold() {

        return utilizationThreshold;
    }

    /**
     * @Author-Shashi
     */
    public double getTemperatureThreshhold() {
        return this.temperatureThreshold;
    }


}
