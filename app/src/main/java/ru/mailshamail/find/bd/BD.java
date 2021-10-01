package ru.mailshamail.find.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

public class BD {

    private Connection connection;

    public BD(){}

    public Connection Connect()  {

         try {

             Class.forName("org.postgresql.Driver");
             connection = DriverManager.getConnection("jdbc:postgresql://home.nixub.ru:32169/coord", "mailshamail", "EfdJKe45sd43afsd");

             System.out.println("Connected");

         }
         catch (Exception e){System.out.println(e);}

         return connection;
    }

    public void addToTable(String name, double  shirota, double dolgota, String date) throws SQLException
    {

        PreparedStatement statement2 = connection.prepareStatement("INSERT INTO info (name, shirota, dolgota, time)\n" +
                "VALUES(?, ?, ?, ?)\n" +
                "ON CONFLICT (name) \n" +
                "DO\n" +
                " UPDATE\n" +
                "   SET shirota ="+shirota + ", dolgota="+dolgota + ", time=" + date +";");


        statement2.setString(1, name);
        statement2.setDouble(2, shirota);
        statement2.setDouble(3, dolgota);
        statement2.setString(4, date);
        statement2.executeUpdate();
        System.out.println("add");
    }

    public ArrayList<String> getNameInBD() throws SQLException {
       ArrayList<String> name = new ArrayList<>();

        Statement statement = connection.createStatement();



        ResultSet resultSet = statement.executeQuery("SELECT * FROM info");
        while (resultSet.next())
        {
            name.add(resultSet.getString("name"));
        }
        //System.out.println("name");
        return name;
    }

    public ArrayList<Double> getShirotaInBD() throws SQLException {
        ArrayList<Double> shirota = new ArrayList<>();

        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM info");
        while (resultSet.next())
        {
            shirota.add(resultSet.getDouble("shirota"));
        }
        //System.out.println("shirota");
        return shirota;
    }

    public ArrayList<Double> getDolgotaInBD() throws SQLException {
        ArrayList<Double> dolgota = new ArrayList<>();

        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM info");
        while (resultSet.next())
        {
            dolgota.add(resultSet.getDouble("dolgota"));
        }
       // System.out.println("dplgota");
        return dolgota;
    }

    public int getCounts() throws SQLException {
        int count=0;

        Statement statement = connection.createStatement();

        ResultSet cou = statement.executeQuery("select count(*) FROM info");
        while(cou.next())
        {
            count = cou.getInt(1);
            System.out.println(count + " : COUNT");
        }

        return count;
    }

    public ArrayList<String> getTimeUpdate() throws SQLException {
        ArrayList<String> time = new ArrayList<>();

        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM info");
        while (resultSet.next())
        {
            time.add(resultSet.getString("time"));
        }
        // System.out.println("dplgota");
        return time;
    }




    public Connection getConnection() {
        return connection;
    }
}
