package org.cloudbus.cloudsim.power;

import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import com.cloudbus.cloudsim.examples.power.thermal.inference.PredictTemperatureHttpCall;
import org.apache.commons.math3.analysis.function.Power;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.List;
import java.util.Set;


import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import java.util.ArrayList;

public class PowerVmAllocationPolicyThermalLPSolverMinPeak extends PowerVmAllocationPolicyMigrationAbstract{


    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
    double utilizationThreshold = 0.9;


    /**Constructor that instantiates the object and sets up the class member*/
    public PowerVmAllocationPolicyThermalLPSolverMinPeak(List<? extends Host> hostList,
                                                                 PowerVmSelectionPolicy vmSelectionPolicy,
                                                                 double parameter){
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalLPSolverMinPeak(List<? extends Host> hostList,
                                                                 PowerVmSelectionPolicy vmSelectionPolicy
    ){
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        double minPower = Double.MAX_VALUE;

        PowerHost allocatedHost  = null;
        allocatedHost = findHosByLPSolver(vm, excludedHosts);


//        PowerHost allocatedHost  = null;
//
//        double coolestServerTemperature = 99999999;
//        int id = -1;
//
//        for (PowerHost host : this.<PowerHost>getHostList()) {
//            if (excludedHosts.contains(host)) {  // acts as candidate list
//                continue;
//            }
//            if (host.isSuitableForVm(vm)) {
//                //          if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
//                if (isHostOverUtilizedAfterAllocation(host, vm)) {
//                    System.out.print("Continue:  id: " + host.getId() + "\n");
//                    continue;
//                }
//
//                try {
//                    double totalRequestedMips =0;
//                    for (Vm vm1 : host.getVmList()) {
//                        totalRequestedMips += vm1.getCurrentRequestedTotalMips();
//                    }
//                    double utilization = totalRequestedMips / host.getTotalMips();  // This method instead getutilize() is used,
//                    // bcz a vm is temporarily created at host in after allocation method
//                    double hostPower = host.getPowerModel().getPower(utilization);
//                    double hostTemperature = host.getDynamicTemperatureAtHost(hostPower);//
//
//                    Log.printLine("@PVMALPSolverMP*****hosttemp- >  " + hostTemperature + " Id-> " + host.getId()  + " *********");
//                    // with host.getuti, it takes too time @ second interval and consumes less energy
//                    if (hostTemperature < coolestServerTemperature ) {
//                        coolestServerTemperature = hostTemperature;
//                        allocatedHost = host;
//                        id = allocatedHost.getId();
//                        System.out.println("++++++++++++  hosttemp- >  " + hostTemperature + " Id-> " + host.getId()  + " ++++++++++++++++++++");
//                    }
//                }
//
//                catch (Exception e) {
//                    Log.printLine("exception @CMAllocation@Coolest@ FindHostForVm-> " + e.toString());
//                }
//            }
//        }
//
//       /*if(allocatedHost == null)
//       {
//           System.out.println("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
//           Log.printLine("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
//           System.exit(0);
//       }*/
//
//        Log.printLine("------------- -> Temp->  " + coolestServerTemperature + " Host ID- > " +  id + " ---------------------------");
        return allocatedHost;
    }


    public  PowerHost findHosByLPSolver(Vm vm, Set<? extends Host> excludedHosts) {

        PowerHost allocatedHost = null;
        Log.printLine("@PVMALPSolver:");


//
//
//        for (PowerHost host : this.<PowerHost>getHostList()) {
//            if (excludedHosts.contains(host)) {  // acts as candidate list
//                continue;
//            }
//            if (host.isSuitableForVm(vm)) {
//                //          if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
//                if (isHostOverUtilizedAfterAllocation(host, vm)) { //  C2 nad C3
//                    System.out.print("Continue:  id: " + host.getId() + "\n");
//                    continue;
//                }
//            }
//        }
//        Log.printLine("@PVMALPSolver:" + " Clock: "+ CloudSim.clock() + " Allocated Host: " + allocatedHost.getId() + " for Vm: " + vm.getId());

        LPWizard lpw = new LPWizard();
        lpw.setMinProblem(true);

        //Objective function
        for(PowerHost host : this.<PowerHost>getHostList()) {

            if (!excludedHosts.contains(host)) {  // Exclude the hosts

                double totalRequestedMips = 0;
                double hostPower = getHostPower(host);
                double currentTemperature = getCurrentTemperature(host);
                double temperatureAfterAllocation = getTemperatureAfterAllocation(host, vm);
                double utilization = getCurrentUtilization(host);
                lpw.plus("t-" + host.getId(), getCurrentTemperature(host)); // Price?
                lpw.setBoolean("t-" + host.getId());
            }
        }
        //total objective value  constraint
        double peakTemperature = 0;


        // Constraint1 -less than threshold
//            Ti < teperatureThreshold
        for(PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine("Inside Constaint 1111111");
            if (!excludedHosts.contains(host)) {
                Log.printLine("Inside Constaint 1");
                LPWizardConstraint temperatureThresholdConstraint = lpw.addConstraint("temp-"+ host.getId(),temperatureThreshold,"<");
                temperatureThresholdConstraint.plus("hostTemp-"+ host.getId(), getCurrentTemperature(host));
                Log.printLine("Test temp: " + getCurrentTemperature(host));
            }
        }


        // Constraint 2, max 1 allocation

        //set constraints: 1. executor placement constraint-> 1 executor in at most 1 agent
        LPWizardConstraint tmpsConsP = lpw.addConstraint("pc" , 1, "=");
        for(PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine("Inside Constaint 22222222222222222");

            if (!excludedHosts.contains(host)) {
                Log.printLine("Inside Constaint 2");

                    tmpsConsP.plus("x-" + "-" +host.getId(), 1);
                    lpw.setBoolean("x-" + "-" +host.getId());
                }
            }


        LinearProgramSolver solver  = SolverFactory.newDefault();
        LPSolution lpsol = lpw.solve(solver);

        Log.printLine("@" + PowerVmAllocationPolicyThermalLPSolverMinPeak.class.getName() + " Finished solving the LP problem " +
                        "Objective Value" +lpsol.getObjectiveValue());

        Log.printLine("@" + PowerVmAllocationPolicyThermalLPSolverMinPeak.class.getName() +
                " LpSolve Value- " + lpsol.toString());

        /// Get the Host Id of solved problem
        int LpSolverId = -1;
        double objVal = lpsol.getObjectiveValue();

        int counter =0;
        if(!Double.isInfinite(objVal)&&objVal>0) {
            for (PowerHost host : this.<PowerHost>getHostList()) {
                boolean lpSolverHostId=  lpsol.getBoolean("hostTemp-"+ host.getId());
                if (lpSolverHostId)
                {

                    Log.printLine("Set to boolean true: " + host.getId());
                    allocatedHost = host;
                    counter ++;
                }
            }

        }
        if (allocatedHost!=null)
        Log.printLine("@" + PowerVmAllocationPolicyThermalLPSolverMinPeak.class.getName() + " Returning from Solver: Allocated Host- "
        +  " Objective Value: " + objVal + " Counter- " + counter   + "Full Object: " + lpsol.toString());

        else
            Log.printLine("Allocated host null");

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


        // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
        if(getCurrentTemperature(host) > temperatureThreshold || getCurrentUtilization(host) > getUtilizationThreshold()){
//        if(getPredictedTemperature(host) > temperatureThreshold || utilization > getUtilizationThreshold()){
            return  true;
        }

        else{
            return  false;
        }
    }

        /** @Author-Shashi*/
    protected  double getHostPower(PowerHost host){

        double hostPower = host.getPowerModel().getPower(getCurrentUtilization(host));

        return  hostPower;

    }

    protected  double getCurrentUtilization(PowerHost host){
        double totalRequestedMips = 0;
        for (Vm vm : host.getVmList()) {
            totalRequestedMips += vm.getCurrentRequestedTotalMips();
        }
        double utilization = totalRequestedMips / host.getTotalMips();  // This method instead getutilize() is used,
        // a vm is temporarily created at host in after allocation method

        return  utilization;

    }

    /** @Author-Shashi*/
    protected double getCurrentTemperature(PowerHost host){

        return  host.getDynamicTemperatureAtHost(getHostPower(host));
    }


    /** @Author-Shashi*/
    protected  double getTemperatureAfterAllocation(PowerHost host, Vm vm){
        double temperature =0;
        if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
              // updates the boolean flag
            temperature = getCurrentTemperature(host);
            host.vmDestroy(vm);
            // Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
        }

        return  temperature;
    }
    protected double getUtilizationThreshold() {

        return utilizationThreshold;
    }
//
//    // HTTP call to a python flask application where inference model is hosted
//    protected String getPredictedTemperature(PowerHost host){
//        PredictTemperatureHttpCall predictTemperature = new PredictTemperatureHttpCall(host);
//
//        String temperature = predictTemperature.getPredictedTemperature();
//
//        return  temperature;
//    }
}
