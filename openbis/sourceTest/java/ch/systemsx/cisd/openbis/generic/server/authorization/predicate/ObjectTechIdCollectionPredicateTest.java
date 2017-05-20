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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class ObjectTechIdCollectionPredicateTest extends CommonCollectionPredicateTest<TechId>
{

    protected static TechId OBJECT_IN_SPACE_PROJECT = new TechId(101L);

    protected static TechId OBJECT_IN_SPACE_ANOTHER_PROJECT = new TechId(102L);

    protected static TechId OBJECT_IN_ANOTHER_SPACE_PROJECT = new TechId(201L);

    protected static TechId OBJECT_IN_ANOTHER_SPACE_ANOTHER_PROJECT = new TechId(202L);

    protected static TechId OBJECT_NON_EXISTENT = new TechId(300L);

    @Override
    protected TechId createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE))
        {
            if (SPACE_PROJECT_PE.equals(projectPE))
            {
                return OBJECT_IN_SPACE_PROJECT;
            } else if (SPACE_ANOTHER_PROJECT_PE.equals(projectPE))
            {
                return OBJECT_IN_SPACE_ANOTHER_PROJECT;
            }
        } else if (ANOTHER_SPACE_PE.equals(spacePE))
        {
            if (ANOTHER_SPACE_PROJECT_PE.equals(projectPE))
            {
                return OBJECT_IN_ANOTHER_SPACE_PROJECT;
            } else if (ANOTHER_SPACE_ANOTHER_PROJECT_PE.equals(projectPE))
            {
                return OBJECT_IN_ANOTHER_SPACE_ANOTHER_PROJECT;
            }
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE))
        {
            return OBJECT_NON_EXISTENT;
        }

        throw new RuntimeException();
    }

    @SuppressWarnings("unchecked")
    protected void expectGetDistinctSpacesByEntityIds(final SpaceOwnerKind spaceOwnerKind)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(with(spaceOwnerKind),
                            with(any(List.class)));

                    will(new CustomAction("getSpacesByEntityIds")
                        {

                            @Override
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                List<TechId> entityIds = (List<TechId>) invocation.getParameter(1);
                                Set<SpacePE> entitySpaces = new HashSet<SpacePE>();

                                if (entityIds.contains(OBJECT_IN_SPACE_PROJECT) || entityIds.contains(OBJECT_IN_SPACE_ANOTHER_PROJECT))
                                {
                                    entitySpaces.add(SPACE_PE);
                                }
                                if (entityIds.contains(OBJECT_IN_ANOTHER_SPACE_PROJECT)
                                        || entityIds.contains(OBJECT_IN_ANOTHER_SPACE_ANOTHER_PROJECT))
                                {
                                    entitySpaces.add(ANOTHER_SPACE_PE);
                                }

                                return entitySpaces;
                            }
                        });
                }
            });
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

}