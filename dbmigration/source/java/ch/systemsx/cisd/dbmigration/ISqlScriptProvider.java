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

import java.io.File;

import ch.systemsx.cisd.common.Script;

/**
 * Provider of SQL scripts for creation and migration of database.
 * 
 * @author Franz-Josef Elmer
 */
public interface ISqlScriptProvider
{
    /**
     * Returns <code>true</code> if this script provider is suitable for a dump restore of the given <var>version</var>
     * of the database, and <code>false</code>, if it is suitable for a regular setup.
     */
    public boolean isDumpRestore(String version);

    /**
     * Returns the folder where all dump files for <var>version</var> reside.
     */
    public File getDumpFolder(String version);
    
    /**
     * Returns the script to create database schemas.
     * 
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetSchemaScript(String version);

    /**
     * Returns the script to create the database functions.
     * 
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetFunctionScript(String version);

    /**
     * Returns the script to create initial data.
     * 
     * @param version Version of the database.
     * @return <code>null</code> if there isn't such a script.
     */
    public Script tryGetDataScript(String version);

    /**
     * Returns the migration script for migrating a database.
     * 
     * @param fromVersion The version of the current database.
     * @param toVersion The version of the database after migration.
     * @return <code>null</code> if there isn't such a migration script.
     */
    public Script tryGetMigrationScript(String fromVersion, String toVersion);

}
