/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.api.Trigger;

/**
 * Abstract super class of the triggers on the FILES table for the H2 database.
 *
 * @author Bernd Rinn
 */
abstract class AbstractFileTrigger implements Trigger
{
    private final String USER_ID_COLUMN_NAME = "USER_ID";
    
    private final String COMPLETE_SIZE_COLUMN_NAME = "COMPLETE_SIZE";
    
    protected int userIdIndex = -1;

    protected int completeSizeIndex = -1;

    public void init(Connection conn, String schemaName, String triggerName, String tableName,
            boolean before, int type) throws SQLException
    {
        final ResultSet columns = conn.getMetaData().getColumns(null, schemaName, tableName, null);
        int idx = 0;
        while (columns.next())
        {
            final String columnName = columns.getString("COLUMN_NAME");
            if (USER_ID_COLUMN_NAME.equals(columnName))
            {
                userIdIndex = idx;
            } else if (COMPLETE_SIZE_COLUMN_NAME.equals(columnName))
            {
                completeSizeIndex = idx;
            }
            ++idx;
        }
    }

}
