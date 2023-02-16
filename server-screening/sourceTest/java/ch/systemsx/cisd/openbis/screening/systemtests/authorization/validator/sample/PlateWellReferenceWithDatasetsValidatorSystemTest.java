/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.sample;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.CommonValidatorScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class PlateWellReferenceWithDatasetsValidatorSystemTest
        extends CommonValidatorScreeningSystemTest<PlateWellReferenceWithDatasets>
{

    @Override
    protected PlateWellReferenceWithDatasets createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        ExperimentPE experimentPE = getExperiment(spacePE, projectPE);
        Plate plate = new Plate(null, null, null, new ExperimentIdentifier(experimentPE.getCode(), experimentPE.getProject().getCode(),
                experimentPE.getProject().getSpace().getCode(), null));
        return new PlateWellReferenceWithDatasets(plate, null);
    }

    @Override
    protected PlateWellReferenceWithDatasets validateObject(ProjectAuthorizationUser user, PlateWellReferenceWithDatasets object,
            Object param)
    {
        return getBean(SampleValidatorScreeningTestService.class).testPlateWellReferenceWithDatasetsValidator(user.getSessionProvider(), object);
    }

}
