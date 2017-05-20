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
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectAugmentedCodePredicateTest extends CommonPredicateTest<String>
{

    private static String PROJECT_IDENTIFIER = "/" + SPACE_CODE + "/" + SPACE_PROJECT_CODE;

    private static String NON_EXISTENT_PROJECT_IDENTIFIER = "/" + NON_EXISTENT_SPACE_CODE + "/" + NON_EXISTENT_SPACE_PROJECT_CODE;

    @Override
    protected void expectWithAll(IAuthorizationConfig config, List<String> objects)
    {
        expectAuthorizationConfig(config);
        prepareProvider(Arrays.asList(SPACE_PE));
    }

    @Override
    protected String createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE) && SPACE_PROJECT_PE.equals(projectPE))
        {
            return PROJECT_IDENTIFIER;
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE) && NON_EXISTENT_SPACE_PROJECT_PE.equals(projectPE))
        {
            return NON_EXISTENT_PROJECT_IDENTIFIER;
        } else
        {
            throw new RuntimeException();
        }
    }

    @Override
    protected Status evaluateObjects(List<String> objects, RoleWithIdentifier... roles)
    {
        ProjectAugmentedCodePredicate predicate = new ProjectAugmentedCodePredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), objects.get(0));
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No project specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

}
