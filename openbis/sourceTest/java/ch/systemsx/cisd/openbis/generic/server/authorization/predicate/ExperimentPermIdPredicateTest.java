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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ExperimentPermIdPredicateTest extends CommonPredicateTest<PermId>
{

    private static final PermId EXPERIMENT_PERM_ID = new PermId("experimentPermId");

    private static final PermId NON_EXISTENT_EXPERIMENT_PERM_ID = new PermId("nonExistentExperimentPermId");

    @Override
    protected void expectWithAll(IAuthorizationConfig config, final List<PermId> objects)
    {
        final PermId object = objects.get(0);

        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (object != null)
                    {
                        allowing(provider).tryGetExperimentByPermId(object.getId());

                        if (EXPERIMENT_PERM_ID.equals(object))
                        {
                            will(returnValue(SPACE_PROJECT_EXPERIMENT_PE));
                        } else if (NON_EXISTENT_EXPERIMENT_PERM_ID.equals(object))
                        {
                            will(returnValue(null));
                        }
                    }
                }
            });
    }

    @Override
    protected PermId createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE) && SPACE_PROJECT_PE.equals(projectPE))
        {
            return EXPERIMENT_PERM_ID;
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE) && NON_EXISTENT_SPACE_PROJECT_PE.equals(projectPE))
        {
            return NON_EXISTENT_EXPERIMENT_PERM_ID;
        } else
        {
            throw new RuntimeException();
        }
    }

    @Override
    protected Status evaluateObjects(List<PermId> objects, RoleWithIdentifier... roles)
    {
        ExperimentPermIdPredicate predicate = new ExperimentPermIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), objects.get(0));
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No experiment perm id specified.");
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
