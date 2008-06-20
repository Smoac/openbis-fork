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

package ch.systemsx.cisd.common.db;

import java.sql.SQLException;

import ch.systemsx.cisd.common.utilities.ExceptionUtils;

/**
 * Some utility methods regarding <i>SQL State</i>.
 * <p>
 * Note: be careful not to duplicate functionality with the <i>Spring</i> framework. Have a look at
 * at <code>org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator</code> and
 * <code>org.springframework.util.StringUtils.SQLErrorCodes</code> before extending the
 * functionality of this class.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class SQLStateUtils
{
    /** SQL State. */
    public static final String FOREIGN_KEY_VIOLATION = "23503";

    /** SQL State. */
    public static final String UNIQUE_VIOLATION = "23505";

    /** SQL State. */
    public static final String INVALID_CATALOG_NAME = "3D000";

    /** SQL State. */
    public static final String DUPLICATE_OBJECT = "42710";

    /** SQL State. */
    public static final String DUPLICATE_DATABASE = "42P04";

    private SQLStateUtils()
    {
        // This class can not be instantiated.
    }

    /**
     * Tries to get the SQL state of given <code>Throwable</code>.
     * <p>
     * This is only possible if {@link Throwable#getCause()} is an instance of
     * <code>SQLException</code>.
     * </p>
     */
    public final static String getSqlState(final Throwable ex)
    {
        SQLException sqlException = ExceptionUtils.tryGetThrowableOfClass(ex, SQLException.class);
        String sqlState = null;
        if (sqlException != null)
        {
            sqlState = sqlException.getSQLState();
            final SQLException cause = sqlException.getNextException();
            while (sqlState == null && cause != null)
            {
                sqlState = cause.getSQLState();
            }
        }
        return sqlState;
    }

    /** Whether given SQL state stands for <i>DUPLICATE OBJECT</i>. */
    public final static boolean isDuplicateObject(final String sqlState)
    {
        return DUPLICATE_OBJECT.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>DUPLICATE DATABASE</i>. */
    public final static boolean isDuplicateDatabase(final String sqlState)
    {
        return DUPLICATE_DATABASE.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>INVALID CATALOG NAME</i>. */
    public final static boolean isInvalidCatalogName(final String sqlState)
    {
        return INVALID_CATALOG_NAME.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>UNIQUE VIOLATION</i>. */
    public final static boolean isUniqueViolation(final String sqlState)
    {
        return UNIQUE_VIOLATION.equalsIgnoreCase(sqlState);
    }

    /** Whether given SQL state stands for <i>FOREIGN KEY VIOLATION</i>. */
    public final static boolean isForeignKeyViolation(final String sqlState)
    {
        return FOREIGN_KEY_VIOLATION.equalsIgnoreCase(sqlState);
    }

}
