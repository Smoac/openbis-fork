/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;

/**
 * 
 *
 * @author Jakub Straszewski
 */
public interface IImagingDataSetRegistrationTransactionV2 extends
 IDataSetRegistrationTransactionV2
{
    /**
     * Creates a new image dataset. See {@link SimpleImageDataConfig} documentation for
     * configuration details.
     */
    IImageDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
            File incomingFolderWithImages);
}
