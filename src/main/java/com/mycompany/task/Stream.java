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
import java.sql.SQLException;
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
    
    static String url = null;
    static String user = null;
    static String pass = null;
    
    /**
     * Конструктор - создание нового объекта с определенными значениями
     * @param url - ссылка на бд
     * @param user - имя пользователя бд
     * @param pass - пароль бд
     */
    public Stream(String url, String user, String pass)
    {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }
    
    /**
     * Функция записывающая в бд данные из фаила с расширением dbf
     * @param dbf_file - ссылка на фаил
     * @return возвращает кол-во добавленных в бд записей
     */
    private int DBF_read(String dbf_file)
    {
        Connection c = null;
        Statement stmt;
        int i = 0;
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(url, user, pass);
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
    
    /**
     * Функция выполняет декомпрессию ARJ архива, а так же высов методов
     * DBF_read и Delete
     * @param arjFile - ссылка на ARJ архив
     * @param outt - ссылка, где будет произведена декомпрессия
     * После декомпрессии происходит считывание фаила в бд, после чего все фаилы удаляются.
     */
    public void unARJ(String arjFile, final String outt)
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
    /**
     * Функция рекурсивного удаления
     * @param file - параметр передаваемого фаила
     */
    private void Delete(File file)
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
