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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.lemnik.eodsql.TypeMapper;

/**
 * A class for mapping <code>byte[]</code> to {@link java.sql.Array}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ByteArrayMapper implements TypeMapper<byte[]>
{
    public byte[] get(ResultSet results, int column) throws SQLException
    {
        return (byte[]) results.getArray(column).getArray();
    }

    public void set(PreparedStatement statement, int column, byte[] obj) throws SQLException
    {
        if (obj != null)
        {
            statement.setArray(column, new SimpleSQLByteArray(obj));
        } else
        {
            statement.setNull(column, Types.ARRAY);
        }
    }

    public void set(ResultSet results, int column, byte[] obj) throws SQLException
    {
        results.updateArray(column, new SimpleSQLByteArray(obj));
    }

}
