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

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.db.SQLStateUtils;

/**
 * Utility database methods.
 *
 * @author Franz-Josef Elmer
 */
public class DBUtilities
{
    /**
     * Checks whether given <code>DataAccessException</code> is caused by a "database does not exist" exception.
     * <p>
     * This is database specific.
     * </p>
     */
    public static boolean isDBNotExistException(DataAccessException ex)
    {
        // 3D000: INVALID CATALOG NAME
        return SQLStateUtils.isInvalidCatalogName(SQLStateUtils.getSqlState(ex));
    }

    /**
     * Checks whether given <code>DataAccessException</code> is caused by a "duplicate object" exception.
     * <p>
     * This is database specific.
     * </p>
     */
    public static boolean isDuplicateObjectException(DataAccessException ex) {
        // 42710 DUPLICATE OBJECT
        return SQLStateUtils.isDuplicateObject(SQLStateUtils.getSqlState(ex));
    }
    
    private DBUtilities() {}
    
}
