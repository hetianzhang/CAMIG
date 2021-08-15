package org.cloudbus.cloudsim.power;
import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Set;


public class PowerVmAllocationPolicyThermalCoolest extends PowerVmAllocationPolicyMigrationAbstract{


    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
    double utilizationThreshold = 0.9;


    /**Constructor that instantiates the object and sets up the class member*/
    public PowerVmAllocationPolicyThermalCoolest(List<? extends Host> hostList,
                                               PowerVmSelectionPolicy vmSelectionPolicy,
                                               double parameter){
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalCoolest(List<? extends Host> hostList,
                                               PowerVmSelectionPolicy vmSelectionPolicy
    ){
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost  = null;

        double coolestServerTemperature = 99999999;
        int id = -1;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            if (excludedHosts.contains(host)) {  // acts as candidate list
                continue;
            }
            if (host.isSuitableForVm(vm)) {
   //          if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                 if (isHostOverUtilizedAfterAllocation(host, vm)) {
//                        System.out.print("Continue:  id: " + host.getId() + "\n");
                        continue;
                }

                try {
                    double totalRequestedMips =0;
                    for (Vm vm1 : host.getVmList()) {
                        totalRequestedMips += vm1.getCurrentRequestedTotalMips();
                    }
                    double utilization = totalRequestedMips / host.getTotalMips();  // This method instead getutilize() is used,
                    // bcz a vm is temporarily created at host in after allocation method
                    double hostPower = host.getPowerModel().getPower(utilization);
                    double hostTemperature = host.getDynamicTemperatureAtHost(hostPower);//

//                    Log.printLine("************** hosttemp- >  " + hostTemperature + " Id-> " + host.getId()  + " *********");
                    // with host.getuti, it takes too time @ second interval and consumes less energy
                    if (hostTemperature < coolestServerTemperature ) {
                        coolestServerTemperature = hostTemperature;
                        allocatedHost = host;
                        id = allocatedHost.getId();
//                        System.out.println("++++++++++++  hosttemp- >  " + hostTemperature + " Id-> " + host.getId()  + " ++++++++++++++++++++");
                    }
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

        Log.printLine("------------- -> Temp->  " + coolestServerTemperature + " Host ID- > " +  id + " ---------------------------");
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
        double hostPower = host.getPowerModel().getPower(utilization);
        // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
       if(host.getDynamicTemperatureAtHost(hostPower) > temperatureThreshold || utilization > getUtilizationThreshold()){
//        if( host.getDynamicTemperatureAtHost(hostPower) > temperatureThreshold ){
            return  true;
        }

        else{
            return  false;
        }
    }

    protected double getUtilizationThreshold() {

        return utilizationThreshold;
    }
}
