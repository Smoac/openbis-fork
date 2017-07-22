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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleV1Util;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class SampleListPredicateSystemTest extends CommonSamplePredicateSystemTest<Sample>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    @Override
    protected Sample createNonexistentObject(Object param)
    {
        return SampleV1Util.createNonexistentObject(param);
    }

    @Override
    protected Sample createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return SampleV1Util.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<Sample> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testSampleListPredicate(sessionProvider, objects);
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No sample specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        }
    }

}
