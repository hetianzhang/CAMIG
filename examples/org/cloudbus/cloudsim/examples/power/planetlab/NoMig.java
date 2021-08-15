package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class NoMig {
    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        String experimentName = "planetlab_npa";
        String outputFolder = "output";
        String inputFolder = NonPowerAware.class.getClassLoader().getResource("workload/planetlab/20110303")
                .getPath();

        Log.setDisabled(!Constants.ENABLE_OUTPUT);
        Log.printLine("Starting " + experimentName);

        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            DatacenterBroker broker = Helper.createBroker();
            int brokerId = broker.getId();

            List<Cloudlet> cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            List<Vm> vmList = Helper.createVmList(brokerId, cloudletList.size());
            List<PowerHost> hostList = Helper.createHostList(PlanetLabConstants.NUMBER_OF_HOSTS);

            PowerDatacenterNonPowerAware datacenter = (PowerDatacenterNonPowerAware) Helper.createDatacenter(
                    "Datacenter",
                    PowerDatacenterNonPowerAware.class,
                    hostList,
                    new PowerVmAllocationPolicySimple(hostList));

            datacenter.setDisableMigrations(true);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
            double lastClock = CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            CloudSim.stopSimulation();

            Helper.printResults(
                    datacenter,
                    vmList,
                    lastClock,
                    experimentName,
                    Constants.OUTPUT_CSV,
                    outputFolder);

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
            System.exit(0);
        }

        Log.printLine("Finished " + experimentName);
    }

}
