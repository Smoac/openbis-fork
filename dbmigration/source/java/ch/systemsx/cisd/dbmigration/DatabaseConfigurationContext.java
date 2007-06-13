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


/**
 * Configuration context for database operations.
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseConfigurationContext
{
    private String driver;
    
    private String adminURL;
    
    private String adminUser;
    
    private String adminPassword;
    
    private String urlTemplate;
    
    private String scriptFolder;
    
    private String folderOfDataScripts;
    
    private String databaseKind;
    
    private boolean createFromScratch;

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
     * Returns the template to created the URL of the database to be created/migrated. It should
     * contain <code>{0}</code> as a placeholder for the name of the database.
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
     * Sets database kind. This will be append to the name of the database. It allows to have different
     * database instances in parallel (for developing, testing, etc.).
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
     * Returns the folder which contains all Data SQL scripts. As a default value {@link #getScriptFolder()}
     * will be returned if not definied by a non-<code>null</code> value in 
     * {@link #setFolderOfDataScripts(String)}. 
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
