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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.InvalidDataTypeException;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;

/**
 * Database utilities.
 * 
 * @author Bernd Rinn
 */
public class DBUtils
{

    /**
     * Use this method instead of {@link QueryTool#getQuery(DataSource, Class)}. Only in this way
     * you make sure that the rigth query mappers will be registered.
     */
    public static <T extends BaseQuery> T getQuery(final DataSource dataSource, final Class<T> query)
            throws InvalidDataTypeException, InvalidQueryException
    {
        return QueryTool.getQuery(dataSource, query);
    }

    /**
     * Rolls backs and closes the given <var>transactionOrNull</var>, if it is not <code>null</code>
     * .
     */
    public static void rollbackAndClose(TransactionQuery transactionOrNull)
    {
        if (transactionOrNull != null)
        {
            transactionOrNull.rollback();
            transactionOrNull.close();
        }
    }

    /**
     * Closes the given <var>transactionOrNull</var>, if it is not <code>null</code> .
     */
    public static void close(TransactionQuery transactionOrNull)
    {
        if (transactionOrNull != null)
        {
            transactionOrNull.close();
        }
    }

}
