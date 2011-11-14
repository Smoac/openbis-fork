package ch.systemsx.cisd.dbmigration.migration;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.IDataSourceFactory;

public class SqlMigrationDataSourceFactory implements IDataSourceFactory
{

    public DataSource createDataSource(String driver, String url, String owner, String password)
    {
        return new SqlMigrationDataSource(driver, url, owner, password);
    }

    public void setMaxActive(int maxActive)
    {
    }

    public void setMaxIdle(int maxIdle)
    {
    }

    public void setMaxWait(long maxWait)
    {
    }

}