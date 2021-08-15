package com.cloudbus.cloudsim.examples.power.thermal.utils;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

public class ModifyTimeSeriesFromSecToHour {

    public  ModifyTimeSeriesFromSecToHour(){

    }

    public static void main(String[] args) throws IOException {

        double[][] dMatrix = new double[50][50];

//        String GraspArray = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/newdata/lrrmmt/20110303_lr_mmt_1.2peakTemperature.csv";
//        //"/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/old/grasp/20110303_thermal_mu_0.8NoOfActiveHostByInterval.csv";
//
//        String randomArray = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/old/random/20110303_thermalRandom_mu_0.8NoOfActiveHostByInterval.csv";
//        String RRArray = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/old/RR/20110303_thermalRR_mu_0.8NoOfActiveHostByInterval.csv";
//        String lrmmtArray = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/old/lrrmmt/20110303_lr_mmt_1.2NoOfActiveHostByInterval.csv";
//        String taArray = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/old/coolest/20110303_thermalCoolest_mu_0.8NoOfActiveHostByInterval.csv";
//
//        String hourgraspactive = "/home/shashi/research/code/draft/cloudsim-3.0.3/output/thermal/20110303_lr_mmt_1.2NoOfActiveHostByInterval.csv";
//        String hourGraniteActive =  "/home/shashi/Phd/code/cloudsim-3.0.3/output/thermal/20110303_thermalGranite_mu_200NoOfActiveHostByInterval.csv";
//        String hourGranitePeakTemp = "/home/shashi/Phd/code/cloudsim-3.0.3/output/thermal/20110303_thermalGranite_mu_200peakTemperature.csv";
        String xgboostperhour = "/home/ubuntu/phd_data/code/cloudsimThermal-master/output/thermal/fastStorage_thermalMinPeakTemp_mmt_200peakTemperature.csv";
        String ThermalMinRR = "/home/ubuntu/phd_data/code/cloudsimThermal-master/output/thermal/fastStorage_thermalMinRR_mmt_200peakTemperature.csv";

        String filePath = xgboostperhour;
        Scanner dataReaderObj = new Scanner(new FileReader(filePath));

        int numberOfEntry = 0;
        double [] hourData = new double[24];
        double[] secData = new double[288];

        while (dataReaderObj.hasNextLine()) {
            String input = dataReaderObj.nextLine();
            String[] attr = null;
            attr = input.split(",");
            if (input != null && attr.length == 2) {

                secData[numberOfEntry+1] = Double.parseDouble(attr[1]);
//                System.out.println(secData[numberOfEntry+1]);
//
                numberOfEntry++;
            }
        }
//        System.out.println("Total " + numberOfEntry);

        convertToHour(secData);
    }
    private static void   convertToHour(double[] secData){

        System.out.println("Hourdata------------");
        double [] hourdata = new double[24];
        int start =0; int end = 0;
        for (int i =0 ; i< 24; i++){
            double sum =0;
//            these intervals for 5 minute scheduling interval- > 1 hour = 12 records
//            start = i*12;
//            end = i*12+12;

            //  these intervals for 10 minute scheduling interval- > 1 hour = 6 records
            start = i*6;
            end = i*6+6;
            for (int j = start; j< end; j++){
                sum+= secData[j];
            }

            hourdata[i] = Math.ceil(sum/6);
            //System.out.print(hourdata[i] + " ");
            double data = hourdata[i];
            System.out.print(data + " ");
        }


    }
}
