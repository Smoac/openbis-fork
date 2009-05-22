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

package ch.systemsx.cisd.yeastx.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

/**
 * Factory for database connections.
 *
 * @author Bernd Rinn
 */
public class DBFactory
{
    static
    {
        QueryTool.getTypeMap().put(float[].class, new FloatArrayMapper());
    }

    public static Connection getConnection() throws SQLException
    {
        try
        {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex)
        {
            throw new SQLException("No suitable driver.");
        }
        final Connection conn =
            DriverManager.getConnection("jdbc:postgresql:metabol", System.getProperties()
                    .getProperty("user.name"), "");
        conn.setAutoCommit(false);
        return conn;
    }

}
