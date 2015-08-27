package ch.ethz.bsse.cisd.dsu.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DbAccess
{

    private static final String FASTQ_GZ_SUFFIX = ".fastq.gz";

    /**
     * Connecting to the pathifno_DB
     * @return Connection
     */
    static Connection connectToDB(Parameters params)
    {
        try
        {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(
                    params.getPathinfoDBConnectionString(), params.getPathinfoDBUser(), params.getPathinfoDBPassword());
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 
     * @return HashMap<String, Integer> of files and the corresponding checksum for the DB
     * Uses a file as key and the checksum as value, we assume that a file is unique
     */
    static HashMap<String, Integer> doQuery(Connection connection, String permId)
    {
        HashMap<String, Integer> dataSetResult = new HashMap<String, Integer>();
        Statement st = null;
        try
        {
            st = connection.createStatement();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        ResultSet rs = null;
        try
        {
            System.out.println("Found data set with permID: " + permId);
            rs = st.executeQuery("select dsf.id, dsf.file_name, dsf.checksum_crc32 from data_sets ds,"
                    + " data_set_files dsf where ds.code='"
                    + permId + "' and ds.id =dsf.dase_id and dsf.is_directory = FALSE;");
            while (rs.next())
            {
                Integer id = rs.getInt("id");
                String fileName = rs.getString("file_name");
                Integer checksum = rs.getInt("checksum_crc32");

                if (fileName.endsWith(FASTQ_GZ_SUFFIX))
                {
                    dataSetResult.put(fileName, checksum);
                }
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        try
        {
            rs.close();
            st.close();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return dataSetResult;
    }

    static void closeDBConnection(Connection connection)
    {
        try
        {
            connection.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }
}
