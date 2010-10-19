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
import ch.systemsx.cisd.common.evaluator.EvaluatorException;

/**
 * @author Piotr Buczek
 */
public class DynamicPropertyCalculator
{
    private static final String ENTITY_VARIABLE_NAME = "entity";

    private static final String BASIC_INITIAL_SCRIPT = "from "
            + StandardFunctions.class.getCanonicalName() + " import *\n"
            + "def int(x):return toInt(x)\n" + "def float(x):return toFloat(x)\n";

    private final Evaluator evaluator;

    public DynamicPropertyCalculator(String expression)
    {
        this.evaluator = new Evaluator(expression, Math.class, BASIC_INITIAL_SCRIPT);
    }

    public void setEntity(IEntityAdaptor entity)
    {
        evaluator.set(ENTITY_VARIABLE_NAME, entity);
    }

    public String evalAsString() throws EvaluatorException
    {
        return evaluator.evalAsString();
    }

    public Object eval() throws EvaluatorException
    {
        return evaluator.eval();
    }

}
