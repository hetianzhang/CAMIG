import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 */

/**
 * @author JD
 *
 */
public class CloudsimSimulationPointBufferExperiment {

    /**
     * @param args
     */

    public static void main(String[] args) {

        // 1.0: Initiate the cloudsim package. It should be called before
        // creating any entities

        int numUser = 5000;
        Calendar cal = Calendar.getInstance();
        boolean traceFlag = true;
        CloudSim.init(numUser, cal, traceFlag);

        // 2.0: Create Datacenter(s) (Datacenter<<-- Datacentercharacteristics
        // <<-- HostList <<-- Processing element List) (Defines policy for VM
        // allocation and scheduling)

        Datacenter dc = CreateDataCenter();

        // 3.0: Create broker

        DatacenterBroker dcb = null;

        try {
            dcb = new DatacenterBroker("DatacenterBroker1");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 4.0: Create Cloudlets(Defines the workload)

        List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

        long cloudletLength = 5000;
        int pesNumber = 1;
        long cloudletFileSize = 29280;
        long cloudletOutputSize = 141600;
        UtilizationModelFull fullUtilize = new UtilizationModelFull();
        //int cloudletId =0;
        for (int cloudletId = 0; cloudletId < 11200; cloudletId++) {
            Random r = new Random();

            Cloudlet newCloudlet = new Cloudlet(cloudletId, (cloudletLength + r.nextInt(10000)), pesNumber,
                    cloudletFileSize, cloudletOutputSize, fullUtilize, fullUtilize, fullUtilize);

            newCloudlet.setUserId(dcb.getId()); // Cloudlet attached with any of
            // user id (Cloudlet Owner)
            cloudletList.add(newCloudlet);
        }

        // 5.0: Create VMs (Define the procedure for Task Scheduling algorithm)
        List<Vm> vmList = new ArrayList<Vm>();

        long diskSize = 1000;
        int ram = 2000;
        int mips = 1000;
        int bandwidth = 1000;
        int vCPU = 1;
        String VMM = "XEN";

        for (int vmId = 0; vmId < 200; vmId++) {

            Vm virtualMachine = new Vm(vmId, dcb.getId(), mips, vCPU, ram, bandwidth, diskSize, VMM,
                    new CloudletSchedulerTimeShared());
            vmList.add(virtualMachine);
        }

        dcb.submitCloudletList(cloudletList);
        dcb.submitVmList(vmList);

        // 6.0: Starts the Simulation(automated process, handled through
        // descreted event simulation engine)

        CloudSim.startSimulation();

        List<Cloudlet> finalClouletExecutionResults = dcb.getCloudletReceivedList();

        CloudSim.stopSimulation();

        // 7.0: Print results when simulation is over (Outputs)

        try {
            String s = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/log/templog.txt";
            System.setOut(new PrintStream(new FileOutputStream(s)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int cloudletNo = 0;
        for (Cloudlet c : finalClouletExecutionResults) {

            //System.out.println("This is test output");
            System.out.println("Result of cloudlet No: " + cloudletNo);
            System.out.println("*****************************");
            System.out.println("ID:" + c.getCloudletId() + ",VM:" + c.getVmId() + ",WaitingTime:" + c.getWaitingTime() + ",status:" + c.getStatus()
                    + ",Execution Time:" + c.getActualCPUTime() + ",Start:" + c.getExecStartTime() + ",Finish:" + c.getFinishTime());
            System.out.println("*****************************");

            Log.printLine("Result of cloudlet No: " + cloudletNo);
            Log.printLine("*****************************");
            Log.printLine("ID:" + c.getCloudletId() + ",VM:" + c.getVmId() + ",status:" + c.getStatus()
                    + ",Execution Time:" + c.getActualCPUTime() + ",Start:" + c.getExecStartTime() + ",Finish:" + c.getFinishTime());
            Log.printLine("*****************************");
            cloudletNo++;

        }

    }

    private static Datacenter CreateDataCenter() {
        List<Pe> peList0 = new ArrayList<Pe>();
        List<Pe> peList1 = new ArrayList<Pe>();
        List<Pe> peList2 = new ArrayList<Pe>();
        List<Pe> peList3 = new ArrayList<Pe>();
        List<Pe> peList4 = new ArrayList<Pe>();
        List<Pe> peList5 = new ArrayList<Pe>();
        List<Pe> peList6 = new ArrayList<Pe>();
        List<Pe> peList7 = new ArrayList<Pe>();
        List<Pe> peList8 = new ArrayList<Pe>();
        List<Pe> peList9 = new ArrayList<Pe>();
        //List<Pe> peList10 = new ArrayList<Pe>();

        PeProvisionerSimple pProvisioner = new PeProvisionerSimple(10000);

        Pe core0 = new Pe(0, pProvisioner);
        peList0.add(core0);
        Pe core1 = new Pe(1, pProvisioner);
        peList0.add(core1);
        Pe core2 = new Pe(2, pProvisioner);
        peList0.add(core2);
        Pe core3 = new Pe(3, pProvisioner);
        peList0.add(core3);

        Pe core4 = new Pe(4, pProvisioner);
        peList1.add(core4);
        Pe core5 = new Pe(5, pProvisioner);
        peList1.add(core5);
        Pe core6 = new Pe(6, pProvisioner);
        peList1.add(core6);
        Pe core7 = new Pe(7, pProvisioner);
        peList1.add(core7);

        Pe core8 = new Pe(8, pProvisioner);
        peList2.add(core8);
        Pe core9 = new Pe(9, pProvisioner);
        peList2.add(core9);
        Pe core10 = new Pe(10, pProvisioner);
        peList2.add(core10);
        Pe core11 = new Pe(11, pProvisioner);
        peList2.add(core11);

        Pe core12 = new Pe(12, pProvisioner);
        peList3.add(core12);
        Pe core13 = new Pe(13, pProvisioner);
        peList3.add(core13);
        Pe core14 = new Pe(14, pProvisioner);
        peList3.add(core14);
        Pe core15 = new Pe(15, pProvisioner);
        peList3.add(core15);

        Pe core16 = new Pe(16, pProvisioner);
        peList4.add(core16);
        Pe core17 = new Pe(17, pProvisioner);
        peList4.add(core17);
        Pe core18 = new Pe(18, pProvisioner);
        peList4.add(core18);
        Pe core19 = new Pe(19, pProvisioner);
        peList4.add(core19);

        Pe core20 = new Pe(20, pProvisioner);
        peList5.add(core20);
        Pe core21 = new Pe(21, pProvisioner);
        peList5.add(core21);
        Pe core22 = new Pe(22, pProvisioner);
        peList5.add(core22);
        Pe core23 = new Pe(23, pProvisioner);
        peList5.add(core23);

        Pe core24 = new Pe(24, pProvisioner);
        peList6.add(core24);
        Pe core25 = new Pe(25, pProvisioner);
        peList6.add(core25);
        Pe core26 = new Pe(26, pProvisioner);
        peList6.add(core26);
        Pe core27 = new Pe(27, pProvisioner);
        peList6.add(core27);

        Pe core28 = new Pe(28, pProvisioner);
        peList7.add(core28);
        Pe core29 = new Pe(29, pProvisioner);
        peList7.add(core29);
        Pe core30 = new Pe(30, pProvisioner);
        peList7.add(core30);
        Pe core31 = new Pe(31, pProvisioner);
        peList7.add(core31);

        Pe core32 = new Pe(32, pProvisioner);
        peList8.add(core32);
        Pe core33 = new Pe(33, pProvisioner);
        peList8.add(core33);
        Pe core34 = new Pe(34, pProvisioner);
        peList8.add(core34);
        Pe core35 = new Pe(35, pProvisioner);
        peList8.add(core35);

        Pe core36 = new Pe(36, pProvisioner);
        peList9.add(core36);
        Pe core37 = new Pe(37, pProvisioner);
        peList9.add(core37);
        Pe core38 = new Pe(38, pProvisioner);
        peList9.add(core38);
        Pe core39 = new Pe(39, pProvisioner);
        peList9.add(core39);

        //Pe core40 = new Pe(40, pProvisioner);
        //peList10.add(core40);
        //Pe core41 = new Pe(41, pProvisioner);
        //peList10.add(core41);
        //Pe core42 = new Pe(42, pProvisioner);
        //peList10.add(core42);
        //Pe core43 = new Pe(43, pProvisioner);
        //peList10.add(core43);

        List<Host> hostlist = new ArrayList<Host>();
        int ram = 32000;
        int bw = 10000;
        long storage = 100000;

        Host host0 = new Host(0, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList0,
                new VmSchedulerTimeShared(peList0));
        hostlist.add(host0);

        Host host1 = new Host(1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1,
                new VmSchedulerTimeShared(peList1));
        hostlist.add(host1);

        Host host2 = new Host(2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2,
                new VmSchedulerTimeShared(peList2));
        hostlist.add(host2);

        Host host3 = new Host(3, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList3,
                new VmSchedulerTimeShared(peList3));
        hostlist.add(host3);

        Host host4 = new Host(4, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList4,
                new VmSchedulerTimeShared(peList4));
        hostlist.add(host4);

        Host host5 = new Host(5, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList5,
                new VmSchedulerTimeShared(peList5));
        hostlist.add(host5);

        Host host6 = new Host(6, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList6,
                new VmSchedulerTimeShared(peList6));
        hostlist.add(host6);

        Host host7 = new Host(7, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList7,
                new VmSchedulerTimeShared(peList7));
        hostlist.add(host7);

        Host host8 = new Host(8, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList8,
                new VmSchedulerTimeShared(peList8));
        hostlist.add(host8);

        Host host9 = new Host(9, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList9,
                new VmSchedulerTimeShared(peList9));
        hostlist.add(host9);

        //Host host10 = new Host(10, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList10,
        //	new VmSchedulerTimeShared(peList10));
        //hostlist.add(host10);

        String architecture = "x86";
        String os = "Linux";
        String vmm = "XEN";
        double timeZone = 5.0;
        double ComputecostPerSec = 3.0;
        double costPerMem = 1.0;
        double costPerStorage = 0.05;
        double costPerBw = 0.10;
        DatacenterCharacteristics dcCharacteristics = new DatacenterCharacteristics(architecture, os, vmm, hostlist,
                timeZone, ComputecostPerSec, costPerMem, costPerStorage, costPerBw);

        LinkedList<Storage> SANstorage = new LinkedList<Storage>();
        Datacenter dc = null;
        try {
            dc = new Datacenter("Datacenter1", dcCharacteristics, new VmAllocationPolicySimple(hostlist), SANstorage,
                    1);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dc;
    }
}