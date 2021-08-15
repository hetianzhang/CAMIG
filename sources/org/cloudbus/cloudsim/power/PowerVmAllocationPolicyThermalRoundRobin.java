package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;

import java.util.*;

public class PowerVmAllocationPolicyThermalRoundRobin extends PowerVmAllocationPolicyMigrationAbstract{

    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;

    int lastAssignedHost = 0;

    double utilizationThreshold = 0.9;
    /**Constructor that instantiates the object and sets up the class member*/
    public PowerVmAllocationPolicyThermalRoundRobin(List<? extends Host> hostList,
                                                 PowerVmSelectionPolicy vmSelectionPolicy,
                                                 double parameter){
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalRoundRobin(List<? extends Host> hostList,
                                                 PowerVmSelectionPolicy vmSelectionPolicy
    ){
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        PowerHost allocatedHost  = null;
        Log.printLine("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  @@ findHostforVM%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        Log.printDebugMessages("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%DEBUGMESSAGE$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
         List<PowerHost> hostList = this.<PowerHost> getHostList();
         int hostListSize =  hostList.size();
         Log.printLine("$$$$$$$$$$$$$$$$$$$$$$$$$ LastAssigned-> " + lastAssignedHost);
          if(lastAssignedHost >= hostListSize -1){ // Set the round robin
              lastAssignedHost = 0;
          }

      //  for (PowerHost host : this.<PowerHost>getHostList()){

        for (int i = lastAssignedHost ; i < hostListSize; i++){
               PowerHost host = hostList.get(i);
            if (excludedHosts.contains(host)) {  // acts as candidate list
                continue;
            }
            if (host.isSuitableForVm(vm)) {
                // if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                if (isHostOverUtilizedAfterAllocation(host, vm)) {
                    continue;
                }
                try {
                        allocatedHost = host;
                    Log.printLine("$$$$$$$$$$$$$$$$$$$$$$$$$ LastAssignedAllocatedHost-> " + allocatedHost.getId());
                        lastAssignedHost = i;
                    if (allocatedHost != null)
                        Log.printLine("@ " + PowerVmAllocationPolicyThermalRoundRobin.class.getName()+
                                " Utilization Level: Clock: " + CloudSim.clock() +  " Allocated Host: " +  allocatedHost.getId() +  " CPU- "+ allocatedHost.getUtilizationOfCpu()
                                +  " Ram allocated: " +  allocatedHost.getRam()+ " Ram Used: "+ allocatedHost.getUtilizationOfRam() + " Bandwidth:" + allocatedHost.getBw()
                                + " Bandwidth Used: " + allocatedHost.getUtilizationOfBw()
                                + " Number of VM: " + allocatedHost.getVmList().size() );


                        return  allocatedHost;


                }

                catch (Exception e) {
                    Log.printLine("exception @CMAllocation@Coolest@ FindHostForVm-> " + e.toString());
                }
            }
        }

       /*if(allocatedHost == null)
       {
           System.out.println("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
           Log.printLine("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
           System.exit(0);
       }*/
        return allocatedHost;
    }




    public  void setsafetyparameter(double parameter){
        this.parameter = parameter;
    }

    public  double  getTemperatureThreshhold(){
        return this.temperatureThreshold;
    }


//******************************************END OF GRASP**************************************************************//


    @Override
    protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
        //  Log.printDebugMessages("@PVMThermlOverutilized");
        boolean isHostOverUtilizedAfterAllocation = true; // default value is true
        if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
            isHostOverUtilizedAfterAllocation = isHostOverUtilized(host); // updates the boolean flag
            host.vmDestroy(vm);
            // Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
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

