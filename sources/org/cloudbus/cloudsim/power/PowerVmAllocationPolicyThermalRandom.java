package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.MathUtil;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Random;


public class PowerVmAllocationPolicyThermalRandom  extends PowerVmAllocationPolicyMigrationAbstract{


    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;

    double utilizationThreshold = 0.9;

    /**Constructor that instantiates the object and sets up the class member*/
    public PowerVmAllocationPolicyThermalRandom(List<? extends Host> hostList,
                                                 PowerVmSelectionPolicy vmSelectionPolicy,
                                                 double parameter){
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalRandom(List<? extends Host> hostList,
                                                 PowerVmSelectionPolicy vmSelectionPolicy
    ){
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        PowerHost allocatedHost  = null;
        Log.printDebugMessages("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& @ findhostforVMRandom#########################");
        List <PowerHost> hosts =  getHostList();
        boolean isRandomHostNotFound = true;
        int max= hosts.size();
        int totalTries = 0;

        while(isRandomHostNotFound)
        {
                // Log.printLine("@PVMAPThermalRandom " +"HostList Size-> " + hosts.size()+ " Selected Host-> " + randomHost
                //            + " Util-> " + host.getUtilizationOfCpu());
            int seed = (int)System.nanoTime();
            int randomHost =  MathUtil.getRandomNumber(seed, hosts.size());
            PowerHost host = hosts.get(randomHost);
            totalTries ++;
            if(totalTries > hosts.size()){
                isRandomHostNotFound = false;

                System.out.println("%%%%%%%%%%%%%%%%%%############################Broken the loop");
            }
            if (excludedHosts.contains(host)) {  // acts as candidate list
                    continue;
            }
            if (host.isSuitableForVm(vm)) {
//                 if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
               if (isHostOverUtilizedAfterAllocation(host, vm)) {
                 //   if ( isHostOverUtilizedAfterAllocation(host, vm)){
                    System.out.print("Continue:  id: " + host.getId() + "\n");
                    continue;
                }

                try {
                    allocatedHost = host;

                   // System.out.println("***********************Assigned host in while loop");
                    isRandomHostNotFound = false;
                    return allocatedHost;

                } catch (Exception e) {
                    Log.printLine("exception @CMAllocation@Random@ FindHostForVm-> " + e.toString());
                }
            }
        }

        return allocatedHost;
    }

    public  void setsafetyparameter(double parameter){
        this.parameter = parameter;
    }

    public  double  getTemperatureThreshhold(){
        return this.temperatureThreshold;
    }




    @Override
    protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
        //  Log.printDebugMessages("@PVMThermlOverutilized");
        boolean isHostOverUtilizedAfterAllocation = true; // default value is true
        if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host); // updates the boolean flag
            host.vmDestroy(vm);
//            Log.printLine("@PVMAPThermalutilAfterAllocationRandom-> " + isHostOverUtilizedAfterAllocation);
        }

        //  Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
        return isHostOverUtilizedAfterAllocation;
    }

    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
        //  addHistoryEntry(host, getUtilizationThreshold());
        double totalRequestedMips = 0;
        for (Vm vm : host.getVmList()) {
            totalRequestedMips += vm.getCurrentRequestedTotalMips();
        }
        double utilization = totalRequestedMips / host.getTotalMips();  // This method instead getutilize() is used,
        // bcz a vm is temporarily created at host in after allocation method

        // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
        /*if(host.getDynamicTemperatureAtHost(utilization) > temperatureThreshold){
            return  true;
        }

        else{
            return  false;
        }
*/
        return utilization > getUtilizationThreshold();

    }

    protected double getUtilizationThreshold() {
        return utilizationThreshold;
    }


}
