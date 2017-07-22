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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.deletion;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonSampleValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.deleteion.DeletionValidatorTestService;

/**
 * @author pkupczyk
 */
public class DeletionValidatorWithSampleSystemTest extends CommonSampleValidatorSystemTest<Deletion>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ_WRITE;
    }

    @Override
    protected Deletion createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE sample = getSample(spacePE, projectPE, (SampleKind) param);
        return getCommonService().trashSample(sample);
    }

    @Override
    protected Deletion validateObject(IAuthSessionProvider sessionProvider, Deletion object, Object param)
    {
        try
        {
            return getBean(DeletionValidatorTestService.class).testDeletionValidator(sessionProvider, object);
        } finally
        {
            if (object != null)
            {
                getCommonService().untrash(object.getId());
            }
        }
    }

    @Override
    protected void assertWithInstanceObserverUser(PersonPE person, Deletion result, Throwable t, Object param)
    {
        assertNull(result);
    }

}
