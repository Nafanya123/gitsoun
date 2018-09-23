package com.mycompany.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveInputStream;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;

public class Stream {
    private static final String INPUT_ZIP_FILE = "C:\\java_projects\\SOUN_DBF.ARJ";
    private static final String OUTPUT_FOLDER = "C:\\java_projects\\UNZIPPED_file";
    
    private static final String dbf_f = "C:\\java_projects\\SOUN1.dbf";
    
    //Временная функция
    public static void main( String[] args ) throws IOException, ArchiveException, Exception
    {
        Validate vl = new Validate("000026235");
        vl.lep();
        //vl.lep();
        //DBF_FF(dbf_f);
        //unArj(INPUT_ZIP_FILE,OUTPUT_FOLDER);
    }
    
    public static void DBF_FF(String dbf_file)
    {
        
        Connection c = null;
        Statement stmt;
        
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hdd", "postgres", "root");
            
            c.setAutoCommit(false);
            System.out.println("Соединение установлено");
            String sql;
            
            DBF dbf = new DBF(dbf_file);
            
            CharField code = (CharField) dbf.getField("KOD");
            CharField shortname = (CharField) dbf.getField("NAIMK");
            CharField fullname = (CharField) dbf.getField("NAIM");
            int i = 0;
            for(i = 1; i < dbf.getRecordCount(); i++)
            {
                dbf.setEncodingType("CP866");
                dbf.read();
                stmt = c.createStatement();
                sql = "INSERT INTO erm_soun (code, shortname, fullname) VALUES ('"+ code.get() +"','"+ shortname.get() +"', '"+ fullname.get() +"')";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
            }
            System.out.println("Успешно, кол-во записей: " + i);
            
        }catch(Exception e)
        {
           e.printStackTrace();
           System.out.println(e.getClass().getName() +":" + e.getMessage());
           System.exit(0);
        }
    }
    public static void unArj(String arjFile, String outt) throws ArchiveException, Exception {

        try (ArjArchiveInputStream stream = new ArjArchiveInputStream(new FileInputStream(arjFile))) {
            System.out.println(stream.toString());
            ArjArchiveEntry entry;
            String name;
            while ((entry = stream.getNextEntry()) != null) {
                name = entry.getName();
                final File archiveEntry = new File(outt, name);
                archiveEntry.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    archiveEntry.mkdir();
                }
                int tmp;
                OutputStream out = new FileOutputStream(archiveEntry);
                while ((tmp = stream.read()) != -1)
                {
                    out.write((char) tmp);
                }
            }
            stream.close();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
}
}
