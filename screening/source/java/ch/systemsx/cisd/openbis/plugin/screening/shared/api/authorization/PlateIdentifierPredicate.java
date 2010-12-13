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
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * A predicate for {@link PlateIdentifier}.
 * 
 * @author Piotr Buczek
 */
public class PlateIdentifierPredicate extends AbstractSpacePredicate<PlateIdentifier>
{

    @Override
    public String getCandidateDescription()
    {
        return "experiment";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            PlateIdentifier value)
    {
        // TODO 2010-12-13, Piotr Buczek: implement dealing with shared plates and permIds
        if (value.getPermId() != null)
        {
            return null;
        }

        final String spaceCode = value.tryGetSpaceCode();
        return evaluate(person, allowedRoles, authorizationDataProvider.getHomeDatabaseInstance(),
                spaceCode);
    }

}
