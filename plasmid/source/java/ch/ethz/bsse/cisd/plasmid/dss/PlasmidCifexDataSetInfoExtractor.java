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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.cifex.CifexDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * {@link IDataSetInfoExtractor} implementation that delegates extraction of data to
 * {@link CifexDataSetInfoExtractor} and adds one property of type {@code FILE_NAME} holding name of
 * the file stored for the data set. Registration will fail if this property type doesn't exist in
 * openBIS DB or is not attached to the data set type extracted by type extractor.
 * 
 * @author Piotr Buczek
 */
public class PlasmidCifexDataSetInfoExtractor implements IDataSetInfoExtractor
{
    private final IDataSetInfoExtractor delegator;

    public PlasmidCifexDataSetInfoExtractor(final Properties globalProperties)
    {
        this.delegator = new CifexDataSetInfoExtractor(globalProperties);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetFile,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        final DataSetInformation result =
                delegator.getDataSetInformation(incomingDataSetFile, openbisService);
        final NewProperty fileNameProperty =
                DataSetFileNamePropertyHelper.createProperty(incomingDataSetFile, true);
        result.getDataSetProperties().add(fileNameProperty);
        return result;
    }
}
