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

package ch.systemsx.cisd.cina.dss;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.etl.MicroscopyBlackboxSeriesStorageProcessor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * An extension of the {@link MicroscopyBlackboxSeriesStorageProcessor} that accepts a particular subclass of DataSetInformation.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaImageStorageProcessor extends MicroscopyBlackboxSeriesStorageProcessor
{

    /**
     * @param properties
     */
    public CinaImageStorageProcessor(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean accepts(DataSetInformation dataSetInformation, File incomingDataSet)
    {
        return dataSetInformation instanceof CinaImageDataSetInformation;
    }

}
