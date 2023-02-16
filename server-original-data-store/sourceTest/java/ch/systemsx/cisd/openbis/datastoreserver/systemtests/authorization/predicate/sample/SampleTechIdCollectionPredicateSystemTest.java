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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SampleTechIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestSampleAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample.SamplePredicateTestService;

/**
 * @author pkupczyk
 */
public class SampleTechIdCollectionPredicateSystemTest extends CommonPredicateSystemTest<TechId>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    public Object[] getParams()
    {
        return getSampleKinds(SampleKind.SHARED_READ);
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
    protected void evaluateObjects(ProjectAuthorizationUser user, List<TechId> objects, Object param)
    {
        getBean(SamplePredicateTestService.class).testSampleTechIdCollectionPredicate(user.getSessionProvider(), objects);
    }

    @Override
    protected CommonPredicateSystemTestAssertions<TechId> getAssertions()
    {
        return new CommonPredicateSystemTestSampleAssertions<TechId>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, NullPointerException.class, null);
                    }
                }

                @Override
                public void assertWithNullCollection(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, UserFailureException.class, "No sample technical ids specified.");
                    }
                }
            };
    }

}
