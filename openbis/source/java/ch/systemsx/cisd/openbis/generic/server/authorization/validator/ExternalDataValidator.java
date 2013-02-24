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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IValidator} implementation suitable for {@link AbstractExternalData}.
 * 
 * @author Tomasz Pylak
 */
public final class ExternalDataValidator extends AbstractValidator<AbstractExternalData>
{
    private final IValidator<Space> groupValidator;

    private final IValidator<AbstractExternalData> storageConfirmedValidator;

    public ExternalDataValidator()
    {
        groupValidator = new SpaceValidator();

        storageConfirmedValidator = new StorageConfirmedForAdminValidator();
    }

    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final AbstractExternalData value)
    {
        final Space space = value.getExperiment().getProject().getSpace();
        return groupValidator.isValid(person, space)
                && storageConfirmedValidator.isValid(person, value);
    }
}
