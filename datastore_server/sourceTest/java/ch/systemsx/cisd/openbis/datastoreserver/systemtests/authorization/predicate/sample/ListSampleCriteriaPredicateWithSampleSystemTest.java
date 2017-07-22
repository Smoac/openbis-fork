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
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public abstract class ListSampleCriteriaPredicateWithSampleSystemTest extends CommonSamplePredicateSystemTest<ListSampleCriteria>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    protected abstract ListSampleCriteria createListSampleCriteria(TechId id);

    @Override
    protected ListSampleCriteria createNonexistentObject(Object param)
    {
        return createListSampleCriteria(new TechId(-1));
    }

    @Override
    protected ListSampleCriteria createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = getSample(spacePE, projectPE, (SampleKind) param);
        return createListSampleCriteria(new TechId(samplePE.getId()));
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<ListSampleCriteria> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testListSampleCriteriaPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No sample listing criteria specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}
