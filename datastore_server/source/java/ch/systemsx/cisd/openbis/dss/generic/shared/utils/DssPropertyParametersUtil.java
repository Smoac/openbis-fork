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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Utility class to load properties.
 * 
 * @author Tomasz Pylak
 */
public class DssPropertyParametersUtil
{
    /** Prefix of system properties which may override service.properties. */
    public static final String OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX = "openbis.dss.";

    @Private
    static final String DSS_CODE_KEY = "data-store-server-code";

    @Private
    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String DOWNLOAD_URL_KEY = "download-url";

    /** Location of service properties file. */
    public static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        return loadProperties(SERVICE_PROPERTIES_FILE);
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        Properties properties = PropertyUtils.loadProperties(filePath);
        Properties systemProperties = System.getProperties();
        ExtendedProperties dssSystemProperties =
                ExtendedProperties.getSubset(systemProperties, OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX, true);
        Set<Entry<Object, Object>> entrySet = dssSystemProperties.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        return ExtendedProperties.createWith(properties);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY);
    }

    public final static File getStoreRootDir(final Properties properties)
    {
        return FileUtilities.normalizeFile(new File(PropertyUtils.getMandatoryProperty(properties,
                STOREROOT_DIR_KEY)));
    }

}
