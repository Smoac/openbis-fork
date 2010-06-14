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

import java.sql.SQLException;
import java.sql.Types;

/**
 * A simple implementation of {@link java.sql.Array} for <code>byte[]</code> which only supports
 * {@link #getArray()} for retrieval.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SimpleSQLByteArray extends AbstractSQLArray
{

    private final byte[] value;

    SimpleSQLByteArray(byte[] array)
    {
        value = array;
    }

    SimpleSQLByteArray(Byte[] array)
    {
        value = new byte[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            value[i] = array[i];
        }
    }

    /**
     * Get the object array.
     * 
     * @return the object array
     */
    public Object getArray()
    {
        return value;
    }

    /**
     * Get the base type of this array.
     * 
     * @return TINYINT
     */
    public int getBaseType()
    {
        return Types.TINYINT;
    }

    /**
     * Get the base type name of this array.
     * 
     * @return "bit8"
     */
    public String getBaseTypeName()
    {
        return "bit8";
    }

    @Override
    public void free() throws SQLException
    {
        // do nothing
    }

    @Override
    /*
     * * For the PostgreSQL JDBC driver to work with this class, this method needs to return
     * '{x,y,z,...}'.
     */
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (byte v : value)
        {
            builder.append(Byte.toString(v));
            builder.append(',');
        }

        // Remove the trailing comma
        if (value.length > 0)
        {
            builder.setLength(builder.length() - 1);
        }
        builder.append('}');
        return builder.toString();
    }

}
