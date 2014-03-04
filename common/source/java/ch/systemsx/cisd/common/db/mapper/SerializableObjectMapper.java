/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.lemnik.eodsql.TypeMapper;

import org.apache.commons.lang.SerializationUtils;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SerializableObjectMapper implements TypeMapper<Serializable>
{

    @Override
    public Serializable get(ResultSet results, int column) throws SQLException
    {
        return (Serializable) SerializationUtils.deserialize(results.getBytes(column));
    }

    @Override
    public void set(ResultSet results, int column, Serializable obj) throws SQLException
    {
        results.updateBytes(column, SerializationUtils.serialize(obj));
    }

    @Override
    public void set(PreparedStatement statement, int column, Serializable obj) throws SQLException
    {
        if (obj != null)
        {
            statement.setBytes(column, SerializationUtils.serialize(obj));
        } else
        {
            statement.setNull(column, Types.BINARY);
        }
    }

}
