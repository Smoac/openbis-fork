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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * An abstract super class that implements {@link Array}. All methods throw an
 * {@link UnsupportedOperationException}.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractSQLArray implements Array
{

    AbstractSQLArray()
    {
        // Call from sub-class.
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object getArray(Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object getArray(long index, int count) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public ResultSet getResultSet() throws SQLException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public ResultSet getResultSet(long index, int count) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map)
            throws SQLException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
}
