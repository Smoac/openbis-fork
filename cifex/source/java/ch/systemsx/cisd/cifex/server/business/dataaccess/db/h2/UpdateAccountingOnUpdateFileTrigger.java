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
 * Trigger for updating accounting information for the quota group when updating a file.
 * 
 * @author Bernd Rinn
 */
public class UpdateAccountingOnUpdateFileTrigger extends AbstractFileTrigger
{

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final long oldUserId = (Long) oldRow[userIdIndex];
        final long newUserId = (Long) newRow[userIdIndex];
        final long oldCompleteFileSize = (Long) oldRow[completeSizeIndex];
        final long newCompleteFileSize = (Long) newRow[completeSizeIndex];
        final boolean userChanged = (oldUserId != newUserId);
        final boolean sizeChanged = (oldCompleteFileSize != newCompleteFileSize);
        final boolean quotaGroupChanged;
        final long newQuotaGroupId;
        final long oldQuotaGroupId;

        if (userChanged || sizeChanged)
        {
            ResultSet result =
                    conn.createStatement().executeQuery(
                            "SELECT Q.ID FROM QUOTA_GROUPS Q JOIN USERS U "
                                    + "ON Q.ID = U.QUOTA_GROUP_ID WHERE U.ID = " + newUserId);
            result.first();
            newQuotaGroupId = result.getLong(1);
            if (userChanged)
            {
                result =
                        conn.createStatement().executeQuery(
                                "SELECT Q.ID FROM QUOTA_GROUPS Q JOIN USERS U "
                                        + "ON Q.ID = U.QUOTA_GROUP_ID WHERE U.ID = " + oldUserId);
                result.first();
                oldQuotaGroupId = result.getLong(1);
                quotaGroupChanged = (oldQuotaGroupId != newQuotaGroupId);
            } else
            {
                oldQuotaGroupId = 0L;
                quotaGroupChanged = false;
            }
        } else
        {
            oldQuotaGroupId = 0L;
            newQuotaGroupId = 0L;
            quotaGroupChanged = false;
        }
        if (quotaGroupChanged)
        {
            conn.createStatement().execute(
                    "UPDATE QUOTA_GROUPS SET FILE_COUNT = FILE_COUNT - 1,"
                            + " FILE_SIZE = FILE_SIZE - " + oldCompleteFileSize + " WHERE ID = "
                            + oldQuotaGroupId);
            conn.createStatement().execute(
                    "UPDATE QUOTA_GROUPS SET FILE_COUNT = FILE_COUNT + 1,"
                            + " FILE_SIZE = FILE_SIZE + " + newCompleteFileSize + " WHERE ID = "
                            + newQuotaGroupId);
        } else if (sizeChanged)
        {
            conn.createStatement().execute(
                    String.format("UPDATE QUOTA_GROUPS "
                            + "SET FILE_SIZE = FILE_SIZE - %d + %d WHERE ID = %d",
                            oldCompleteFileSize, newCompleteFileSize, newQuotaGroupId));
        }
    }

}
