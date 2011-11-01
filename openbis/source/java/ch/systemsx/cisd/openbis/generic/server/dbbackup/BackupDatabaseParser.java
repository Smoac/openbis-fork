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

package ch.systemsx.cisd.openbis.generic.server.dbbackup;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Parses a given <code>service.properties</code> and produces a textual description of a database
 * that needs to be backed up. The textual description looks like this :
 * 
 * <pre>
 * database=XXXX;username=XXXX;password=XXXX
 * </pre>
 * 
 * Based on it, the openBIS admin scripts can backup databases during the upgrade process.
 * 
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseParser
{

    private static final String OWNER = "owner";

    private static final String OWNER_PASSWORD = "owner-password";

    private static final String URL_HOST_PART = "url-host-part";

    private static final String DB_KIND = "kind";

    private static final String EMPTY = "";

    public static String getAppServerDatabaseDescription(Properties properties, String dbKeyPrefix,
            String basicDatabaseName)
    {
        ExtendedProperties dbProps = ExtendedProperties.getSubset(properties, dbKeyPrefix, true);
        if (dbProps.isEmpty())
        {
            return EMPTY;
        }

        DatabaseConfigurationContext context = new DatabaseConfigurationContext();

        context.setBasicDatabaseName(basicDatabaseName);
        context.setDatabaseKind(dbProps.getProperty(DB_KIND, "dev"));
        context.setUrlHostPart(dbProps.getProperty(URL_HOST_PART, EMPTY));
        context.setOwner(dbProps.getProperty(OWNER, EMPTY));
        context.setPassword(dbProps.getProperty(OWNER_PASSWORD, EMPTY));

        return createReturnValue(context);
    }

    public static String getDssServerDatabaseDescription(Properties properties,
            String databaseVersionHolder)
    {
        String dbKeyPrefix = findDatabasePrefixForVersionHolder(properties, databaseVersionHolder);

        if (StringUtils.isEmpty(dbKeyPrefix))
        {
            return StringUtils.EMPTY;
        }

        ExtendedProperties dbProps = ExtendedProperties.getSubset(properties, dbKeyPrefix, true);
        DatabaseConfigurationContext context =
                BeanUtils.createBean(DatabaseConfigurationContext.class, dbProps);

        return createReturnValue(context);
    }

    private static String findDatabasePrefixForVersionHolder(Properties properties,
            String databaseVersionHolder)
    {
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            String propertyValue = ((String) entry.getValue()).trim();
            if (databaseVersionHolder.equalsIgnoreCase(propertyValue))
            {
                String key = (String) entry.getKey();
                return getDatabasePrefixFromVersionHolderKey(key);
            }
        }
        return null;
    }

    private static String getDatabasePrefixFromVersionHolderKey(String key)
    {
        final String VERSION_HOLDER_CLASS = "version-holder-class";
        if (false == key.endsWith(VERSION_HOLDER_CLASS))
        {
            throw new IllegalArgumentException(String.format(
                    "Version holder key '%s' must end with '%s'.", key, VERSION_HOLDER_CLASS));

        }

        return key.substring(0, key.length() - VERSION_HOLDER_CLASS.length());
    }

    private static String createReturnValue(DatabaseConfigurationContext context)
    {
        String database = context.getDatabaseName();
        String username = context.getOwner();
        String password = context.getPassword();

        String template = "database=%s;username=%s;password=%s";

        return String.format(template, database, username, password);
    }
}
