/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * Class for evaluating scripts that control managed properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ManagedPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ManagedPropertyEvaluator.class);

    private final ScriptPE scriptPE;

    /**
     * The name of the script that expects the property to be there and updates it.
     */
    private static final String CONFIGURE_OUTPUT_EXPRESSION = "configure_output()";

    private static final String PROPERTY_VARIABLE_NAME = "property";

    public ManagedPropertyEvaluator(ScriptPE scriptPE)
    {
        this.scriptPE = scriptPE;
    }

    public void evalConfigureProperty(ManagedEntityProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property '%s'.", managedProperty));
        }

        Evaluator evaluator =
                new Evaluator(CONFIGURE_OUTPUT_EXPRESSION, ScriptUtilityFactory.class,
                        scriptPE.getScript());
        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.eval();
    }

    public static class ScriptUtilityFactory
    {
        static public TypedTableModelBuilder<ISerializable> createTableBuilder()
        {
            return new TypedTableModelBuilder<ISerializable>();
        }
    }
}
