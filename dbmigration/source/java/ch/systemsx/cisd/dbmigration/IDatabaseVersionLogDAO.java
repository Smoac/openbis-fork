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
 * Interface for a Data Access Object for the database version log.
 *
 * @author Franz-Josef Elmer
 */
public interface IDatabaseVersionLogDAO
{
    /**
     * Returns <code>true</code> if the database instance with database version log can be connected.
     */
    public boolean canConnectToDatabase();
    
    /**
     * Creates the table DATABASE_VERSION_LOG.
     */
    public void createTable();
    
    /**
     * Returns the last log entry found.
     * 
     * @return <code>null</code> if log is empty.
     */
    public LogEntry getLastEntry();
    
    /**
     * Inserts a new entry into the version log with {@link LogEntry.RunStatus#START}. 
     * 
     * @param version Version of the database after creation/migration.
     * @param moduleName Name of the module/script to be applied. 
     * @param moduleCode Script code. 
     */
    public void logStart(String version, String moduleName, String moduleCode);
    
    /**
     * Update log entry specified by version and module name to {@link LogEntry.RunStatus#SUCCESS}. 
     * 
     * @param version Version of the database after creation/migration.
     * @param moduleName Name of the successfully applied module/script. 
     */
    public void logSuccess(String version, String moduleName);
    
    /**
     * Update log entry specified by version and module name to {@link LogEntry.RunStatus#FAILED}. 
     * 
     * @param version Version of the database after creation/migration.
     * @param moduleName Name of the failed module/script.
     * @param runException Exception causing the failure. 
     */
    public void logFailure(String version, String moduleName, Throwable runException);

}
