package org.cloudbus.cloudsim.power;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
import com.cloudbus.cloudsim.examples.power.thermal.ThermalGRASPValueHolder;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.MathUtil;

public class PowerVmAllocationPolicyThermalGRASP extends PowerVmAllocationPolicyMigrationAbstract{


    double parameter = 0;

    double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
    double utilizationThreshold = 0.9;

    /**Constructor that instantiates the object and sets up the class member*/
   public PowerVmAllocationPolicyThermalGRASP(List<? extends Host> hostList,
                                              PowerVmSelectionPolicy vmSelectionPolicy,
                                              double parameter){
        super(hostList, vmSelectionPolicy);

        setsafetyparameter(parameter);
    }

    public PowerVmAllocationPolicyThermalGRASP(List<? extends Host> hostList,
                                               PowerVmSelectionPolicy vmSelectionPolicy
                                   ){
        super(hostList, vmSelectionPolicy);

    }

    @Override
    public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
        double minPower = Double.MAX_VALUE;
        PowerHost allocatedHost  = null;
        allocatedHost = findHostByGRASP(vm, excludedHosts);

       /*if(allocatedHost == null)
       {
           System.out.println("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
           Log.printLine("@PVMAAPThermal@findHostforVM ->GRASP returned null Host");
           System.exit(0);
       }*/
        return allocatedHost;
    }

    @Override
    protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
     //   Log.printDebugMessages("@PVMThermalPAL");
        double power = 0;
        try {
            power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }


    public  void setsafetyparameter(double parameter){
        this.parameter = parameter;
    }

    public  double  getTemperatureThreshhold(){
        return this.temperatureThreshold;
    }

    //***********************************GRASP**********************************************//
    public  PowerHost findHostByGRASP(Vm vm, Set<? extends Host> excludedHosts){
        //PowerHost allocatedHost = null;

       // LinkedHashMap <PowerHost, Double> currentBest = null;
        ThermalGRASPValueHolder currentBest = new ThermalGRASPValueHolder();
        currentBest.setGreedyGRASPValue(Double.MAX_VALUE);
        currentBest.setHost(null);

        double delta = 0 ;
        double epsilon = 0.1;
        int lastCountDeltaNotChanged = 0;
        double previousIterationValue = 0;
        double currentIterationvalue;

        // TO DO Make RCL based on nonactive and excluded hosts
        boolean isSolutionNotReached =  true;

        while(isSolutionNotReached){

            //TODO . Consider the global total power for each time frame n adjust the supply temperature.

            LinkedHashMap<PowerHost, Double> feasibleSolutionList = constructGreedySolution(vm, excludedHosts);
            ThermalGRASPValueHolder newBestSolution = LocalSearch(feasibleSolutionList);

            if (newBestSolution.getGreedyGRASPValue() < currentBest.getGreedyGRASPValue()){
            //    Log.printLine("@PVMAPThermal@findHostByGraspInsideIf");
                currentBest.setGreedyGRASPValue(newBestSolution.getGreedyGRASPValue());
                currentBest.setHost(newBestSolution.getHost());
                lastCountDeltaNotChanged = 0;
            }

            else{
                lastCountDeltaNotChanged++;
              //  System.out.println("%%%%%%%%%%% Delta is greater than 0");
            }


          // Stop the GRASP

//            if(lastCountDeltaNotChanged > delta){
//                isSolutionNotReached = false;
//            //    System.out.println("Solution  Reached- Breaking the Loop");
//            }
            // new technique to stop.
            double improvedCost=  currentBest.getGreedyGRASPValue() - newBestSolution.getGreedyGRASPValue() ;
            if (improvedCost > epsilon){
                currentBest.setGreedyGRASPValue(newBestSolution.getGreedyGRASPValue());
                currentBest.setHost(newBestSolution.getHost());
            }
            else
            {
                isSolutionNotReached = false;
            }
            /************************************************************/
        }
        /*Log.printLine("################## + currenBestPower-> " + currentBest.getGreedyGRASPValue() + "hostdid-> "
                        + currentBest.getHost().getId() );*/
        return  currentBest.getHost();
    }

    public   LinkedHashMap<PowerHost, Double> constructGreedySolution(Vm vm, Set<? extends Host> excludedHosts) {

        double minPowerGreedy = Double.MAX_VALUE;
        LinkedHashMap<PowerHost, Double>  feasibleSolutionList = new LinkedHashMap<PowerHost, Double>();

        int numberOfTries = 25;//200; // define in terms of beta - 0-1 -> 1-> full serach 0 -> no try, 0.1-> 10 % of the sample space
        List<PowerHost> hosts = this.<PowerHost>getHostList();
        //TODO, statastical definition of numberoftries
        for (int i = 0 ; i < numberOfTries;  i ++){
            //for (PowerHost host : this.<PowerHost>getHostList()) {

            PowerHost host = hosts.get(MathUtil.getRandomNumber((int)System.nanoTime(), hosts.size()));
            if (excludedHosts.contains(host)) {  // acts as candidate list
                continue;
            }

            if (host.isSuitableForVm(vm)) {
                if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
                    continue;
                }

                try {
                    double powerAfterAllocation = getPowerAfterAllocation(host, vm);
                    double powerDiff = powerAfterAllocation - host.getPower();

                    double powerForCooling = powerAfterAllocation / (0.0068 * ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE *
                            ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE
                            + 0.0008 * ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE + 0.458);
                    if (powerAfterAllocation != -1) {
                        // double powerDiff = powerAfterAllocation - host.getPower();

                        double totalPowerIncurred = powerDiff + powerForCooling;

                        try {
                                feasibleSolutionList.put(host, totalPowerIncurred);
                        }
                        catch (Exception e){
                            Log.printLine("exception @ ConstructGreedySolution-> " + e.toString());
                        }

                       /* Log.printLine("Power- > " + powerAfterAllocation + " coolingpower-> " + powerForCooling + " totalpower->" + totalPowerIncurred +
                                       " feasibleSolutionSize->" + feasibleSolutionList.size());*/
                    }
                } catch (Exception e) {
                }
            }
        }

       // Log.printLine("@PVMAPThermal@ConstructSolution -> feasibleSolutionSize-> " +  feasibleSolutionList.size());
        return feasibleSolutionList;
    }

    public  ThermalGRASPValueHolder LocalSearch( LinkedHashMap<PowerHost, Double> feasibleSolutionList){

        double minPowerIncurred = Double.MAX_VALUE;
        ThermalGRASPValueHolder localOptima = new ThermalGRASPValueHolder();
        if(feasibleSolutionList.size() > 0) {

            for (PowerHost host : feasibleSolutionList.keySet()) {
                //Log.printLine("$$$$$$$$$$$$$$$$$$$$$$ @InsideLocalSeachInsideFor->  fesibleSize-> " + feasibleSolutionList.size() );
                if (feasibleSolutionList.get(host) < minPowerIncurred  ) {
                    minPowerIncurred = feasibleSolutionList.get(host);
                    localOptima.setHost(host);
                    localOptima.setGreedyGRASPValue(minPowerIncurred);
                 //   Log.printLine("$$$$$$$$$$$$$$$$$$$$$$ @InsideLocalSeachInIf");
                }
            }
        }
        else
        {
          //  System.out.println("@ERROR---> @PVMAPThermal@LocalSearch- > Feasible Solution List is null");
//            Log.printLine("@ERROR---> @PVMAPThermal@LocalSearch- > Feasible Solution List is null");
            //System.exit(0);
        }
        return  localOptima;
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
        double utilization = totalRequestedMips / host.getTotalMips();
        double hostPower = host.getPowerModel().getPower(utilization);

        // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
//        Log.printLine("@PVMGRASP@ISOVerUtil HostId - > "+ host.getId() + " util-> " + utilization + " hostPower-> " + hostPower + " Temperature-> "
//                +  host.getDynamicTemperatureAtHost(hostPower));

        if(host.getDynamicTemperatureAtHost(hostPower) > temperatureThreshold || utilization > getUtilizationThreshold()){
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
