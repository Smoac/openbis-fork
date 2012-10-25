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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.jython.evaluator.Evaluator;
import ch.systemsx.cisd.common.jython.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiActionDescriptionFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;

/**
 * Class for evaluating scripts that control managed properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ManagedPropertyEvaluator
{
    private static final class UniqunessChecker
    {
        private final Set<String> codes = new HashSet<String>();

        private final String type;

        private final String codeName;

        UniqunessChecker(String type, String codeName)
        {
            this.type = type;
            this.codeName = codeName;
        }

        void check(String code)
        {
            if (codes.add(code) == false)
            {
                throw new EvaluatorException("There is already " + type + " with " + codeName
                        + ": " + code);
            }
        }
    }

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
     * The name of the function that returns an array of @IManagedInputWidgetDescription.
     */
    private static final String INPUT_WIDGETS_FUNCTION = "inputWidgets";

    /**
     * The name of the function that returns <code>true</code> if the raw value should be shown in
     * forms.
     */
    private static final String SHOW_RAW_VALUE_FUNCTION = "showRawValueInForms";

    /**
     * The name of the function that expects a map of bindings.
     */
    private static final String UPDATE_FROM_BATCH_INPUT_FUNCTION = "updateFromBatchInput";

    private static final String PROPERTY_VARIABLE_NAME = "property";

    private static final String PROPERTY_PE_VARIABLE_NAME = "propertyPE";

    private static final String PERSON_VARIABLE_NAME = "person";

    private final Evaluator evaluator;

    private final List<String> columnNames;

    private final boolean updateFromBatchFunctionDefined;

    private final boolean showRawValueInForms;

    private List<IManagedInputWidgetDescription> inputWidgetDescriptions;

    public ManagedPropertyEvaluator(String scriptExpression)
    {
        evaluator = new Evaluator("", ManagedPropertyFunctions.class, scriptExpression);
        updateFromBatchFunctionDefined = evaluator.hasFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION);
        boolean batchColumnNamesFunctionDefined =
                evaluator.hasFunction(BATCH_COLUMN_NAMES_FUNCTION);
        boolean inputWidgetsFunctionDefined = evaluator.hasFunction(INPUT_WIDGETS_FUNCTION);
        showRawValueInForms = evalFunctionShowRawValue();
        checkCombinationsOfDefinedFunctions(batchColumnNamesFunctionDefined,
                inputWidgetsFunctionDefined);
        columnNames = new ArrayList<String>();
        inputWidgetDescriptions = new ArrayList<IManagedInputWidgetDescription>();
        if (inputWidgetsFunctionDefined)
        {
            List<?> widgets = evalFunction(INPUT_WIDGETS_FUNCTION);
            UniqunessChecker uniqunessChecker = new UniqunessChecker("an input widget", "code");
            for (int i = 0; i < widgets.size(); i++)
            {
                Object widget = widgets.get(i);
                if (widget == null)
                {
                    throw new EvaluatorException("Function " + INPUT_WIDGETS_FUNCTION
                            + " has returned a list where the " + (i + 1) + ". element is null.");
                }
                if (widget instanceof IManagedInputWidgetDescription == false)
                {
                    throw new EvaluatorException("Function " + INPUT_WIDGETS_FUNCTION
                            + " has returned a list where the " + (i + 1)
                            + ". element isn't of type "
                            + IManagedInputWidgetDescription.class.getName() + " but "
                            + widget.getClass().getName() + ".");
                }
                IManagedInputWidgetDescription widgetDescription =
                        (IManagedInputWidgetDescription) widget;
                if (inputWidgetsAllowed())
                {
                    inputWidgetDescriptions.add(widgetDescription);
                }
                if (batchColumnNamesFunctionDefined == false)
                {
                    String code = widgetDescription.getCode();
                    uniqunessChecker.check(code);
                    columnNames.add(code);
                }
            }
        }
        if (batchColumnNamesFunctionDefined)
        {
            List<?> list = evalFunction(BATCH_COLUMN_NAMES_FUNCTION);
            UniqunessChecker uniqunessChecker =
                    new UniqunessChecker("a batch column", "name in uppercase");
            ManagedUiActionDescriptionFactory descriptionFactory =
                    new ManagedUiActionDescriptionFactory();
            for (Object element : list)
            {
                String columnName = element.toString();
                String code = columnName.toUpperCase();
                uniqunessChecker.check(code);
                columnNames.add(code);
                if (inputWidgetsFunctionDefined == false && inputWidgetsAllowed())
                {
                    inputWidgetDescriptions
                            .add(descriptionFactory.createTextInputField(columnName));
                }
            }
        }
    }

    private boolean inputWidgetsAllowed()
    {
        return showRawValueInForms == false;
    }

    private void checkCombinationsOfDefinedFunctions(boolean batchColumnNamesFunctionDefined,
            boolean inputWidgetsFunctionDefined)
    {
        if ((batchColumnNamesFunctionDefined || inputWidgetsFunctionDefined)
                && updateFromBatchFunctionDefined == false)
        {
            StringBuilder builder = new StringBuilder("Function ");
            builder.append(UPDATE_FROM_BATCH_INPUT_FUNCTION);
            builder.append(" is not defined although function");
            boolean both = batchColumnNamesFunctionDefined && inputWidgetsFunctionDefined;
            builder.append(both ? "s " : " ");
            builder.append(batchColumnNamesFunctionDefined ? BATCH_COLUMN_NAMES_FUNCTION : "");
            builder.append(both ? " and " : "");
            builder.append(inputWidgetsFunctionDefined ? INPUT_WIDGETS_FUNCTION : "");
            builder.append(both ? " are defined." : " is defined.");
            throw new EvaluatorException(builder.toString());
        }
    }

    private boolean evalFunctionShowRawValue()
    {
        boolean showRawValueFunctionDefined = evaluator.hasFunction(SHOW_RAW_VALUE_FUNCTION);
        if (showRawValueFunctionDefined == false)
        {
            return false;
        }
        Object result = evaluator.evalFunction(SHOW_RAW_VALUE_FUNCTION);
        if (result instanceof Boolean == false)
        {
            throw new EvaluatorException("Function '" + SHOW_RAW_VALUE_FUNCTION
                    + "' doesn't return a boolean values but an object of type '"
                    + result.getClass().getName() + "'.");
        }
        return (Boolean) result;
    }

    private List<?> evalFunction(String functionName)
    {
        Object result = evaluator.evalFunction(functionName);
        if (result instanceof List == false)
        {
            throw new EvaluatorException("Function '" + functionName
                    + "' doesn't return a List but an object of type '"
                    + result.getClass().getName() + "': " + result);
        }
        return (List<?>) result;
    }

    public void configureUI(IManagedProperty managedProperty, EntityPropertyPE entityPropertyPE)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property ui configuration'%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.set(PROPERTY_PE_VARIABLE_NAME, entityPropertyPE);
        evaluator.evalFunction(CONFIGURE_UI_FUNCTION);
    }

    public void updateFromUI(IManagedProperty managedProperty, IPerson person,
            IManagedUiAction action)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Evaluating managed property value update '%s'.",
                    managedProperty));
        }

        evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
        evaluator.set(PERSON_VARIABLE_NAME, person);
        evaluator.evalFunction(UPDATE_FROM_UI_FUNCTION, action);
    }

    public boolean getShowRawValueInForms()
    {
        return showRawValueInForms;
    }

    public List<String> getBatchColumnNames()
    {
        return columnNames;
    }

    public List<IManagedInputWidgetDescription> getInputWidgetDescriptions()
    {
        return inputWidgetDescriptions;
    }

    public void updateFromBatchInput(IManagedProperty managedProperty, IPerson person,
            Map<String, String> bindings)
    {
        if (updateFromBatchFunctionDefined == false)
        {
            if (bindings.containsKey(""))
            {
                managedProperty.setValue(bindings.get(""));
            }
        } else
        {
            evaluator.set(PROPERTY_VARIABLE_NAME, managedProperty);
            evaluator.set(PERSON_VARIABLE_NAME, person);
            evaluator.evalFunction(UPDATE_FROM_BATCH_INPUT_FUNCTION, bindings);
        }
    }

}
