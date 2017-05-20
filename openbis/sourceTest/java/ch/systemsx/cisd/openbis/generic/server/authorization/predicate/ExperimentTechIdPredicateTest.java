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

import org.jmock.Expectations;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ExperimentTechIdPredicateTest extends CommonPredicateTest<TechId>
{

    private static TechId EXPERIMENT_ID = new TechId(123L);

    private static TechId NON_EXISTENT_EXPERIMENT_ID = new TechId(234L);

    @Override
    protected void expectWithAll(IAuthorizationConfig config, final List<TechId> objects)
    {
        final TechId object = objects.get(0);

        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.EXPERIMENT, object);

                    if (EXPERIMENT_ID.equals(object))
                    {
                        will(returnValue(SPACE_PE));
                    } else if (NON_EXISTENT_EXPERIMENT_ID.equals(object))
                    {
                        will(returnValue(null));
                    }

                    allowing(provider).tryGetExperimentByTechId(object);

                    if (EXPERIMENT_ID.equals(object))
                    {
                        will(returnValue(SPACE_PROJECT_EXPERIMENT_PE));
                    } else if (NON_EXISTENT_EXPERIMENT_ID.equals(object))
                    {
                        will(returnValue(null));
                    }
                }
            });
    }

    @Override
    protected TechId createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE) && SPACE_PROJECT_PE.equals(projectPE))
        {
            return EXPERIMENT_ID;
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE) && NON_EXISTENT_SPACE_PROJECT_PE.equals(projectPE))
        {
            return NON_EXISTENT_EXPERIMENT_ID;
        } else
        {
            throw new RuntimeException();
        }
    }

    @Override
    protected Status evaluateObjects(List<TechId> objects, RoleWithIdentifier... roles)
    {
        ExperimentTechIdPredicate predicate = new ExperimentTechIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), objects.get(0));
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No technical id specified.");
    }

}
