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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;

/**
 * Class for evaluating scripts that control managed properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ManagedPropertyEvaluator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ManagedPropertyEvaluator.class);

    /**
     * The name of the script that expects the property to be there and updates ui configuration.
     */
    private static final String CONFIGURE_UI_EXPRESSION = "configureUI";

    /**
     * The name of the script that expects the property to be there and updates the value.
     */
    private static final String UPDATE_VALUE_EXPRESSION = "updateValue";

    /**
     * The name of the function that returns an array of column names.
     */
    private static final String BATCH_COLUMN_NAMES_FUNCTION = "batchColumnNames";
    
    /**
     * The name of the function that expects a map of bindings.
     */
    private static final String UPDATE_FROM_BATCH_INPUT_FUNCTION = "updateFromBatchInput";
    
    private static final String PROPERTY_VARIABLE_NAME = "property";
    
    private final Evaluator evaluator;

    public ManagedPropertyEvaluator(String scriptExpression)
    {
        evaluator = new Evaluator("", ScriptUtilityFactory.class, scriptExpression);
    }

    public void evalConfigureProperty(IManagedProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property ui configuration'%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.evalFunction(CONFIGURE_UI_EXPRESSION);
    }

    public void evaluateUpdateProperty(IManagedProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property value update '%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.evalFunction(UPDATE_VALUE_EXPRESSION);
    }
    
    public void assertBatchColumnNamesAreUppercase()
    {
        if (hasBatchColumnNamesFunction())
        {
            
            List<String> batchColumnNames = getBatchColumnNames();
            List<String> notUpperCaseNames = new ArrayList<String>();
            for (String name : batchColumnNames)
            {
                if (name.toUpperCase().equals(name) == false)
                {
                    notUpperCaseNames.add(name);
                }
            }
            if (notUpperCaseNames.isEmpty() == false)
            {
                throw new EvaluatorException(
                        "The following batch column names as returned by function '"
                                + BATCH_COLUMN_NAMES_FUNCTION + "' are not in uupper case: "
                                + notUpperCaseNames);
            }
        }
    }
    
    public boolean hasBatchColumnNamesFunction()
    {
        return evaluator.hasFunction(BATCH_COLUMN_NAMES_FUNCTION);
    }
    
    public List<String> getBatchColumnNames()
    {
        Object result = evaluator.evalFunction(BATCH_COLUMN_NAMES_FUNCTION);
        if (result instanceof List == false)
        {
            throw new EvaluatorException("Function '" + BATCH_COLUMN_NAMES_FUNCTION
                    + "' doesn't return a List but an object of type '"
                    + result.getClass().getName() + "': " + result);
        }
        List<?> list = (List<?>) result;
        ArrayList<String> columnNames = new ArrayList<String>();
        for (Object element : list)
        {
            columnNames.add(element.toString());
        }
        return columnNames;
    }

    public String updateFromBatchInput(Map<String, String> bindings)
    {
        ManagedProperty property = new ManagedProperty();
        evaluator.set(PROPERTY_VARIABLE_NAME, property);
        evaluator.evalFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION, bindings);
        return property.getValue();
    }

}
