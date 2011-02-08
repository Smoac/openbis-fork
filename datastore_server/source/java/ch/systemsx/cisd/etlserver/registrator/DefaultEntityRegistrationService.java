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
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityRegistrationDetails;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.AtomicEntityRegistrationResult;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetRegistrationInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

public class DefaultEntityRegistrationService<T extends DataSetInformation> implements
        IEntityRegistrationService<T>
{
    private final AbstractOmniscientTopLevelDataSetRegistrator<T> registrator;

    public DefaultEntityRegistrationService(
            AbstractOmniscientTopLevelDataSetRegistrator<T> registrator)
    {
        this.registrator = registrator;
    }

    public AtomicEntityRegistrationResult registerEntitiesInApplcationServer(
            AtomicEntityRegistrationDetails<T> registrationDetails)
    {
        // Arrays to hold return values
        ArrayList<Experiment> experimentsUpdated = new ArrayList<Experiment>();
        ArrayList<Experiment> experimentsCreated = new ArrayList<Experiment>();
        ArrayList<Sample> samplesUpdated = new ArrayList<Sample>();
        ArrayList<Sample> samplesCreated = new ArrayList<Sample>();
        ArrayList<DataSetInformation> dataSetsCreated = new ArrayList<DataSetInformation>();

        IEncapsulatedOpenBISService openBisService =
                registrator.getGlobalState().getOpenBisService();
        List<NewExperiment> experimentRegistrations =
                registrationDetails.getExperimentRegistrations();
        for (NewExperiment experiment : experimentRegistrations)
        {
            openBisService.registerExperiment(experiment);
            ExperimentIdentifier experimentIdentifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            experimentsCreated.add(openBisService.tryToGetExperiment(experimentIdentifier));
        }

        List<DataSetRegistrationInformation<T>> dataSetRegistrations =
                registrationDetails.getDataSetRegistrations();
        for (DataSetRegistrationInformation<T> dataSetRegistration : dataSetRegistrations)
        {
            openBisService.registerDataSet(dataSetRegistration.getDataSetInformation(),
                    dataSetRegistration.getExternalData());
            dataSetsCreated.add(dataSetRegistration.getDataSetInformation());
        }

        return new AtomicEntityRegistrationResult(experimentsUpdated, experimentsCreated,
                samplesUpdated, samplesCreated, dataSetsCreated);

    }

}