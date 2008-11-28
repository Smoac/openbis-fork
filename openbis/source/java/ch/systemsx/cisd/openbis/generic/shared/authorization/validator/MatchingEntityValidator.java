/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A {@link IValidator} implementation suitable for {@link IMatchingEntity}.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntityValidator extends AbstractValidator<SearchHit>
{
    private final IValidator<GroupPE> groupValidator;

    public MatchingEntityValidator()
    {
        groupValidator = new GroupValidator();
    }

    //
    // AbstractValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final SearchHit hit)
    {
        return doValidation(person, hit.getEntity());
    }

    private boolean doValidation(final PersonPE person, final IMatchingEntity entity)
    {
        final EntityKind entityKind = entity.getEntityKind();
        if (entityKind == EntityKind.EXPERIMENT)
        {
            final ExperimentPE experiment = (ExperimentPE) entity;
            return groupValidator.isValid(person, experiment.getProject().getGroup());
        } else if (entityKind == EntityKind.SAMPLE)
        {
            final SamplePE sample = (SamplePE) entity;
            final SampleIdentifier sampleIdentifier = sample.getSampleIdentifier();
            // Everyone can read from the database instance level.
            if (sampleIdentifier.isGroupLevel())
            {
                return groupValidator.isValid(person, sample.getGroup());
            }
        }
        return true;
    }
}
