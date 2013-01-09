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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Pawel Glyzewski
 */
public class JythonEntityValidator extends AbstractEntityValidator implements IEntityValidator
{
    private final ScriptPE script;

    public JythonEntityValidator(ScriptPE script)
    {
        this.script = script;
    }

    @Override
    public String validate(IEntityAdaptor entity, boolean isNew)
    {
        JythonEntityValidationCalculator calculator =
                JythonEntityValidationCalculator.create(script.getScript(),
                        validationRequestedDelegate);
        calculator.setEntity(entity);
        calculator.setIsNewEntity(isNew);

        return calculator.evalAsString();
    }
}
