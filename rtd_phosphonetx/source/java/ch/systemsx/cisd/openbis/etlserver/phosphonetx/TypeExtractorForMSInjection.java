/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.DATA_SET_PROPERTIES_FILE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.DATA_SET_TYPE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.FILE_TYPE_KEY;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypeExtractorForMSInjection implements ITypeExtractor
{
    private static final LocatorType LOCATOR_TYPE = new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
    
    public TypeExtractorForMSInjection(Properties properties)
    {
    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        Properties properties = loadProperties(incomingDataSetPath);
        return new DataSetType(PropertyUtils.getMandatoryProperty(properties, DATA_SET_TYPE_KEY));
    }

    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        Properties properties = loadProperties(incomingDataSetPath);
        return new FileFormatType(PropertyUtils.getMandatoryProperty(properties, FILE_TYPE_KEY));
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return LOCATOR_TYPE;
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return true;
    }

    private Properties loadProperties(File incomingDataSetPath)
    {
        return Util.loadPropertiesFile(incomingDataSetPath, DATA_SET_PROPERTIES_FILE);
    }
    
}
