package com.cloudbus.cloudsim.examples.power.thermal.inference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.net.InetSocketAddress;
import java.io.OutputStreamWriter;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class RESTAPICall {

    public static void main(String[] args) {

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

//            String userpass = "ubuntu" + ":" + "amma6298";
//            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes("UTF-8"));
//            conn.setRequestProperty ("Authorization", basicAuth);

//            String data =  "{\"format\":\"json\",\"pattern\":\"#\"}";
            String data = "{\"MSE\": \"mse\", \"RMSE\": \"rmse\", \"Accuracy\": \"accuracy\"}";
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data);
            out.close();

            System.out.println("Request Sent, Result: ");

            InputStreamReader response =  new InputStreamReader(conn.getInputStream());
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(response);
            System.out.println("JsonObejct: " + jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
