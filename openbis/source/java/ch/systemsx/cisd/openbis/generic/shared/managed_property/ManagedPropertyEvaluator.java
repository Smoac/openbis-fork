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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;

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
     * The name of the function that creates ui description
     */
    private static final String CONFIGURE_UI_FUNCTION = "configureUI";

    /**
     * The name of the function that updates the value of managed property from ui input fields
     */
    private static final String UPDATE_FROM_UI_FUNCTION = "updateFromUI";

    /**
     * The name of the function that returns an array of column names.
     */
    private static final String BATCH_COLUMN_NAMES_FUNCTION = "batchColumnNames";

    /**
     * The name of the function that expects a map of bindings.
     */
    private static final String UPDATE_FROM_BATCH_INPUT_FUNCTION = "updateFromBatchInput";

    private static final String PROPERTY_VARIABLE_NAME = "property";

    /**
     * Asserts that for all specified batch column names bindings are specified. If the list of
     * column names is empty the value should be bound at an empty string.
     * 
     * @param propertyTypeCode Property type code. Only needed for error messages.
     */
    public static void assertBatchColumnNames(String propertyTypeCode, List<String> columnNames,
            Map<String, String> bindings)
    {
        Set<String> names = bindings.keySet();
        if (columnNames.isEmpty())
        {
            if (names.contains("") == false)
            {
                throw new UserFailureException("No subcolumns expected for property '"
                        + propertyTypeCode + "': " + names);
            }
        } else
        {
            List<String> missingColumns = new ArrayList<String>();
            for (String columnName : columnNames)
            {
                if (names.contains(columnName) == false)
                {
                    missingColumns.add(propertyTypeCode + ":" + columnName);
                }
            }
            if (missingColumns.isEmpty() == false)
            {
                throw new UserFailureException("Following columns are missed: " + missingColumns);
            }
        }
    }

    private final Evaluator evaluator;

    private final List<String> columnNames;

    private final boolean updateFromBatchFunctionDefined;

    public ManagedPropertyEvaluator(String scriptExpression)
    {
        evaluator = new Evaluator("", ScriptUtilityFactory.class, scriptExpression);
        updateFromBatchFunctionDefined = evaluator.hasFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION);
        List<String> names = new ArrayList<String>();
        if (evaluator.hasFunction(BATCH_COLUMN_NAMES_FUNCTION))
        {
            if (updateFromBatchFunctionDefined == false)
            {
                throw new EvaluatorException("Function '" + BATCH_COLUMN_NAMES_FUNCTION
                        + "' defined but not '" + UPDATE_FROM_BATCH_INPUT_FUNCTION + "'.");
            }
            Object result = evaluator.evalFunction(BATCH_COLUMN_NAMES_FUNCTION);
            if (result instanceof List == false)
            {
                throw new EvaluatorException("Function '" + BATCH_COLUMN_NAMES_FUNCTION
                        + "' doesn't return a List but an object of type '"
                        + result.getClass().getName() + "': " + result);
            }
            List<?> list = (List<?>) result;
            List<String> notUpperCaseNames = new ArrayList<String>();
            for (Object element : list)
            {
                String columnName = element.toString();
                if (columnName.toUpperCase().equals(columnName) == false)
                {
                    notUpperCaseNames.add(columnName);
                }
                names.add(columnName);
            }
            if (notUpperCaseNames.isEmpty() == false)
            {
                throw new EvaluatorException(
                        "The following batch column names as returned by function '"
                                + BATCH_COLUMN_NAMES_FUNCTION + "' are not in upper case: "
                                + notUpperCaseNames);
            }
        }
        columnNames = Collections.unmodifiableList(names);
    }

    public void configureUI(IManagedProperty managedProperty)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property ui configuration'%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.evalFunction(CONFIGURE_UI_FUNCTION);
    }

    public void updateFromUI(IManagedProperty managedProperty, IManagedUiAction action)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property value update '%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.evalFunction(UPDATE_FROM_UI_FUNCTION, action);
    }

    public List<String> getBatchColumnNames()
    {
        return columnNames;
    }

    public void updateFromBatchInput(IManagedProperty managedProperty, Map<String, String> bindings)
    {
        assertBatchColumnNames(managedProperty.getPropertyTypeCode(), columnNames, bindings);
        if (updateFromBatchFunctionDefined == false)
        {
            managedProperty.setValue(bindings.get(""));
        } else
        {
            evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
            evaluator.evalFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION, bindings);
        }
    }

}
