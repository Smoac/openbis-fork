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
 * A trigger for creating and setting a new quota_group if none is provided.
 * 
 * @author Bernd Rinn
 */
public class UpdateQuotaGroupIdOnInsertUpdateUserTrigger extends AbstractUserTrigger
{

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final Long quotaGroupId = (Long) newRow[quotaGroupIdIndex];
        if (quotaGroupId == null)
        {
            final ResultSet result =
                    conn.createStatement().executeQuery("SELECT NEXTVAL('QUOTA_GROUP_ID_SEQ')");
            result.first();
            final long newQuotaGroupId = result.getLong(1);
            newRow[quotaGroupIdIndex] = newQuotaGroupId;
            conn.createStatement().execute(
                    "INSERT INTO QUOTA_GROUPS (ID) VALUES (" + newQuotaGroupId + ")");
        }
    }

}
