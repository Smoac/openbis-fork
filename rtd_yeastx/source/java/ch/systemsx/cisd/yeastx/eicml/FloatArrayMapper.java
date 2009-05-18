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

package ch.systemsx.cisd.yeastx.eicml;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.lemnik.eodsql.TypeMapper;

import org.apache.commons.lang.StringUtils;

/**
 * {@link TypeMapper} for <code>float[]</var> columns.
 * 
 * @author Bernd Rinn
 */
class FloatArrayMapper implements TypeMapper<float[]>
{

    public float[] get(ResultSet results, int column) throws SQLException
    {
        final String[] floatStr = StringUtils.split(results.getString(column), ',');
        final float[] floatArr = new float[floatStr.length];
        for (int i = 0; i < floatStr.length; ++i)
        {
            floatArr[i] = Float.parseFloat(floatStr[i]);
        }
        return floatArr;
    }

    public void set(ResultSet results, int column, float[] obj) throws SQLException
    {
        results.updateString(column, toString(obj));
    }

    public void set(PreparedStatement statement, int column, float[] obj) throws SQLException
    {
        if (obj != null)
        {
            statement.setString(column, toString(obj));
        } else
        {
            statement.setNull(column, Types.VARCHAR);
        }
    }

    private String toString(float[] array)
    {
        StringBuilder b = new StringBuilder();
        for (float v : array)
        {
            b.append(v);
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}