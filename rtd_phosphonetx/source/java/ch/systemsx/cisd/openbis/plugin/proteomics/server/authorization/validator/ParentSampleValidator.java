/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSample;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
public class ParentSampleValidator implements IValidator<Sample>
{
    private IAuthorizationDataProvider provider;

    private IValidator<Space> validator = new SpaceValidator();

    @Override
    public boolean isValid(PersonPE person, Sample sample)
    {
        return isValid(person, sample, true);
    }

    public boolean isValid(PersonPE person, Sample sample, boolean parentHasToBeValid)
    {
        Sample parent = sample.getGeneratedFrom();

        if (parent != null)
        {
            Space space = parent.getSpace();

            if (parentHasToBeValid == false || space == null || validator.isValid(person, space))
            {
                return true;
            } else
            {
                IProjectAuthorization<Sample> pa = new ProjectAuthorizationBuilder<Sample>()
                        .withData(provider)
                        .withUser(new UserProviderFromPersonPE(person))
                        .withRoles(new RolesProviderFromPersonPE(person))
                        .withObjects(new ProjectProviderFromSample(parent))
                        .build();

                return pa.getObjectsWithoutAccess().isEmpty();
            }
        }

        return false;
    }

    @Override
    public void init(IAuthorizationDataProvider authorizationDataProvider)
    {
        provider = authorizationDataProvider;
    }
}
