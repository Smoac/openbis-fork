/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.installer.DataValidator;

/**
 * Tests if there is a valid PostgreSQL installation on the local machine that is setup to accept
 * connections from local users without requiring a password.
 * 
 * @author Kaloyan Enimanev
 */
public class DBConnectionValidator implements DataValidator
{

    private static final String DEFAULT_ERROR_MESSAGE = "Cannot connect to the specified database.";

    private static final String JDBC_DRIVER_NAME = "org.postgresql.Driver";

    private static final String CONNECTION_STRING = "jdbc:postgresql://localhost/template1";

    private static final String POSTGRES_USER = "postgres";

    private static final String NO_PASSWORD = "";

    private String errorMessage;

    public boolean getDefaultAnswer()
    {
        return true;
    }

    public String getErrorMessageId()
    {
        if (errorMessage != null)
        {
            return errorMessage;
        } else
        {
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    public String getWarningMessageId()
    {
        return getErrorMessageId();
    }

    public Status validateData(AutomatedInstallData data)
    {
        if (testConnectionOK(POSTGRES_USER, NO_PASSWORD))
        {
                return Status.OK;
        }
        return Status.ERROR;
    }

    private boolean testConnectionOK(String username, String password)
    {
        boolean connected = false;
        try
        {
            Class.forName(JDBC_DRIVER_NAME);
            Connection connection =
                    DriverManager.getConnection(CONNECTION_STRING, username, password);
            if (connection != null)
            {
                connected = true;
                connection.close();
            }
        } catch (ClassNotFoundException cnfe)
        {
            errorMessage = cnfe.getMessage();
        } catch (SQLException e)
        {
            errorMessage = e.getMessage();
        }

        return connected;
    }
}
