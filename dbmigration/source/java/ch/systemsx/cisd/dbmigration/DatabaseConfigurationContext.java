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
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Configuration context for database operations.
 * 
 * @author Franz-Josef Elmer
 */
public class DatabaseConfigurationContext
{
    private String driver;

    private LobHandler lobHandler;

    private String adminURL;

    private String adminUser;

    private String adminPassword;

    private String urlTemplate;

    private String scriptFolder;

    private String folderOfDataScripts;

    private String databaseKind;

    private boolean createFromScratch;

    private DataSource dataSource;
    
    private DataSource adminDataSource;

    private String owner;

    private String basicDatabaseName;

    private String databaseName;
    
    public DatabaseConfigurationContext()
    {
        owner = System.getProperty("user.name");
    }

    /**
     * Creates a <code>DataSource</code> for this context.
     */
    private final DataSource createDataSource()
    {
        final BasicDataSource myDataSource = new BasicDataSource();
        myDataSource.setDriverClassName(getDriver());
        final String url = MessageFormat.format(getUrlTemplate(), getDatabaseName());
        myDataSource.setUrl(url);
        myDataSource.setUsername(owner);
        myDataSource.setPassword("");
        return myDataSource;
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
     * Returns data source for admin purposes.
     */
    final DataSource getAdminDataSource()
    {
        if (adminDataSource == null)
        {
            BasicDataSource myDataSource = new BasicDataSource();
            myDataSource.setDriverClassName(getDriver());
            myDataSource.setUrl(getAdminURL());
            myDataSource.setUsername(getAdminUser());
            myDataSource.setPassword(getAdminPassword());
            adminDataSource = myDataSource;
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
     * Returns user name of the administrator.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getAdminUser()
    {
        return adminUser;
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
     * Returns the basic name of the database. The kind of database will be added to this to create the full database
     * name.
     */
    public String getBasicDatabaseName()
    {
        return basicDatabaseName;
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
     * Returns password of the administrator.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getAdminPassword()
    {
        return adminPassword;
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
     * Returns the fully-qualified class name of the JDBC driver.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getDriver()
    {
        return driver;
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
     * Returns the template to created the URL of the database to be created/migrated. It should contain
     * <code>{0}</code> as a placeholder for the name of the database.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getUrlTemplate()
    {
        return urlTemplate;
    }

    /**
     * Sets the template to created the URL of the database to be created/migrated.
     * 
     * @param urlTemplate New value. Can be <code>null</code>.
     * @see #getUrlTemplate()
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
     * Returns database kind.
     * 
     * @return <code>null</code> when undefined.
     */
    public final String getDatabaseKind()
    {
        return databaseKind;
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
     * @param scriptFolderOfDataScripts New value. Can be <code>null</code>.
     */
    public final void setFolderOfDataScripts(String scriptFolderOfDataScripts)
    {
        this.folderOfDataScripts = scriptFolderOfDataScripts;
    }

}
