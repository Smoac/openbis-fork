/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.experiment;

import org.springframework.beans.factory.annotation.Autowired;

import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment.ExperimentDB;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.CommonValidatorSystemTest;

/**
 * @author pkupczyk
 */
public class ScreeningExperimentValidatorSystemTest extends CommonValidatorSystemTest<ExperimentIdentifier>
{

    @Autowired
    private ExperimentValidatorScreeningTestService service;

    @Override
    protected ExperimentIdentifier createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        String code = ExperimentDB.getCode(spacePE, projectPE);
        String permId = ExperimentDB.getPermId(spacePE, projectPE);
        return new ExperimentIdentifier(code, projectPE.getCode(), spacePE.getCode(), permId);
    }

    @Override
    protected ExperimentIdentifier validateObject(IAuthSessionProvider sessionProvider, ExperimentIdentifier object)
    {
        return service.testScreeningExperimentValidator(sessionProvider, object);
    }

}
