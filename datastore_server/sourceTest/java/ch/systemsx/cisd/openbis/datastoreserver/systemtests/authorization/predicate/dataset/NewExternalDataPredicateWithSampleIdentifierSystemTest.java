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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleIdentifierUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class NewExternalDataPredicateWithSampleIdentifierSystemTest extends CommonSamplePredicateSystemTest<SampleIdentifier>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ_WRITE;
    }

    @Override
    protected SampleIdentifier createNonexistentObject(Object param)
    {
        return SampleIdentifierUtil.createNonexistentObject(param);
    }

    @Override
    protected SampleIdentifier createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return SampleIdentifierUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<SampleIdentifier> objects, Object param)
    {
        NewExternalData data = null;

        if (objects.get(0) != null)
        {
            data = new NewExternalData();
            data.setSampleIdentifierOrNull(objects.get(0));
        }

        getBean(DataSetPredicateTestService.class).testNewExternalDataPredicate(sessionProvider, data);
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No new data set specified.");
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            assertNoException(t);
        }
    }

}
