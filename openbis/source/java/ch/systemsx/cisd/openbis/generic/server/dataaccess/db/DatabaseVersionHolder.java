/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

/**
 * A static class which holds the database version.
 * 
 * @author Christian Ribeaud
 */
public final class DatabaseVersionHolder
{
    /** Current version of the database. */
    private static final String DATABASE_VERSION = "034";

    private DatabaseVersionHolder()
    {
        // Can not be instantiated
    }

    /** Returns the current version of the database. */
    public final static String getDatabaseVersion()
    {
        return DATABASE_VERSION;
    }
}