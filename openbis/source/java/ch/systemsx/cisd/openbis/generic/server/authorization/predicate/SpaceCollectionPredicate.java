/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IPredicate} based on a list of space codes.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@ShouldFlattenCollections(value = false)
public class SpaceCollectionPredicate extends AbstractSpacePredicate<List<String>>
{
    @Override
    public String getCandidateDescription()
    {
        return "space code";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, List<String> spaceCodes)
    {
        assert initialized : "Predicate has not been initialized";
    
        for (String spaceCode : spaceCodes)
        {
            Status result = evaluate(allowedRoles, person, spaceCode);
            if (result != Status.OK)
            {
                return result;
            }
        }

        return Status.OK;
    }
}
