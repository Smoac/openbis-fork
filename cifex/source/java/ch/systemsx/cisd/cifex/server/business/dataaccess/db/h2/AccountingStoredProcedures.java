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
 * A class implementing stored procedures to re-calculate the accounting information for all or some
 * quota groups for the H2 database.
 * 
 * @author Bernd Rinn
 */
public class AccountingStoredProcedures
{

    /**
     * Re-calculate the accounting information for all quota groups.
     */
    public static void calcAccountingForAllQuotaGroups(final Connection conn) throws SQLException
    {
        final boolean startNewTransaction = conn.getAutoCommit();
        if (startNewTransaction)
        {
            conn.setAutoCommit(false);
        }
        conn.createStatement().execute(
                "UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (SELECT COUNT(*) "
                        + "FROM FILES F JOIN USERS U ON F.USER_ID = U.ID "
                        + "WHERE U.QUOTA_GROUP_ID = Q.ID)");
        conn.createStatement().execute(
                "UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) "
                        + "FROM FILES F JOIN USERS U ON F.USER_ID = U.ID "
                        + "WHERE U.QUOTA_GROUP_ID = Q.ID)");
        if (startNewTransaction)
        {
            conn.commit();
        }
    }

    /**
     * Re-calculate the accounting information for the given quota groups.
     */
    public static void calcAccountingForQuotaGroups(final Connection conn, long... quotaGroupIds)
            throws SQLException
    {
        final boolean startNewTransaction = conn.getAutoCommit();
        if (startNewTransaction)
        {
            conn.setAutoCommit(false);
        }
        for (long quotaGroupId : quotaGroupIds)
        {
            conn.createStatement().execute(
                    "UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (SELECT COUNT(*) "
                            + "FROM FILES F JOIN USERS U ON F.USER_ID = U.ID "
                            + "WHERE U.QUOTA_GROUP_ID = Q.ID) WHERE Q.ID = " + quotaGroupId);
            conn.createStatement().execute(
                    "UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) "
                            + "FROM FILES F JOIN USERS U ON F.USER_ID = U.ID "
                            + "WHERE U.QUOTA_GROUP_ID = Q.ID) WHERE Q.ID = " + quotaGroupId);
        }
        if (startNewTransaction)
        {
            conn.commit();
        }
    }

}
