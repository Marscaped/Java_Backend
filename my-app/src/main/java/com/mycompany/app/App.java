package com.mycompany.app;
import static spark.Spark.*;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class App 
{
    static Scanner scanner = new Scanner(System.in);

    static Thread sensorUpdateThread = new Thread() {
        public void run() {
            while (true) {
                getLatestSensorDataHandlerAutomatic();

                try {
                    TimeUnit.MINUTES.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static void main( String[] args )
    {   
        System.out.println("-= Welcome to the Marscaped-Backend =-");
        System.out.println("Enter IP-Adress of MARS-Arduino:");
        ArduinoHandler.setIP(scanner.nextLine());

        System.out.println( "--- Listening on Port 4567 ---" );

        // Starting Automatic Sensor Update Thread
        // 2 Minute Update
        sensorUpdateThread.start();
        System.out.println( "[INFORMATION] Started automatic sensor update "  + "[" + LocalDateTime.now() + "]");
        

        // HTTP-Request Paths
        get("/helloworld", (req, res) -> "So... you found the Hello World link... Congratulations");
        get("/getLatestSensorData", (req, res) -> getLatestSensorDataHandler());
        get("/changeDeviceState", (req, res) -> {
            String device = req.queryParams("device");
            String newState = req.queryParams("state");
            String username = req.queryParams("user");
            
            return setDeviceState(device, newState, username);
        });        
        get("/authorizeUser", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            return SQLHandler.AuthenticateUser(new User(username, password));
        });
    }

    static String getLatestSensorDataHandler() {
        logHTTPRequest("getLatestSensorDataHandler");
        String data = SQLHandler.getLatestData();
        //SQLHandler.sendSensorDataToDatabase(data);
        return data;
        //return Double.toString(data[0]) + ";" + Double.toString(data[1]) + ";" + Double.toString(data[2]) + ";" + Double.toString(data[3]); 
    }

    static void getLatestSensorDataHandlerAutomatic() {
        logHTTPRequest("getLatestSensorDataHandler [Automatic]");
        double[] data = ArduinoHandler.getSensorData();
        
        if(data[0] != 0 && data[1] != 0 && data[2] != 0 && data[3] != 0) {
            SQLHandler.sendSensorDataToDatabase(data);
        }
    }

    static boolean setDeviceState(String device, String state, String username) {
        logHTTPRequest("setDeviceState for " + device);
        boolean response = ArduinoHandler.setDeviceState(device, state);

        if (response) {
            SQLHandler.sendDeviceChangeToDatabase(device, state, username);
        }

        return response;
    }

    static void logHTTPRequest(String request) {
        System.out.println("[INFORMATION] " + request + " has been called " + "[" + LocalDateTime.now() + "]");
    }
}
