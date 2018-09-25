package com.mycompany.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.xBaseJ.DBF;
import org.xBaseJ.fields.CharField;

public class Stream {
    private static final String INPUT = "C:\\java_projects\\SOUN_DBF.ARJ";
    private static final String OUTPUT = "C:\\java_projects\\UNZIPPED_file";
    
    //Временная функция
    public static void main( String[] args ) throws IOException, Exception
    {
       //Validate vl = new Validate("000026235");
        //vl.lep();
        //DBF_FF(dbf_f);
        unARJ(INPUT,OUTPUT);
    }
    
    public static int DBF_read(String dbf_file)
    {
        Connection c = null;
        Statement stmt;
        int i = 0;
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/hdd", "postgres", "root");
            
            c.setAutoCommit(false);
            String sql;
            
            DBF dbf = new DBF(dbf_file);
            
            CharField code = (CharField) dbf.getField("KOD");
            CharField shortname = (CharField) dbf.getField("NAIMK");
            CharField fullname = (CharField) dbf.getField("NAIM");
            
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
            dbf.close();
        }catch(Exception e)
        {
           e.printStackTrace();
           System.out.println(e.getClass().getName() +":" + e.getMessage());
           System.exit(0);
        }
        return i;
    }
    public static void unARJ(String arjFile, final String outt) throws InterruptedException
    {
        try {
            //Инициализация собственной библиотеки
            SevenZip.initSevenZipFromPlatformJAR();

            RandomAccessFile randomAccessFile = new RandomAccessFile(arjFile, "r");
            IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                if (!item.isFolder()) {
                    final File archiveEntry = new File(outt, item.getPath());
                    archiveEntry.getParentFile().mkdirs();
                    final OutputStream out = new FileOutputStream(archiveEntry);
                    item.extractSlow(new ISequentialOutStream() {
                        public int write(byte[] data) throws SevenZipException {
                            for (byte b : data) {
                                try {
                                    //Наполнение фаила полученными данными с архива
                                    out.write((char) b);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            return data.length;
                        }
                    });
                }
            }
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SevenZipException e) {
            e.printStackTrace();
        }
        DBF_read(outt + "\\SOUN1.dbf");
        Delete(new File(outt));      
    }

    // Рекурсивное удаление
    private static void Delete(File file)
    {
        if(!file.exists())
        {
            return;
        }

        if(file.isDirectory())
        {
            for(File f : file.listFiles())
            {
                Delete(f);
            }
        }
        file.delete();
    }
}
