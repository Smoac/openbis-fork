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

/**
 * A trigger for deleting a quota group when its last user is deleted.
 * 
 * @author Bernd Rinn
 */
public class DeleteQuotaGroupOnDeleteUserTrigger extends AbstractUserTrigger
{

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final long quotaGroupId = (Long) oldRow[quotaGroupIdIndex];
        final ResultSet result = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM USERS WHERE QUOTA_GROUP_ID = " + quotaGroupId);
        result.first();
        final long remainingUsersInGroup = result.getLong(1);
        if (remainingUsersInGroup == 0)
        {
            conn.createStatement().execute("DELETE FROM QUOTA_GROUPS WHERE ID = " + quotaGroupId);
        }
    }

}
