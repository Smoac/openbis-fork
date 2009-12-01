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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.SimpleTypeExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypeExtractor implements ITypeExtractor
{
    static final String DATA_TYPE = ".data.txt";
    
    private final SimpleTypeExtractor extractor;

    public TypeExtractor(Properties properties)
    {
        extractor = new SimpleTypeExtractor(properties);
    }

    public final DataSetType getDataSetType(File incomingDataSetPath)
    {
        if (incomingDataSetPath.isDirectory() == false)
        {
            throw new UserFailureException("Data set should be a folder: "
                    + incomingDataSetPath.getAbsolutePath());
        }
        String[] files = incomingDataSetPath.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(DATA_TYPE);
                }
            });
        if (files.length != 1)
        {
            throw new UserFailureException("Exactly one file of type '" + DATA_TYPE
                    + "' expected instead of " + files.length);
        }
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(files[0].substring(0, files[0].length() - DATA_TYPE.length()));
        return dataSetType;
    }

    public final FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return extractor.getFileFormatType(incomingDataSetPath);
    }

    public final LocatorType getLocatorType(File incomingDataSetPath)
    {
        return extractor.getLocatorType(incomingDataSetPath);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return extractor.getProcessorType(incomingDataSetPath);
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return extractor.isMeasuredData(incomingDataSetPath);
    }

    
}
