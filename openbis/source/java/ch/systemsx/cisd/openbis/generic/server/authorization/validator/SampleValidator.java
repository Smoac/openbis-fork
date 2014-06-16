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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A {@link IValidator} implementation suitable for {@link Sample}.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleValidator extends AbstractValidator<Sample>
{
    private final IValidator<Space> spaceValidator;

    public SampleValidator()
    {
        spaceValidator = new SpaceValidator();
    }

    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Sample value)
    {
        final Space space = value.getSpace();

        if (space != null)
        {
            return matchesSpace(person, space);
        } else
        {

            for (RoleAssignmentPE assignment : person.getRoleAssignments())
            {
                if (assignment.getSpace() == null)
                {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean matchesSpace(PersonPE person, Space space)
    {
        return spaceValidator.isValid(person, space);
    }
}
