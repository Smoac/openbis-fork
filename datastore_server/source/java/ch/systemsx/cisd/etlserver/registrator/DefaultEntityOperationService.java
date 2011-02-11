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

package ch.systemsx.cisd.etlserver.registrator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

public class DefaultEntityOperationService<T extends DataSetInformation> implements
        IEntityOperationService<T>
{
    private final AbstractOmniscientTopLevelDataSetRegistrator<T> registrator;

    public DefaultEntityOperationService(AbstractOmniscientTopLevelDataSetRegistrator<T> registrator)
    {
        this.registrator = registrator;
    }

    public AtomicEntityOperationResult performOperationsInApplcationServer(
            AtomicEntityOperationDetails<T> registrationDetails)
    {
        IEncapsulatedOpenBISService openBisService =
                registrator.getGlobalState().getOpenBisService();

        return openBisService.performEntityOperations(convert(registrationDetails));
    }

    private ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails convert(
            AtomicEntityOperationDetails<T> details)
    {

        List<NewExperiment> experimentRegistrations = details.getExperimentRegistrations();
        List<SampleUpdatesDTO> sampleUpdates = details.getSampleUpdates();
        List<NewSample> sampleRegistrations = details.getSampleRegistrations();
        List<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();

        for (DataSetRegistrationInformation<?> dsRegistration : details.getDataSetRegistrations())
        {
            NewExternalData newExternalData = dsRegistration.getExternalData();
            dataSetRegistrations.add(newExternalData);
        }

        return new ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails(
                experimentRegistrations, sampleUpdates, sampleRegistrations, dataSetRegistrations);
    }

}