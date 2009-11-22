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
 * Abstract super class of the triggers on the USERS table for the H2 database.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractUserTrigger implements Trigger
{
    private final String USER_CODE_COLUMN_NAME = "USER_CODE";

    private final String OLD_USER_CODE_COLUMN_NAME = "USER_ID";

    private final String QUOTA_GROUP_ID_COLUMN_NAME = "QUOTA_GROUP_ID";

    protected int userCodeIndex = -1;

    protected int quotaGroupIdIndex = -1;

    public void init(Connection conn, String schemaName, String triggerName, String tableName,
            boolean before, int type) throws SQLException
    {
        final ResultSet columns = conn.getMetaData().getColumns(null, schemaName, tableName, null);
        int idx = 0;
        while (columns.next())
        {
            final String columnName = columns.getString("COLUMN_NAME");
            if (QUOTA_GROUP_ID_COLUMN_NAME.equals(columnName))
            {
                quotaGroupIdIndex = idx;
            } else if (USER_CODE_COLUMN_NAME.equals(columnName)
                    || OLD_USER_CODE_COLUMN_NAME.equals(columnName))
            {
                userCodeIndex = idx;
            }
            ++idx;
        }
    }

}
