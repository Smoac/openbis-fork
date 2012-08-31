/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.calculator.AbstractCalculator;

/**
 * @author Jakub Straszewski
 */
public class EntityValidationCalculator extends AbstractCalculator
{
    private static final String ENTITY_VARIABLE_NAME = "__entity";

    private static final String IS_NEW_ENTITY_VARIABLE_NAME = "__isNewEntity";

    private static final String INVOKE_CALCULATE_EXPR = "validate(" + ENTITY_VARIABLE_NAME + ", "
            + IS_NEW_ENTITY_VARIABLE_NAME + ")";

    private static final String REQUEST_DELEGATE_VARIABLE = "__entityValidationRequestDelegate";

    private static final String VALIDATION_REQUEST_FUNCTION = "def requestValidation(entity):\n  "
            + REQUEST_DELEGATE_VARIABLE + ".requestValidation(entity)\n";

    public interface IValidationRequestDelegate
    {
        public void requestValidation(Object o);
    }

    private IValidationRequestDelegate validationRequested;

    /**
     * Creates a calculator for given <code>expression</code>.
     * <p>
     * The script is expected to contain validate method with two parameters: "entity" and
     * "isNewEntity"
     */
    public static EntityValidationCalculator create(String expression,
            IValidationRequestDelegate validationRequestedDelegate)
    {
        String initialScript = getBasicInitialScript();
        initialScript += importFunctions(EntityValidationCalculator.class) + NEWLINE;
        initialScript += VALIDATION_REQUEST_FUNCTION + NEWLINE;
        initialScript += expression;
        String calculatedExpression = INVOKE_CALCULATE_EXPR;
        return new EntityValidationCalculator(new Evaluator(calculatedExpression, Math.class,
                initialScript), validationRequestedDelegate);
    }

    public EntityValidationCalculator(Evaluator evaluator,
            IValidationRequestDelegate validationRequested)
    {
        super(evaluator);
        evaluator.set(REQUEST_DELEGATE_VARIABLE, validationRequested);
    }

    public void setEntity(IEntityAdaptor entity)
    {
        evaluator.set(ENTITY_VARIABLE_NAME, entity);
    }

    public void setIsNewEntity(boolean isNewEntity)
    {
        evaluator.set(IS_NEW_ENTITY_VARIABLE_NAME, isNewEntity);
    }
}
