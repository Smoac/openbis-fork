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
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.cifex.CifexDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TimeSeriesDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private CifexDataSetInfoExtractor infoExtractor;

    public TimeSeriesDataSetInfoExtractor(Properties globalProperties)
    {
        infoExtractor = new CifexDataSetInfoExtractor(globalProperties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation info = infoExtractor.getDataSetInformation(incomingDataSetPath, openbisService);
        String email = info.tryGetUploadingUserEmail();
        if (email != null)
        {
            info.getDataSetProperties().add(new NewProperty(TimePointPropertyType.UPLOADER_EMAIL.toString(), email));
        }
        return info;
    }

}
