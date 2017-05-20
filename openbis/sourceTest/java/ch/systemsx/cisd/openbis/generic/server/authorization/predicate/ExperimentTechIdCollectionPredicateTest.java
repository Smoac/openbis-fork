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
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ExperimentTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ExperimentTechIdCollectionPredicateTest extends ObjectTechIdCollectionPredicateTest
{

    private static ExperimentPE EXPERIMENT_IN_SPACE_PROJECT = new ExperimentPE();

    private static ExperimentPE EXPERIMENT_IN_SPACE_ANOTHER_PROJECT = new ExperimentPE();

    private static ExperimentPE EXPERIMENT_IN_ANOTHER_SPACE_PROJECT = new ExperimentPE();

    private static ExperimentPE EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT = new ExperimentPE();

    private static Map<TechId, ExperimentPE> EXPERIMENT_ID_TO_PE = new HashMap<TechId, ExperimentPE>();

    static
    {
        EXPERIMENT_IN_SPACE_PROJECT.setProject(SPACE_PROJECT_PE);
        EXPERIMENT_IN_SPACE_ANOTHER_PROJECT.setProject(SPACE_ANOTHER_PROJECT_PE);

        EXPERIMENT_IN_ANOTHER_SPACE_PROJECT.setProject(ANOTHER_SPACE_PROJECT_PE);
        EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT.setProject(ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        EXPERIMENT_ID_TO_PE.put(OBJECT_IN_SPACE_PROJECT, EXPERIMENT_IN_SPACE_PROJECT);
        EXPERIMENT_ID_TO_PE.put(OBJECT_IN_SPACE_ANOTHER_PROJECT, EXPERIMENT_IN_SPACE_ANOTHER_PROJECT);
        EXPERIMENT_ID_TO_PE.put(OBJECT_IN_ANOTHER_SPACE_PROJECT, EXPERIMENT_IN_ANOTHER_SPACE_PROJECT);
        EXPERIMENT_ID_TO_PE.put(OBJECT_IN_ANOTHER_SPACE_ANOTHER_PROJECT, EXPERIMENT_IN_ANOTHER_SPACE_ANOTHER_PROJECT);
    }

    @Override
    protected Status evaluateObjects(List<TechId> object, RoleWithIdentifier... roles)
    {
        ExperimentTechIdCollectionPredicate predicate = new ExperimentTechIdCollectionPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), object);
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
                        allowing(provider).tryGetExperimentsByTechIds(objects);

                        Map<TechId, ExperimentPE> map = new HashMap<TechId, ExperimentPE>();

                        if (objects != null)
                        {
                            for (TechId techId : objects)
                            {
                                if (EXPERIMENT_ID_TO_PE.containsKey(techId))
                                {
                                    map.put(techId, EXPERIMENT_ID_TO_PE.get(techId));
                                }
                            }
                        }

                        will(returnValue(map));
                    }

                    expectGetDistinctSpacesByEntityIds(SpaceOwnerKind.EXPERIMENT);
                }
            });
    }

    @Override
    protected void assertWithNullCollection(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No EXPERIMENT technical id collection specified.");
    }

}