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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectPEPredicateTest extends CommonPredicateTest<ProjectPE>
{

    @Override
    protected void expectWithAll(IAuthorizationConfig config, ProjectPE object)
    {
        expectAuthorizationConfig(config);
    }

    @Override
    protected ProjectPE createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return projectPE;
    }

    @Override
    protected Status evaluateObject(ProjectPE object, RoleWithIdentifier... roles)
    {
        ProjectPEPredicate predicate = new ProjectPEPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), object);
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertEquals(NullPointerException.class, t.getClass());
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

}
