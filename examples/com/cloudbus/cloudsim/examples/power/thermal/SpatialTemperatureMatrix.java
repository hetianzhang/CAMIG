package com.cloudbus.cloudsim.examples.power.thermal;
import org.cloudbus.cloudsim.Log;

import java.util.Scanner;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class SpatialTemperatureMatrix {




    public  SpatialTemperatureMatrix(){

        }

    public static  double[][] getSpatialTemperatureMatrix() throws IOException{



        String filePath = getFilePath();

        double [][] dMatrix = new double[50][50];
        Scanner dataReaderObj = new Scanner(new FileReader(filePath));


        int numberOfEntry = 0;

        while(dataReaderObj.hasNextLine())
        {
            String input = dataReaderObj.nextLine();
            String[] attr = null;
            if (input != null ) {
                attr = input.split(" ");
            }
            if (attr != null && attr.length == 50 ) {

                for (int i = 0; i < 50 ; i++){
                    dMatrix[numberOfEntry][i]= Double.parseDouble(attr[i]);
                }

                numberOfEntry++;
            }

        }
        Log.printLine("Total Points " + dMatrix.length + " oth element" + dMatrix[48][49] );
       /* for ( int i =0 ; i < 50 ;i++)
            for (int j=0; j < 50; j++){
                Log.printLine(String.format("@SSTM i-> %d j-> %d D[%d][%d]-> %.2f", i, j, i,j, dMatrix[i][j] ));
                Log.printLine("@SSTM" + " i " + " j " +  dMatrix[i][j] );
            }*/


        return  dMatrix;

    }


    public static String getFilePath(){

        return ThermalConstants.SPATIAL_TEMPERATURE_MATRIX_FILE_PATH;

    }
}
