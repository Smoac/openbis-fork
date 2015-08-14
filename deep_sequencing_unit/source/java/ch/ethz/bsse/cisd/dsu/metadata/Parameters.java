/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.bsse.cisd.dsu.metadata;

import static ch.systemsx.cisd.common.properties.PropertyUtils.getMandatoryProperty;

import java.util.Properties;

/**
 * @author Manuel Kohler
 */
public class Parameters
{
    private static final String OPENBIS_USER = "openbis-user";

    private static final String OPENBIS_PASSWORD = "openbis-password";

    private static final String OPENBIS_SERVER_URL = "openbis-server-url";

    private static final String PATHINFO_DB_CONNECTION_STRING = "pathinfo-db-connection-string";

    private static final String PATHINFO_DB_USER = "pathinfo-db-user";

    private static final String PATHINFO_DB_CONNECTION_PASSWORD = "pathinfo-db-password";

    private final String openbisUser;

    private final String openbisPassword;

    private final String openbisServerURL;

    private final String pathinfoDBConnectionString;

    private final String pathinfoDBUser;

    private final String pathinfoDBPassword;

    public Parameters(Properties props)
    {
        this.openbisUser = getMandatoryProperty(props, OPENBIS_USER);
        this.openbisPassword = getMandatoryProperty(props, OPENBIS_PASSWORD);
        this.openbisServerURL = getMandatoryProperty(props, OPENBIS_SERVER_URL);
        this.pathinfoDBConnectionString = getMandatoryProperty(props, PATHINFO_DB_CONNECTION_STRING);
        this.pathinfoDBUser = getMandatoryProperty(props, PATHINFO_DB_USER);
        this.pathinfoDBPassword = getMandatoryProperty(props, PATHINFO_DB_CONNECTION_PASSWORD);
    }

    public String getOpenbisUser()
    {
        return openbisUser;
    }

    public String getOpenbisPassword()
    {
        return openbisPassword;
    }

    public String getOpenbisServerURL()
    {
        return openbisServerURL;
    }

    public String getPathinfoDBConnectionString()
    {
        return pathinfoDBConnectionString;
    }

    public String getPathinfoDBUser()
    {
        return pathinfoDBUser;
    }

    public String getPathinfoDBPassword()
    {
        return pathinfoDBPassword;
    }
}
