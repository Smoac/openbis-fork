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
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleTechIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class SampleTechIdPredicateSystemTest extends CommonSamplePredicateSystemTest<TechId>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    @Override
    protected TechId createNonexistentObject(Object param)
    {
        return SampleTechIdUtil.createNonexistentObject(param);
    }

    @Override
    protected TechId createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return SampleTechIdUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<TechId> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testSampleTechIdPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No sample technical id specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        SampleTechIdUtil.assertWithNonexistentObjectForInstanceUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        SampleTechIdUtil.assertWithNonexistentObjectForProjectUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        SampleTechIdUtil.assertWithNonexistentObjectForSpaceUser(person, t, param);
    }

}
