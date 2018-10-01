package com.mycompany.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class Validate {
    String kpp;

    /**
    * Конструктор - создание нового объекта с определенными значениями
    * @param kpp - код КПП, состоящий из 9-ти цифр
    **/
    public Validate(String kpp)
    {
        this.kpp = kpp;
        if((kpp.length() - 1) != 8 || !kpp.matches("[0-9]+")) throw new IllegalStateException("КПП может состоять только из 9 цифр");
    }
    
    /**
    * Функция поиска по первым четырем цифрам переданные
    * из конструктора в бд, по полю code
    * @param url - ссылка на бд
    * @param user - имя пользователя бд
    * @param pass - пароль бд
    **/
    public void found(String url, String user, String pass)
    {
        String l = kpp.substring(0, 4);
        Connection c = null;
        Statement stmt;
        
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(url, user, pass);
            c.setAutoCommit(false);
            String sql;
            stmt = c.createStatement();
            sql = "SELECT * FROM erm_soun WHERE code = '" + l +"'";
            ResultSet rs =  stmt.executeQuery(sql);
            
            while(rs.next())
            {
                String code = rs.getString("code");
                String shortname = rs.getString("shortname");
                String fullname = rs.getString("fullname");
                System.out.println(code + " " + shortname + " " + fullname);
            }
            rs.close();
        }catch(Exception e)
        {
           e.printStackTrace();
           System.out.println(e.getClass().getName() +":" + e.getMessage());
           System.exit(0);
        }
    }
}
