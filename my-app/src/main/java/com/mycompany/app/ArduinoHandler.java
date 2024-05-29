package com.mycompany.app;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ArduinoHandler {
    static String ip = "";
    static String port = "80";

    static void setIP(String newIP) {
        ip = newIP;
    }

    static double[] getSensorData() {
        double[] data = new double[4];
        String url_string = "http://" + ip + ":" + port + "/getSensorData";

        try {
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            // Wenn die Anfrage erfolgreich war (Code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // BufferedReader zum Lesen der Antwort erstellen
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                
                // Antwortzeilen lesen
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                
                // BufferedReader schließen
                in.close();
                
                // Antwort ausgeben
                System.out.println("Response: " + response.toString());
                String[] splitResponse = response.toString().split(";");

                for (int i = 0; i < splitResponse.length; i++) {
                    data[i] = Double.parseDouble(splitResponse[i]);
                }

                
            } else {
                System.out.println("GET request failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    static boolean setDeviceState(String device, String state) {
        String url_string = "http://" + ip + ":" + port + "/set" + device + state;

        try {
            URL url = new URL(url_string);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = httpURLConnection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            // Wenn die Anfrage erfolgreich war (Code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                System.out.println("POST request failed for " + device + state);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
