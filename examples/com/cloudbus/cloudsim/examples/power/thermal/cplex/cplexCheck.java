package com.cloudbus.cloudsim.examples.power.thermal.cplex;
import  java.lang.Math;



import java.io.IOException;

public class cplexCheck {

    public static void main(String[] args) throws IOException {


        double SCHEDULING_INTERVAL = 300;
        double SIMULATION_LIMIT = 24*60*2; // 4  15 = 6 hrs, 30 = 12 hrs    24 * 60 * 60; = 24 hrs


         int NUMBER_OF_DATA_SAMPLES = (int) Math.ceil( SIMULATION_LIMIT / SCHEDULING_INTERVAL)+1; //  289;
        System.out.println("DSata "  +NUMBER_OF_DATA_SAMPLES);


    }
}
