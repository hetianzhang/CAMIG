package org.cloudbus.cloudsim;

//import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class UtilizationModelNetworkTxBitBrainInMemory implements UtilizationModel {

    /** The scheduling interval. */
    private double schedulingInterval;


    /** The cpuUtilizationData for Outgoing/Transmitted BandWidth Utilization**/
    private  final  double[] bandwidthTXUtilizationData;

    /**
     * Instantiates a new utilization model Bitbrain with variable cpuUtilizationData samples.
     *
     * @param inputPath the input path
     * @param dataSamples number of samples in the file
     * @throws NumberFormatException the number format exception
     * @throws IOException Signals that an I/O exception has occurred.
     */

    //TODO load other cpuUtilizationData fields and modify the cpuUtilizationData of bitbrains for 24 hour
    public UtilizationModelNetworkTxBitBrainInMemory(String inputPath, double schedulingInterval, int dataSamples)
            throws NumberFormatException,
            IOException {
        setSchedulingInterval(schedulingInterval);
        bandwidthTXUtilizationData   = new double[dataSamples];
// should be less than or equal to number of entry in cpuUtilizationData file
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputPath));
        int n = bandwidthTXUtilizationData.length;

        for (int i = 0; i < n - 1; i++) {
            //Skip the CSV header
            String line= bufferedReader.readLine(); // TODO check null condition
            if (i==0)
                continue;
            String[] nextRow = line.split(";");
            bandwidthTXUtilizationData[i] = Double.parseDouble(nextRow[10]);

            Log.printDebugMessages("@ "+ UtilizationModelNetworkTxBitBrainInMemory.class.getSimpleName() + " currentRow Data of Workload- Row: " + i +
                    " Array size: " + nextRow.length +
                    " Array  Data: " + Arrays.toString(nextRow));
            Log.printDebugMessages("NetworkTx Util Data: Cloudlet- " + i + " = " + bandwidthTXUtilizationData[i]);

        }
        bandwidthTXUtilizationData[n - 1] = bandwidthTXUtilizationData[n - 2];
        bufferedReader.close();
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.power.UtilizationModel#getUtilization(double)
     */
    // gives utilization percentage of machine/vm of that particular time
    @Override
    public double getUtilization(double time) {
        if (time % getSchedulingInterval() == 0) {
            return bandwidthTXUtilizationData[(int) time / (int) getSchedulingInterval()];
        }
        int time1 = (int) Math.floor(time / getSchedulingInterval());
        int time2 = (int) Math.ceil(time / getSchedulingInterval());
        double utilization1 = bandwidthTXUtilizationData[time1];
        double utilization2 = bandwidthTXUtilizationData[time2];
        double delta = (utilization2 - utilization1) / ((time2 - time1) * getSchedulingInterval());
        double utilization = utilization1 + delta * (time - time1 * getSchedulingInterval());
        return utilization;
    }




    /**
     * Sets the scheduling interval.
     *
     * @param schedulingInterval the new scheduling interval
     */
    public void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

    /**
     * Gets the scheduling interval.
     *
     * @return the scheduling interval
     */
    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    @Override
    public void setUtilization(double utilizaiton, double time) {
        // TODO Auto-generated method stub

    }
}
