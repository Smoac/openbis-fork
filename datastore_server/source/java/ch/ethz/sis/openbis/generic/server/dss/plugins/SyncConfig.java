/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.lang.reflect.Field;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
class SyncConfig
{
    private String dataSourceURI;

    public String getDataSourceURI()
    {
        return dataSourceURI;
    }

    public void setDataSourceURI(String dataSourceURI)
    {
        this.dataSourceURI = dataSourceURI;
    }

    public String getDataSourceOpenbisURL()
    {
        return dataSourceOpenbisURL;
    }

    public void setDataSourceOpenbisURL(String dataSourceOpenbisURL)
    {
        this.dataSourceOpenbisURL = dataSourceOpenbisURL;
    }

    public String getDataSourceDSSURL()
    {
        return dataSourceDSSURL;
    }

    public void setDataSourceDSSURL(String dataSourceDSSURL)
    {
        this.dataSourceDSSURL = dataSourceDSSURL;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass(String pass)
    {
        this.pass = pass;
    }

    public String getLastSyncTimestampFileName()
    {
        return lastSyncTimestampFileName;
    }

    public void setLastSyncTimestampFileName(String lastSyncTimestampFileName)
    {
        this.lastSyncTimestampFileName = lastSyncTimestampFileName;
    }

    public String getDataSourcePrefix()
    {
        return dataSourcePrefix;
    }

    public void setDataSourcePrefix(String dataSourcePrefix)
    {
        this.dataSourcePrefix = dataSourcePrefix;
    }

    public String getDataSourceSpaces()
    {
        return dataSourceSpaces;
    }

    public void setDataSourceSpaces(String dataSourceSpaces)
    {
        this.dataSourceSpaces = dataSourceSpaces;
    }

    public String getHarvesterSpaces()
    {
        return harvesterSpaces;
    }

    public void setHarvesterSpaces(String harvesterSpaces)
    {
        this.harvesterSpaces = harvesterSpaces;
    }

    public String getHarvesterTempDir()
    {
        return harvesterTempDir;
    }

    public void setHarvesterTempDir(String harvesterTempDir)
    {
        this.harvesterTempDir = harvesterTempDir;
    }

    public void printConfig()
    {
        for (Field field : this.getClass().getDeclaredFields())
        {
            field.setAccessible(true);
            String name = field.getName();
            Object value;
            try
            {
                value = field.get(this);
                System.out.printf("%s : %s%n", name, value);
            } catch (IllegalArgumentException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String dataSourceOpenbisURL;

    private String dataSourceDSSURL;

    private String realm;

    private String user;

    private String pass;

    private String lastSyncTimestampFileName;

    private String dataSourcePrefix;

    private String dataSourceSpaces;

    private String harvesterSpaces;

    private String harvesterTempDir;

    private String emailAddresses;

    private String logFilePath;

    public String getLogFilePath()
    {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath)
    {
        this.logFilePath = logFilePath;
    }

    public String getEmailAddresses()
    {
        return emailAddresses;
    }

    public void setEmailAddresses(String emailAddresses)
    {
        this.emailAddresses = emailAddresses;
    }
}
