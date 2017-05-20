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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ProjectTechIdCollectionPredicateTest extends ObjectTechIdCollectionPredicateTest
{

    private static Map<TechId, ProjectPE> PROJECT_ID_TO_PE = new HashMap<TechId, ProjectPE>();

    static
    {
        PROJECT_ID_TO_PE.put(OBJECT_IN_SPACE_PROJECT, SPACE_PROJECT_PE);
        PROJECT_ID_TO_PE.put(OBJECT_IN_SPACE_ANOTHER_PROJECT, SPACE_ANOTHER_PROJECT_PE);
        PROJECT_ID_TO_PE.put(OBJECT_IN_ANOTHER_SPACE_PROJECT, ANOTHER_SPACE_PROJECT_PE);
        PROJECT_ID_TO_PE.put(OBJECT_IN_ANOTHER_SPACE_ANOTHER_PROJECT, ANOTHER_SPACE_ANOTHER_PROJECT_PE);
    }

    @Override
    protected Status evaluateObjects(List<TechId> objects, RoleWithIdentifier... roles)
    {
        ProjectTechIdCollectionPredicate predicate = new ProjectTechIdCollectionPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), objects);
    }

    @Override
    protected void expectWithAll(final IAuthorizationConfig config, final List<TechId> objects)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(objects);

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();

                        if (objects != null)
                        {
                            for (TechId object : objects)
                            {
                                if (PROJECT_ID_TO_PE.containsKey(object))
                                {
                                    map.put(object, PROJECT_ID_TO_PE.get(object));
                                }
                            }
                        }

                        will(returnValue(map));
                    }

                    expectGetDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT);
                }
            });
    }

    @Override
    protected void assertWithNullCollection(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No PROJECT technical id collection specified.");
    }

}