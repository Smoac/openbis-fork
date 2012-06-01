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

package ch.systemsx.cisd.cina.dss.bundle;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaBundleTypeExtractor implements ITypeExtractor
{
    public CinaBundleTypeExtractor(final Properties properties)
    {

    }

    @Override
    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        return new DataSetType(CinaConstants.BUNDLE_DATA_SET_TYPE_CODE);
    }

    @Override
    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return new FileFormatType("PROPRIETARY");
    }

    @Override
    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return new LocatorType("RELATIVE_LOCATION");
    }

    @Override
    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    @Override
    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return true;
    }

}
