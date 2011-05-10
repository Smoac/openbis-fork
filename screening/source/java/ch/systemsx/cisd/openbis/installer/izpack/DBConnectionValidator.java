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

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

/**
 * @author Kaloyan Enimanev
 */
public class DBConnectionValidator implements DataValidator
{

    private static final String DEFAULT_ERROR_MESSAGE = "Cannot connect to the specified database.";

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
        String hostname = data.getVariable("DB.HOST");
        String port = data.getVariable("DB.PORT");
        String username = data.getVariable("DB.USERNAME");
        String password = data.getVariable("DB.PASSWORD");
        String adminUsername = data.getVariable("DB.ADMIN.USERNAME");
        String adminPassword = data.getVariable("DB.ADMIN.PASSWORD");
        String dbname = data.getVariable("DB.NAME");

        String connectionString = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbname;

        if (testConnectionOK(connectionString, username, password))
        {
            if (testConnectionOK(connectionString, adminUsername, adminPassword))
            {
                return Status.OK;
            }
        }
        return Status.ERROR;
    }

    private boolean testConnectionOK(String connectionString,
            String username, String password)
    {
        boolean connected = false;
        try
        {
            Class.forName("org.postgresql.Driver");
            Connection connection =
                    DriverManager.getConnection(connectionString, username, password);
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
