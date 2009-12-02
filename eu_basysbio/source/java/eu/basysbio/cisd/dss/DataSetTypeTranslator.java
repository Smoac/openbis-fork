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

package eu.basysbio.cisd.dss;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;

/**
 * Translator of data set types into openBIS conform upper-case data set types. 
 *
 * @author Franz-Josef Elmer
 */
class DataSetTypeTranslator
{
    static final String DATA_SET_TYPES_KEY = "data-set-types";
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, DataSetTypeTranslator.class);
    
    private final Map<String, String> map = new HashMap<String, String>();

    DataSetTypeTranslator(Properties properties)
    {
        String sequence = PropertyUtils.getMandatoryProperty(properties, DATA_SET_TYPES_KEY);
        String[] types = PropertyParametersUtil.parseItemisedProperty(sequence, DATA_SET_TYPES_KEY);
        for (String type : types)
        {
            String translatedType = properties.getProperty(type);
            if (translatedType == null)
            {
                translatedType = type.toUpperCase();
                operationLog.warn("No translation found for data set type '" + type
                        + "' using default translation '" + translatedType + "'.");
            }
            map.put(type, translatedType);
        }
    }
    
    String translate(String type)
    {
        String translatedType = map.get(type);
        if (translatedType == null)
        {
            throw new UserFailureException("Unknown data set type: " + type);
        }
        return translatedType;
    }
}
