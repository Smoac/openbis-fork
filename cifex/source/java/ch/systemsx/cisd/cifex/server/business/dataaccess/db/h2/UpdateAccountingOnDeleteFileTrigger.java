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
import java.sql.SQLException;

/**
 * A trigger for updating accounting for the quota group when deleting a file.
 * 
 * @author Bernd Rinn
 */
public final class UpdateAccountingOnDeleteFileTrigger extends AbstractFileTrigger
{

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final long userId = (Long) oldRow[userIdIndex];
        final long completeFileSize = (Long) oldRow[completeSizeIndex];
        conn.createStatement()
                .execute(
                        "UPDATE QUOTA_GROUPS SET FILE_COUNT = FILE_COUNT - 1,"
                                + " FILE_SIZE = FILE_SIZE - " + completeFileSize
                                + " WHERE ID = (SELECT QUOTA_GROUP_ID FROM USERS WHERE ID = "
                                + userId + ")");
    }

}
