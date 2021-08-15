//package org.cloudbus.cloudsim.power;
//
//import com.cloudbus.cloudsim.examples.power.thermal.ThermalConstants;
//import com.cloudbus.cloudsim.examples.power.thermal.ThermalGRASPValueHolder;
//import ilog.concert.IloNumVar;
//import org.cloudbus.cloudsim.Host;
//import org.cloudbus.cloudsim.Log;
//import org.cloudbus.cloudsim.Vm;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Set;
//
//import ilog.concert.IloException;
//import ilog.cplex.IloCplex;
//import ilog.cplex.IloCplexModeler;
//
//
//public class PowerVmAllocationPolicyThermalOptimal extends PowerVmAllocationPolicyMigrationAbstract{
//
//        double parameter = 0;
//
//        double temperatureThreshold = ThermalConstants.MAXIMUM_THRESHHOLD_TEMPERATURE;
//        double utilizationThreshold = 0.9;
//
//        /**Constructor that instantiates the object and sets up the class member*/
//        public PowerVmAllocationPolicyThermalOptimal(List<? extends Host> hostList,
//                                                   PowerVmSelectionPolicy vmSelectionPolicy,
//                                                   double parameter){
//            super(hostList, vmSelectionPolicy);
//
//            setsafetyparameter(parameter);
//        }
//
//        public PowerVmAllocationPolicyThermalOptimal(List<? extends Host> hostList,
//                                                   PowerVmSelectionPolicy vmSelectionPolicy
//        ){
//            super(hostList, vmSelectionPolicy);
//
//        }
//
//        @Override
//        public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
//            double minPower = Double.MAX_VALUE;
//            PowerHost allocatedHost  = null;
//
//            for (PowerHost host : this.<PowerHost>getHostList())
//            {
//                if (excludedHosts.contains(host))
//                {  // acts as candidate list
//                    continue;
//                }
//                if (host.isSuitableForVm(vm))
//                {
////                if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
//                    if (isHostOverUtilizedAfterAllocation(host, vm)) {
//                        continue;
//                    }
//
//                    try
//                    {
//                        double powerAfterAllocation = getPowerAfterAllocation(host, vm);
//                        double powerDiff = powerAfterAllocation - host.getPower();
//                        double powerForCooling = powerAfterAllocation / (0.0068 * ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE *
//                                ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE
//                                + 0.0008 * ThermalConstants.CRAC_COLDAIR_SUPPLY_TEMPERATURE + 0.458);
//                        if (powerAfterAllocation != -1) {
//                            // double powerDiff = powerAfterAllocation - host.getPower();
//
//                            double totalPowerIncurred = powerDiff + powerForCooling;
//
//                            try {
//                                //feasibleSolutionList.put(host, totalPowerIncurred);
//
//                                if (totalPowerIncurred <  minPower){
//                                    Log.printLine("@PVMAPThermal@findHostByGraspInsideIf");
//                                    minPower = totalPowerIncurred;
//                                    allocatedHost = host;
//                                }
//                            }
//
//                            catch (Exception e){
//                                Log.printLine("exception @ ConstructGreedySolution-> " + e.toString());
//                            }
//                       /* Log.printLine("Power- > " + powerAfterAllocation + " coolingpower-> " + powerForCooling + " totalpower->" + totalPowerIncurred +
//                                        " feasibleSolutionSize->" + feasibleSolutionList.size());*/
//                        }
//                    } catch (Exception e) {
//                    }
//                }
//            }
//            return allocatedHost;
//        }
//
//        @Override
//        protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
//            //   Log.printDebugMessages("@PVMThermalPAL");
//            double power = 0;
//            try {
//                power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.exit(0);
//            }
//            return power;
//        }
//
//        public  void setsafetyparameter(double parameter){
//            this.parameter = parameter;
//        }
//
//        public  double  getTemperatureThreshhold(){
//            return this.temperatureThreshold;
//        }
//
//
//        @Override
//        protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
//            //  Log.printDebugMessages("@PVMThermlOverutilized");
//            boolean isHostOverUtilizedAfterAllocation = true; // default value is true
//            if (host.vmCreate(vm)) { // acts as both boolean updater and checks whether vm creation is possible or not
//                isHostOverUtilizedAfterAllocation = isHostOverUtilized(host); // updates the boolean flag
//                host.vmDestroy(vm);
//                // Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
//            }
//
//            //  Log.printLine("@PVMAPThermalutilAfterAllocation-> " + isHostOverUtilizedAfterAllocation);
//
//            return isHostOverUtilizedAfterAllocation;
//        }
//
//        @Override
//        protected boolean isHostOverUtilized(PowerHost host) {
//            //  addHistoryEntry(host, getUtilizationThreshold());
//            double totalRequestedMips = 0;
//            for (Vm vm : host.getVmList()) {
//                totalRequestedMips += vm.getCurrentRequestedTotalMips();
//            }
//            double utilization = totalRequestedMips / host.getTotalMips();
//            double hostPower = host.getPowerModel().getPower(utilization);
//            // GET AN OPTION TO CALCULATE DYNAMIC TEMPERATURE
//            if(host.getDynamicTemperatureAtHost(hostPower) > temperatureThreshold || utilization > getUtilizationThreshold()){
//                return  true;
//            }
//
//            else{
//                return  false;
//            }
//
//        }
//
//    protected double getUtilizationThreshold() {
//
//            return utilizationThreshold;
//    }
//
//
//}