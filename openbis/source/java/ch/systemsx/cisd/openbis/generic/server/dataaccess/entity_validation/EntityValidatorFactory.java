/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Pawel Glyzewski
 */
public class EntityValidatorFactory
{
    public static IEntityValidator createEntityValidator(EntityTypePE entityTypePE,
            IValidationRequestDelegate<INonAbstractEntityAdapter> validationRequestedDelegate)
    {
        ScriptPE scriptPE = entityTypePE.getValidationScript();

        if (scriptPE != null)
        {
            IEntityValidator validator = new JythonEntityValidator(scriptPE);
            validator.init(validationRequestedDelegate);
            return validator;
        }

        return null;
    }
}
