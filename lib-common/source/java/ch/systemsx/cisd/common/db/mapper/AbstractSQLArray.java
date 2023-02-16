/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.db.mapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * An abstract super class that implements {@link Array}. All methods throw an {@link UnsupportedOperationException}.
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
    @Override
    public Object getArray(Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public Object getArray(long index, int count) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ResultSet getResultSet() throws SQLException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ResultSet getResultSet(long index, int count) throws SQLException,
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map)
            throws SQLException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void free() throws SQLException
    {
        throw new UnsupportedOperationException();
    }
}
