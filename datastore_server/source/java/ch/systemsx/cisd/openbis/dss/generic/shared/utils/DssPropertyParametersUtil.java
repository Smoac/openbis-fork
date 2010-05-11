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

import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Utility class to load properties.
 * 
 * @author Tomasz Pylak
 */
public class DssPropertyParametersUtil
{
    @Private
    static final String DSS_CODE_KEY = "data-store-server-code";

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        return loadProperties(SERVICE_PROPERTIES_FILE);
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        Properties properties = PropertyUtils.loadProperties(filePath);
        return ExtendedProperties.createWith(properties);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY);
    }

}
