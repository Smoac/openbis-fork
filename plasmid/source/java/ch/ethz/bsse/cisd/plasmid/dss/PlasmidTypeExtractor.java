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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * The extractor that expects a single file and recognizes following data set types:
 * <ul>
 * <li>GB -- for a file with {@code .gb} extension
 * <li>SEQUENCING -- for a file with {@code .ab1} extension
 * <li>VERIFICATION -- for all other files
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class PlasmidTypeExtractor implements ITypeExtractor
{

    public PlasmidTypeExtractor(final Properties properties)
    {
    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        final String code =
                DataSetTypeOracle.extractDataSetTypeInfo(incomingDataSetPath).getDataSetTypeCode();
        return new DataSetType(code);
    }

    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return new FileFormatType(FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE);
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return DataSetTypeOracle.extractDataSetTypeInfo(incomingDataSetPath).isMeasured();
    }

}
