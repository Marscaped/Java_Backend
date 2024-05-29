package com.mycompany.app;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
//import java.sql.Driver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

//import org.mariadb.jdbc.Driver;

public class SQLHandler {
    // JDBC URL, Benutzername und Passwort der MariaDB-Datenbank
    static String jdbcUrl = "jdbc:mariadb://127.0.0.1:3306/projekta";
    static String username = "root";
    static String password = "admin";

    static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";

    static Connection connection = null;
    static Statement statement = null;
    static ResultSet resultSet = null;

    //Class.forName("org.mariadb.jdbc.Driver");

    static void logSQLRequest(String sql) {
        App.logHTTPRequest("SQL Request [" + sql + "]");
    }

    static boolean AuthenticateUser(User user) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            //System.out.println("Verbindung erfolgreich!");

            // Statement-Objekt erstellen
            statement = connection.createStatement();

            // SQL-Abfrage ausführen
            //String sql = "INSERT INTO Sensor VALUES(" + data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + LocalDateTime.now() + ");";
            String sql = "SELECT password from User WHERE UserName = '" + user.username + "'";
            resultSet = statement.executeQuery(sql);
            logSQLRequest(sql);

            String password = "";
            while (resultSet.next()) {
                password = resultSet.getString("password");
            }

            if (password.equals(user.password)) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // Ressourcen schließen
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }

    static void sendSensorDataToDatabase(double[] data) {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(jdbcUrl + "?user=" + username + "&password=" + password);
            //System.out.println("Verbindung erfolgreich!");

            // Statement-Objekt erstellen
            statement = connection.createStatement();

            // SQL-Abfrage ausführen
            // DB Aufbau: ID(Auto) TimeStamp Temp Moist Humid Light
            // Data Aufbau: Light Temp moist humid
            LocalDateTime timeNow = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String time = timeNow.format(formatter);

            String sql = "INSERT INTO Sensor" + 
            " VALUES (null" + "," + '"' + time + '"' + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[0] + ");";
            
            statement.execute(sql);

            logSQLRequest(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ressourcen schließen
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String getLatestData() {
        

        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            //System.out.println("Verbindung erfolgreich!");

            // Statement-Objekt erstellen
            statement = connection.createStatement();

            // SQL-Abfrage ausführen
            String sql = "SELECT * from Sensor ORDER BY SID DESC LIMIT 1";
            resultSet = statement.executeQuery(sql);

            logSQLRequest(sql);


            Date dt = new Date(1970, 1, 1);
            Time time = new Time(0);
            double temp = 0;
            double moist = 0;
            double humid = 0;
            double light = 0;
            // Ergebnisse anzeigen
            while (resultSet.next()) {
                 dt = resultSet.getDate("datetime");
                 time = resultSet.getTime("datetime");
                 temp = resultSet.getDouble("temp");
                 moist = resultSet.getDouble("moist");
                 humid = resultSet.getDouble("humid");
                 light = resultSet.getDouble("light");
                //System.out.println("ID: " + id + ", Name: " + temp);
            }

            return dt + " " + time + ";" + temp + ";" + moist + ";" + humid + ";" + light;

            //return (resultSet.getDate("datetime") + ";" + resultSet.getDouble("temp") + ";" + resultSet.getDouble("moist") + ";" + resultSet.getDouble("humid") + ";" + resultSet.getDouble("light"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ressourcen schließen
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    static void sendDeviceChangeToDatabase(String device, String newState, String user) {

        try {
            // Verbindung zur Datenbank herstellen
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            //System.out.println("Verbindung erfolgreich!");

            // Statement-Objekt erstellen
            statement = connection.createStatement();

            // SQL-Abfrage ausführen
            String sql = "SELECT * from Components WHERE compname = '" + device + "'";
            resultSet = statement.executeQuery(sql);
            logSQLRequest(sql);

            int did = 0;
            while (resultSet.next()) {
                did = resultSet.getInt("CID");
            }

            sql = "SELECT * from User WHERE UserName = '" + user + "'";
            resultSet = statement.executeQuery(sql);
            logSQLRequest(sql);

            int uid = 0;
            while (resultSet.next()) {
                uid = resultSet.getInt("UID");
            }

            LocalDateTime timeNow = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String time = timeNow.format(formatter);

            sql = "INSERT INTO Comp_User VALUES (" + '"' + time + '"' + "," + uid + "," + did + "," + '"' + newState + '"' + ");";
            //System.out.println(sql);
            statement.execute(sql);
            logSQLRequest(sql);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ressourcen schließen
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
