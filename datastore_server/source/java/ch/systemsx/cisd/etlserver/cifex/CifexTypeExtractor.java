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

package ch.systemsx.cisd.etlserver.cifex;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.SimpleTypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorType;

/**
 * {@link ITypeExtractor} which extracts data set type and file format from the CIFEX comment saved
 * in 'request.properties' file. <br>
 * Locator type, processor type and 'is measured' values are calculated by corresponding methods of
 * {@link SimpleTypeExtractor}.
 * 
 * @author Izabela Adamczyk
 */
public class CifexTypeExtractor implements ITypeExtractor
{

    private ITypeExtractor simpleTypeExtractor;

    public CifexTypeExtractor(final Properties properties)
    {
        simpleTypeExtractor = new SimpleTypeExtractor(properties);
    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        return new DataSetType(CifexExtratorHelper.getDataSetUploadInfo(incomingDataSetPath)
                .getDataSetType());
    }

    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return new FileFormatType(CifexExtratorHelper.getDataSetUploadInfo(incomingDataSetPath)
                .getFileType());
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return simpleTypeExtractor.getLocatorType(incomingDataSetPath);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return simpleTypeExtractor.getProcessorType(incomingDataSetPath);
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return simpleTypeExtractor.isMeasuredData(incomingDataSetPath);
    }

}
