/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.dbmigration;

import java.text.MessageFormat;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.support.lob.LobHandler;

import ch.systemsx.cisd.common.db.ISequencerHandler;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Configuration context for database operations.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseConfigurationContext implements DisposableBean
{
    private static final class BasicDataSourceFactory implements IDataSourceFactory
    {
        public DataSource createDataSource(String driver, String url, String owner, String password)
        {
            final BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driver);
            dataSource.setUrl(url);
            dataSource.setUsername(owner);
            dataSource.setPassword(password);
            dataSource.setMinIdle(0);
            dataSource.setMaxIdle(0);
            return dataSource;
        }
    }

    private String driver;

    private LobHandler lobHandler;

    private ISequencerHandler sequencerHandler;

    private String adminURL;

    private String adminUser;

    private String adminPassword;

    private String urlTemplate;

    private String scriptFolder;

    private String folderOfDataScripts;

    private String folderOfMassUploadFiles;

    private String databaseKind;

    private String databaseType;

    private boolean createFromScratch;

    private boolean scriptSingleStepMode;

    private IDataSourceFactory dataSourceFactory = new BasicDataSourceFactory();

    private DataSource dataSource;

    private DataSource adminDataSource;

    private String owner;

    private String basicDatabaseName;

    private String databaseName;

    public DatabaseConfigurationContext()
    {
        owner = System.getProperty("user.name").toLowerCase();
    }

    public final void setDataSourceFactory(IDataSourceFactory dataSourceFactory)
    {
        this.dataSourceFactory = dataSourceFactory;
    }

    private final static void closeConnection(final DataSource dataSource)
    {
        if (dataSource != null)
        {
            try
            {
                if (dataSource instanceof BasicDataSource)
                {
                    (((BasicDataSource) dataSource)).close();
                }
                if (dataSource instanceof DisposableBean)
                {
                    ((DisposableBean) dataSource).destroy();
                }
            } catch (final Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    /**
     * Creates a <code>DataSource</code> for this context.
     */
    private final DataSource createDataSource()
    {
        final String dsDriver = getDriver();
        final String dsUrlTemplate = getUrlTemplate();
        final String dsDatabaseName = getDatabaseName();
        if (dsDatabaseName == null)
        {
            throw new ConfigurationFailureException("No db name defined.");
        }
        final String url = MessageFormat.format(dsUrlTemplate, dsDatabaseName);
        return dataSourceFactory.createDataSource(dsDriver, url, owner, "");
    }

    /**
     * Returns the template to created the URL of the database to be created/migrated. It should contain
     * <code>{0}</code> as a placeholder for the name of the database.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getUrlTemplate()
    {
        if (urlTemplate == null)
        {
            throw new ConfigurationFailureException("No db url template defined.");
        }
        return urlTemplate;
    }

    /**
     * Returns the fully-qualified class name of the JDBC driver.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getDriver()
    {
        if (driver == null)
        {
            throw new ConfigurationFailureException("No db driver defined.");
        }
        return driver;
    }

    /**
     * Returns user name of the administrator.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getAdminUser()
    {
        return adminUser;
    }

    /**
     * Returns password of the administrator.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getAdminPassword()
    {
        return adminPassword;
    }

    /**
     * Returns database kind.
     * 
     * @return <code>null</code> when undefined.
     */
    private final String getDatabaseKind()
    {
        return databaseKind;
    }

    /**
     * Returns the {@link DataSource} of this data configuration.
     */
    public final DataSource getDataSource()
    {
        if (dataSource == null)
        {
            dataSource = createDataSource();
        }
        return dataSource;
    }

    /**
     * Returns the basic name of the database. The kind of database will be added to this to create the full database
     * name.
     */
    public String getBasicDatabaseName()
    {
        return basicDatabaseName;
    }

    /**
     * Returns data source for admin purposes.
     */
    public final DataSource getAdminDataSource()
    {
        if (adminDataSource == null)
        {
            adminDataSource =
                    dataSourceFactory.createDataSource(getDriver(), getAdminURL(), getAdminUser(), getAdminPassword());
        }
        return adminDataSource;
    }

    /**
     * Returns the user name of the owner of the database.
     */
    public final String getOwner()
    {
        return owner;
    }

    /**
     * Sets the user name of the onwer of the database.
     */
    public final void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     * Sets user name of the administrator.
     * 
     * @param adminUser New value. Can be <code>null</code>.
     */
    public final void setAdminUser(String adminUser)
    {
        this.adminUser = adminUser;
    }

    /**
     * Sets the basic name of the database. The kind of database will be added to this to create the full database name.
     * 
     * @param basicDatabaseName The basic name of the database. Must not be <code>null</code>.
     */
    public void setBasicDatabaseName(String basicDatabaseName)
    {
        this.basicDatabaseName = basicDatabaseName;
    }

    public String getDatabaseName()
    {
        if (databaseName == null)
        {
            databaseName = getBasicDatabaseName() + "_" + getDatabaseKind();
        }
        return databaseName;
    }

    /**
     * Sets password of the administrator.
     * 
     * @param adminPassword New value. Can be <code>null</code>.
     */
    public final void setAdminPassword(String adminPassword)
    {
        this.adminPassword = adminPassword;
    }

    /**
     * Returns the URL of the database server which allows to create a new database.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getAdminURL()
    {
        return adminURL;
    }

    /**
     * Sets the URL of the database server which allows to create a new database.
     * 
     * @param adminURL New value. Can be <code>null</code>.
     */
    public final void setAdminURL(String adminURL)
    {
        this.adminURL = adminURL;
    }

    /**
     * Sets the fully-qualified class name of the JDBC driver.
     * 
     * @param driver New value. Can be <code>null</code>.
     */
    public final void setDriver(String driver)
    {
        this.driver = driver;
    }

    /**
     * Returns lobHandler.
     * 
     * @return <code>null</code> when undefined.
     */
    public final LobHandler getLobHandler()
    {
        return lobHandler;
    }

    /**
     * Sets lobHandler.
     * 
     * @param lobHandler New value. Can be <code>null</code>.
     */
    public final void setLobHandler(LobHandler lobHandler)
    {
        this.lobHandler = lobHandler;
    }

    /**
     * Returns <code>sequencerHandler</code>.
     * 
     * @return <code>null</code> when undefined.
     */
    public final ISequencerHandler getSequencerHandler()
    {
        return sequencerHandler;
    }

    /**
     * Sets <code>sequencerHandler</code>.
     * 
     * @param sequencerHandler New value. Can be <code>null</code>.
     */
    public final void setSequencerHandler(ISequencerHandler sequencerHandler)
    {
        this.sequencerHandler = sequencerHandler;
    }

    /**
     * Sets the template to created the URL of the database to be created/migrated.
     * 
     * @param urlTemplate New value. Can be <code>null</code>.
     */
    public final void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    /**
     * Returns <code>true</code> if the current database should be dropped and (re)created from scratch.
     * 
     * @return <code>false</code> when the database should only be migrated if necessary.
     */
    public final boolean isCreateFromScratch()
    {
        return createFromScratch;
    }

    /**
     * Sets the database should be dropped and (re)created from scratch or not.
     */
    public final void setCreateFromScratch(boolean createFromScratch)
    {
        this.createFromScratch = createFromScratch;
    }

    /**
     * Returns <code>true</code> if scripts in the db migration engine should be executed statement by statement. This
     * mode gives better error messages on where the faulty SQL is but on the other hand it is a lot slower.
     */
    public final boolean isScriptSingleStepMode()
    {
        return scriptSingleStepMode;
    }

    /**
     * Sets the db migration engine to single step mode for scripts.
     */
    public final void setScriptSingleStepMode(boolean singleStepMode)
    {
        this.scriptSingleStepMode = singleStepMode;
    }

    /**
     * Sets database kind. This will be append to the name of the database. It allows to have different database
     * instances in parallel (for developing, testing, etc.).
     * 
     * @param databaseKind New value. Can be <code>null</code>.
     */
    public final void setDatabaseKind(String databaseKind)
    {
        this.databaseKind = databaseKind;
    }

    /**
     * Returns databaseType.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getDatabaseType()
    {
        return databaseType;
    }

    /**
     * Sets databaseType.
     * 
     * @param databaseType New value. Can be <code>null</code>.
     */
    public final void setDatabaseType(String databaseType)
    {
        this.databaseType = databaseType;
    }

    /**
     * Returns the folder which contains all SQL scripts.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getScriptFolder()
    {
        return scriptFolder;
    }

    /**
     * Sets the folder which contains all SQL scripts.
     * 
     * @param scriptFolder New value. Can be <code>null</code>.
     */
    public final void setScriptFolder(String scriptFolder)
    {
        this.scriptFolder = scriptFolder;
    }

    /**
     * Returns the folder which contains all Data SQL scripts. As a default value {@link #getScriptFolder()} will be
     * returned if not definied by a non-<code>null</code> value in {@link #setFolderOfDataScripts(String)}.
     * 
     * @return <code>null</code> when {@link #getScriptFolder()} returns <code>null</code>.
     */
    public final String getFolderOfDataScripts()
    {
        return folderOfDataScripts == null ? getScriptFolder() : folderOfDataScripts;
    }

    /**
     * Sets the folder which contains all Data SQL scripts.
     * 
     * @param folderOfDataScripts New value. Can be <code>null</code>.
     */
    public final void setFolderOfDataScripts(String folderOfDataScripts)
    {
        this.folderOfDataScripts = folderOfDataScripts;
    }

    /**
     * Returns the folder which contains the files for mass upload to the database. As a default value
     * {@link #getFolderOfDataScripts()} will be returned if not definied by a non-<code>null</code> value in
     * {@link #setFolderOfMassUploadFiles(String)}.
     */
    public String getFolderOfMassUploadFiles()
    {
        return folderOfMassUploadFiles == null ? getFolderOfDataScripts() : folderOfMassUploadFiles;
    }

    /**
     * Sets the folder which contains the files for mass uploads.
     * 
     * @param folderOfMassUploadFiles New value. Can be <code>null</code>. An empty value will be interpreted as
     *            <code>null</code>.
     */
    public void setFolderOfMassUploadFiles(String folderOfMassUploadFiles)
    {
        if ("".equals(folderOfMassUploadFiles))
        {
            this.folderOfMassUploadFiles = null;
        } else
        {
            this.folderOfMassUploadFiles = folderOfMassUploadFiles;
        }
    }

    /** Closes opened database connections. */
    public final void closeConnections()
    {
        closeConnection(dataSource);
        dataSource = null;
        closeConnection(adminDataSource);
        adminDataSource = null;
    }

    //
    // DisposableBean
    //

    public final void destroy() throws Exception
    {
        closeConnections();
    }

}
