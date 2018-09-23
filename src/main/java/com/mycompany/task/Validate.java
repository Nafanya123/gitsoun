package com.mycompany.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class Validate {
    String kpp;
    public Validate(String kpp)
    {
        this.kpp = kpp;
        if((kpp.length() - 1) != 8 || !kpp.matches("[0-9]+")) throw new IllegalStateException("КПП может состоять только из 9 цифр");
    }
    
    public void lep()
    {
        String l = kpp.substring(0, 4);
        Connection c = null;
        Statement stmt = null;
        
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hdd", "postgres", "root");
            
            c.setAutoCommit(false);
            System.out.println("Соединение установлено");
            String sql;
            stmt = c.createStatement();
            sql = "SELECT * FROM erm_soun WHERE code = '" + l +"'";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next())
            {
                String code = rs.getString("code");
                String shortname = rs.getString("shortname");
                String fullname = rs.getString("fullname");
                System.out.println(code + " " + shortname + " " + fullname);
            }
        }catch(Exception e)
        {
           e.printStackTrace();
           System.out.println(e.getClass().getName() +":" + e.getMessage());
           System.exit(0);
        }
    }
}
