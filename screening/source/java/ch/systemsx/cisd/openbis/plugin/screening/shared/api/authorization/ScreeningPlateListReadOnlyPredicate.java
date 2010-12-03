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
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * A predicate for lists of entities which have {@link PlateIdentifier} as their super-class. This
 * predicate authorizes for read-only access, i.e. it will allow access to shared samples for all
 * users.
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class ScreeningPlateListReadOnlyPredicate extends
        AbstractSpacePredicate<List<? extends PlateIdentifier>>
{

    @Override
    public String getCandidateDescription()
    {
        return "plate";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<? extends PlateIdentifier> plates)
    {
        for (PlateIdentifier plate : plates)
        {
            if (plate.getPermId() != null)
            {
                final SamplePE sampleOrNull =
                        authorizationDataProvider.tryGetSampleByPermId(plate.getPermId());
                if (sampleOrNull == null)
                {
                    return Status.createError(String.format(
                            "User '%s' does not have enough privileges.", person.getUserId()));
                }
                final SpacePE space = sampleOrNull.getSpace();
                final Status status =
                        evaluate(person, allowedRoles, space.getDatabaseInstance(), space.getCode());
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }

            final String spaceCode = SpaceCodeHelper.getSpaceCode(person, plate.tryGetSpaceCode());
            if (plate.isSharedPlate() == false)
            {
                final Status status =
                        evaluate(person, allowedRoles, authorizationDataProvider
                                .getHomeDatabaseInstance(), spaceCode);
                if (Status.OK.equals(status) == false)
                {
                    return status;
                }
            }
        }
        return Status.OK;
    }

}
