/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.path;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.Test;

/**
 * This is a minimal test for {@link IPathsInfoDAO} which ensures that the query annotations can be
 * parsed correctly.
 * 
 * @author Bernd Rinn
 */
public class IPathsInfoDAOTest
{
    
    private final DataSource DUMMY_DATA_SOURCE = new DataSource()
    {
        @Override
        public Connection getConnection() throws SQLException
        {
            return null;
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException
        {
            return null;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException
        {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException
        {
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException
        {
        }

        @Override
        public int getLoginTimeout() throws SQLException
        {
            return 0;
        }
        
        @Override
        public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException
        {
            return null;
        }
        
        @Override
        public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException
        {
            return false;
        }
    };
    
    @Test
    public void testGetQuery()
    {
        QueryTool.getQuery(DUMMY_DATA_SOURCE, IPathsInfoDAO.class);
    }

}
