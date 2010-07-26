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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractGroupPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;

/**
 * A predicate for {@link ExperimentIdentifier}.
 * 
 * @author Bernd Rinn
 */
public class ExperimentIdentifierPredicate extends AbstractGroupPredicate<ExperimentIdentifier>
{

    @Override
    public String getCandidateDescription()
    {
        return "experiment";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            ExperimentIdentifier value)
    {
        if (value.getPermId() != null)
        {
            final ExperimentPE experimentOrNull =
                    authorizationDataProvider.tryGetExperimentByPermId(value.getPermId());
            if (experimentOrNull == null)
            {
                return Status.createError(String.format(
                        "User '%s' does not have enough privileges to "
                                + "access experiment with permId='%s'", person.getUserId(), value
                                .getPermId()));
            }
            final GroupPE space = experimentOrNull.getProject().getGroup();
            return evaluate(person, allowedRoles, space.getDatabaseInstance(), space.getCode());
        }

        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, value.getSpaceCode());
        return evaluate(person, allowedRoles, authorizationDataProvider.getHomeDatabaseInstance(),
                spaceCode);
    }

}
