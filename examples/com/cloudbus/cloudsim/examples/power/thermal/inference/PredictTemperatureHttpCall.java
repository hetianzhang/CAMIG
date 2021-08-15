package com.cloudbus.cloudsim.examples.power.thermal.inference;

import org.apache.commons.math3.analysis.function.Power;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.DoubleAccumulator;

public class PredictTemperatureHttpCall {



    public PredictTemperatureHttpCall(){
    }
    // params, add ram, n/w,  number of running VMs etc..
    public double getPredictedTemperature(double hostId, double cpuLoad, double ramUsed, double networkUsage, double cpuPower, double numberofVms,
                                          double  numberofCPUCoresUsed) {
        String outputData = null;
        double temperature =0;
        String inputData = null;

        try {
//

//            curl -d '{"MSE": "mse", "RMSE": "rmse", "Accuracy": "accuracy"}' -H "Content-Type: application/json" \
//            -X POST http://localhost:8000/predict && \
//            echo -e "\n -> predict OK"
            String url = "http://localhost:8000/predict";

            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            conn.setRequestMethod("POST"); // PUT

//            String userpass = "ubuntu" + ":" + "pass";
//            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes("UTF-8"));
//            conn.setRequestProperty ("Authorization", basicAuth);

//            String data =  "{\"format\":\"json\",\"pattern\":\"#\"}";
//            String inputData = makeJsonInput(host.getId(), cpuUtilization,  hostPower, networkRX, networkTX);

            //  The host Id in data prediction model ranges from 120-195; Hence converting to map them
            hostId = hostId + 120;
            // CPU Load in trained model data is measured in 0-100 % . The current data is between 0-1. Converting it to 0-100 range
            cpuLoad = cpuLoad*100;

            //Create Json Input String based on feature names in data
            JSONObject json = new JSONObject();
            Integer Id = Double.valueOf(hostId).intValue(); // trim the decimal value of double variable
            json.put("Id", Id);
            json.put("CPU_Load", cpuLoad);
            json.put("Power", cpuPower);
            json.put("Network_RX",networkUsage/2); // We assume Rx and Tx are equally distributed among total,
            // we have added together. TODO, introduce cloudlet with rx an tx/
            json.put("Network_TX",networkUsage/2);
            json.put("Ram_Used", ramUsed);
            json.put("No_Of_Running_vms", numberofVms);
            json.put("CPU_cores_used", numberofCPUCoresUsed);

            inputData = json.toString();




            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());

            System.out.println("@" + PredictTemperatureHttpCall.class.getName() + " Sending Request- JsonObject: " + inputData );
            out.write(inputData);
            out.close();



            InputStreamReader response =  new InputStreamReader(conn.getInputStream());
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(response);
            outputData = jsonObject.toString();

            System.out.println( "@" + PredictTemperatureHttpCall.class.getName() +" Response1111: " + outputData );
//            temperature = (double)jsonObject.get("Temperature");
            temperature = Double.parseDouble((String)jsonObject.get("Temperature"));

            System.out.println( "@" + PredictTemperatureHttpCall.class.getName() +" Response222: " + temperature );


        } catch (Exception e) {
            e.printStackTrace();
        }

            System.out.println(String.format("@" + PredictTemperatureHttpCall.class.getName() + " Predicted Temperature: %s  ", temperature));
        if (temperature < 35 && temperature > 105){
            Log.printLine("Incorrect Prediction: Clock: "  + CloudSim.clock() + " RequestObject: " + inputData + " Response: " + outputData );

        }
        return  temperature;


   }




//    public static void main(String[] args) throws IOException {
//
//        PredictTemperatureHttpCall pd;
////        PowerHost host = new PowerHost();
//        pd = new PredictTemperatureHttpCall( );
//        double hostId, double cpuLoad, double ramUsed, double networkUsage, double cpuPower, double numberofVms,
//        double  numberofCPUCoresUsed
//        String temp=  pd.getPredictedTemperature(0.2327287449,0.4086668416, 0.0129276299,0.0026676073,
//            0.7189942888},"Fan_speed2":{"174":0.7176539648},"Fan_speed3":{"174":0.7149297985},
//            #  "Fan_speed4":{"174":0.7129823856},"Ram_Used":{"174":0.6834138599},"No_Of_Running_vms":{"174":0.7727272727},"CPU_cores_used":{"174":0.6336633663);
//
//        System.out.println(String.format("Response Temperature: %s", temp));
//
//
//    }

}
