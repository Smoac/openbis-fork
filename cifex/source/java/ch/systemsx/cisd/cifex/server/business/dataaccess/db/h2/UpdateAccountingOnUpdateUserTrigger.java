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

import ch.systemsx.cisd.common.db.SQLStateUtils;

/**
 * Trigger for updating the accounting information when a user changes the quota group.
 * 
 * @author Bernd Rinn
 */
public class UpdateAccountingOnUpdateUserTrigger extends AbstractUserTrigger
{

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final String newUserCode = (String) newRow[userCodeIndex];
        // Simulate NOT NULL constraint on USERS.QUOTA_GROUP_ID.
        // Implementation note: H2 checks constraints before calling the BEFORE ROW triggers,
        // thus we can't use the normal constraint mechanism or else we would get a constraint
        // violation on USERS.QUOTA_GROUP_ID not being set when inserting a user.
        if (newRow[quotaGroupIdIndex] == null)
        {
            throw new SQLException("Quota group of user " + newUserCode
                    + " must not be set to NULL", SQLStateUtils.NULL_VALUE_VIOLATION);
        }
        final Long oldQuotaGroupId = (Long) oldRow[quotaGroupIdIndex];
        final Long newQuotaGroupId = (Long) newRow[quotaGroupIdIndex];
        if (oldQuotaGroupId != newQuotaGroupId)
        {
            if (oldQuotaGroupId == null) // Happens during migration
            {
                conn.createStatement().execute(
                        "SELECT CALC_ACCOUNTING_FOR_QUOTA_GROUPS(" + newQuotaGroupId + ")");
            } else
            {
                conn.createStatement().execute(
                        "SELECT CALC_ACCOUNTING_FOR_QUOTA_GROUPS(" + oldQuotaGroupId + ","
                                + newQuotaGroupId + ")");
            }
        }
    }

}
