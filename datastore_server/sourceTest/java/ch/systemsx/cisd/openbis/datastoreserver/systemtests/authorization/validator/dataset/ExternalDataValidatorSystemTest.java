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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.dataset;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.ExternalDataUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonDataSetValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.dataset.DataSetValidatorTestService;

/**
 * @author pkupczyk
 */
public class ExternalDataValidatorSystemTest extends CommonDataSetValidatorSystemTest<AbstractExternalData>
{

    @Override
    protected AbstractExternalData createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return ExternalDataUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected AbstractExternalData validateObject(IAuthSessionProvider sessionProvider, AbstractExternalData object, Object param)
    {
        return getBean(DataSetValidatorTestService.class).testExternalDataValidator(sessionProvider, object);
    }

}
