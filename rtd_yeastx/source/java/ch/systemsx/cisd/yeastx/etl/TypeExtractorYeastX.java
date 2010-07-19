/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.etlserver.FileTypeExtractor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * Implementation of {@link ITypeExtractor} which gets the types from the file extension. Some of
 * the types are hard-coded.
 * 
 * @author Tomasz Pylak
 */
public class TypeExtractorYeastX extends FileTypeExtractor implements ITypeExtractor
{
    private static final String LOCATOR_TYPE_CODE = LocatorType.DEFAULT_LOCATOR_TYPE_CODE;

    @Private
    // Dataset type which is used when the user has not configured it otherwise
    static final String UNRECOGNIZED_DATA_SET_TYPE = "UNKNOWN";

    public TypeExtractorYeastX(final Properties properties)
    {
        super(properties);
    }

    public final LocatorType getLocatorType(final File incomingDataSetPath)
    {
        return new LocatorType(LOCATOR_TYPE_CODE);
    }

    public final DataSetType getDataSetType(final File incomingDataSetPath)
    {
        String fileExtension = getExtension(incomingDataSetPath);
        String datasetType;
        if (getMappedExtension(fileExtension) == null)
        {
            datasetType = UNRECOGNIZED_DATA_SET_TYPE;
        } else
        {
            datasetType = fileExtension;
        }
        return new DataSetType(datasetType);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return getExtension(incomingDataSetPath).equalsIgnoreCase(ConstantsYeastX.MZXML_EXT);
    }

}
