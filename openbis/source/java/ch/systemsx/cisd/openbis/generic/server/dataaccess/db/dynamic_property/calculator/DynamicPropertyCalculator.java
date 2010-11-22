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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.openbis.generic.shared.calculator.AbstractCalculator;

/**
 * Calculator for dynamic properties.
 * <p>
 * Variables : <code>entity<code> of type {@link IEntityAdaptor}
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyCalculator extends AbstractCalculator
{

    private static final String ENTITY_VARIABLE_NAME = "entity";

    private static final String INVOKE_CALCULATE_EXPR = "calculate()";

    /**
     * Creates a calculator for given <code>expression</code>.
     * <p>
     * Result of the calculation depends on whether the expression is multiline or not. Returned
     * value will be equal to:
     * <ul>
     * <li>result of calculation of a *single line* expression
     * <li>result of invocation of 'calculate()' function for a *multiline* expression
     * </ul>
     */
    public static DynamicPropertyCalculator create(String expression)
    {
        String calculatedExpression = expression;
        String initialScript = getBasicInitialScript();
        initialScript += importFunctions(DynamicPropertyFunctions.class) + NEWLINE;
        if (Evaluator.isMultiline(expression))
        {
            initialScript += expression;
            calculatedExpression = INVOKE_CALCULATE_EXPR;
        }
        return new DynamicPropertyCalculator(new Evaluator(calculatedExpression, Math.class,
                initialScript));
    }

    private DynamicPropertyCalculator(Evaluator evaluator)
    {
        super(evaluator);
    }

    public void setEntity(IEntityAdaptor entity)
    {
        evaluator.set(ENTITY_VARIABLE_NAME, entity);
    }

}
