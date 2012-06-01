package ch.systemsx.cisd.dbmigration.migration;

import javax.sql.DataSource;

import ch.systemsx.cisd.dbmigration.IDataSourceFactory;

public class SqlMigrationDataSourceFactory implements IDataSourceFactory
{

    @Override
    public DataSource createDataSource(String driver, String url, String owner, String password)
    {
        return new SqlMigrationDataSource(driver, url, owner, password);
    }

    @Override
    public void setMaxActive(int maxActive)
    {
    }

    @Override
    public void setMaxIdle(int maxIdle)
    {
    }

    @Override
    public void setMaxWait(long maxWait)
    {
    }

}