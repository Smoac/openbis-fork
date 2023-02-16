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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.dataset;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.dataset.DataSetValidatorTestService;

/**
 * @author pkupczyk
 */
public class DataSetPEByExperimentOrSampleIdentifierValidatorSystemTest extends CommonValidatorSystemTest<DataPE>
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected DataPE createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return getDataSet(spacePE, projectPE, (DataSetKind) param);
    }

    @Override
    protected DataPE validateObject(ProjectAuthorizationUser user, DataPE object, Object param)
    {
        return getBean(DataSetValidatorTestService.class).testDataSetPEByExperimentOrSampleIdentifierValidator(user.getSessionProvider(), object);
    }

    @Override
    protected CommonValidatorSystemTestAssertions<DataPE> getAssertions()
    {
        return new CommonValidatorSystemTestDataSetAssertions<DataPE>(super.getAssertions());
    }

}
