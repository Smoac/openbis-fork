/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPredicate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * @author Pawel Glyzewski
 */
public class ProjectPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testHaveAccessToProject()
    {
        ProjectPredicate predicate = new ProjectPredicate();
        Project project = new Project(SPACE_CODE, "XXX");

        context.checking(new Expectations()
            {
                {
                    allowing(provider).listSpaces();
                    will(returnValue(createGroups()));

                    DatabaseInstancePE db = new DatabaseInstancePE();
                    db.setCode(INSTANCE_CODE);
                    db.setUuid("global_" + INSTANCE_CODE);
                    allowing(provider).getHomeDatabaseInstance();
                    will(returnValue(db));
                }
            });
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(false), project);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testHaveNoAccessToProject()
    {
        ProjectPredicate predicate = new ProjectPredicate();
        Project project = new Project(ANOTHER_GROUP_CODE, "XXX");

        context.checking(new Expectations()
            {
                {
                    allowing(provider).listSpaces();
                    will(returnValue(createGroups()));

                    DatabaseInstancePE db = new DatabaseInstancePE();
                    db.setCode(INSTANCE_CODE);
                    db.setUuid("global_" + INSTANCE_CODE);
                    allowing(provider).getHomeDatabaseInstance();
                    will(returnValue(db));
                }
            });
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(false), project);

        assertEquals(true, status.isError());
        context.assertIsSatisfied();
    }
}
