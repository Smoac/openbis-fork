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
package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.project;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertionsDelegate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.project.ProjectPredicateTestService;

/**
 * @author pkupczyk
 */
public class ProjectIdPredicateWithTechIdSystemTest extends CommonPredicateSystemTest<ProjectTechIdId>
{

    @Override
    protected ProjectTechIdId createNonexistentObject(Object param)
    {
        return new ProjectTechIdId(-1L);
    }

    @Override
    protected ProjectTechIdId createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return new ProjectTechIdId(projectPE.getId());
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<ProjectTechIdId> objects, Object param)
    {
        getBean(ProjectPredicateTestService.class).testProjectIdPredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<ProjectTechIdId> getAssertions()
    {
        return new CommonPredicateSystemTestAssertionsDelegate<ProjectTechIdId>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertException(t, UserFailureException.class, "No project id specified.");
                    }
                }

                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    if (user.isDisabledProjectUser())
                    {
                        assertAuthorizationFailureExceptionThatNoRoles(t);
                    } else
                    {
                        assertUserFailureExceptionThatProjectDoesNotExist(t);
                    }
                }
            };
    }

}
