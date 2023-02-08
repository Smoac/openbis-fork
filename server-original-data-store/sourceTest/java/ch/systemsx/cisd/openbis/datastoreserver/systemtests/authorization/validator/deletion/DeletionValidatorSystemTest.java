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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.deleteion.DeletionValidatorTestService;

/**
 * @author pkupczyk
 */
public abstract class DeletionValidatorSystemTest extends CommonValidatorSystemTest<Deletion>
{

    @Override
    protected Deletion validateObject(ProjectAuthorizationUser user, Deletion object, Object param)
    {
        try
        {
            return getBean(DeletionValidatorTestService.class).testDeletionValidator(user.getSessionProvider(), object);
        } finally
        {
            if (object != null)
            {
                getCommonService().untrash(object.getId());
            }
        }
    }

}
