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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedComboBoxInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedInputFieldType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedOutputWidgetType;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;

/**
 * @author Franz-Josef Elmer
 * @author Piotr Buczek
 */
public class ManagedPropertyEvaluatorTest extends AssertJUnit
{
    @Test
    public void testEmptyScript()
    {
        assertEquals(0, new ManagedPropertyEvaluator("").getBatchColumnNames().size());
    }

    @Test
    public void testScriptWithSyntaxError()
    {
        try
        {
            new ManagedPropertyEvaluator("a =");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("SyntaxError: ('invalid syntax', ('<string>', 1, 4, 'a ='))",
                    ex.getMessage());
        }
    }

    @Test
    public void testConfigureUIOutput()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        managedProperty.setOwnTab(false);
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def configureUI():\n"
                        + "    tableBuilder = createTableBuilder()\n" + "\n"
                        + "    tableBuilder.addHeader('column1')\n"
                        + "    tableBuilder.addHeader('column2')\n"
                        + "    tableBuilder.addHeader('column3')\n" + "\n"
                        + "    row1 = tableBuilder.addRow()\n"
                        + "    row1.setCell('column1','v1')\n" + "    row1.setCell('column2', 1)\n"
                        + "    row1.setCell('column3', 1.5)\n" + "\n"
                        + "    row2 = tableBuilder.addRow()\n"
                        + "    row2.setCell('column1','v2')\n" + "    row2.setCell('column2', 2)\n"
                        + "    row2.setCell('column3', 2.5)\n"
                        + "    row3 = tableBuilder.addRow()\n"
                        + "    row3.setCell('column1','v3')\n" + "\n"
                        + "    property.setOwnTab(True)\n"
                        + "    uiDesc = property.getUiDescription()\n"
                        + "    uiDesc.useTableOutput(tableBuilder.getTableModel())");
        evaluator.configureUI(managedProperty);
        assertEquals(true, managedProperty.isOwnTab());
        IManagedOutputWidgetDescription outputWidgetDescripion =
                managedProperty.getUiDescription().getOutputWidgetDescription();
        assertNotNull(outputWidgetDescripion);
        assertEquals(ManagedOutputWidgetType.TABLE,
                outputWidgetDescripion.getManagedOutputWidgetType());
        if (outputWidgetDescripion instanceof ManagedTableWidgetDescription)
        {
            ManagedTableWidgetDescription tableDescription =
                    (ManagedTableWidgetDescription) outputWidgetDescripion;
            TableModel tableModel = tableDescription.getTableModel();
            assertNotNull(tableModel);

            assertEquals(3, tableModel.getHeader().size());
            assertEquals("column1", tableModel.getHeader().get(0).getId());
            assertEquals("column2", tableModel.getHeader().get(1).getId());
            assertEquals("column3", tableModel.getHeader().get(2).getId());

            assertEquals(3, tableModel.getRows().size());
            assertEquals(SimpleTableModelBuilder.asText("v1"), tableModel.getRows().get(0)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asInteger(1), tableModel.getRows().get(0)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asDouble(1.5), tableModel.getRows().get(0)
                    .getValues().get(2));

            assertEquals(SimpleTableModelBuilder.asText("v2"), tableModel.getRows().get(1)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asInteger(2), tableModel.getRows().get(1)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asDouble(2.5), tableModel.getRows().get(1)
                    .getValues().get(2));

            assertEquals(SimpleTableModelBuilder.asText("v3"), tableModel.getRows().get(2)
                    .getValues().get(0));
            assertEquals(SimpleTableModelBuilder.asText(null), tableModel.getRows().get(2)
                    .getValues().get(1));
            assertEquals(SimpleTableModelBuilder.asText(null), tableModel.getRows().get(2)
                    .getValues().get(2));
        } else
        {
            fail("expected instance of " + ManagedTableWidgetDescription.class.getSimpleName()
                    + ", got " + outputWidgetDescripion.getClass().getSimpleName());
        }
    }

    @Test
    public void testConfigureUIInput()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        managedProperty.setOwnTab(false);
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator(
                        "def configureUI():\n"
                                + "    uiDesc = property.getUiDescription()\n"
                                + "    uiDesc.addTextInputField('t1')\n"
                                + "    uiDesc.addTextInputField('t2').setValue('default 2')\n"
                                + "    uiDesc.addTextInputField('t3').setDescription('description 3')\n"
                                + "    uiDesc.addMultilineTextInputField('multi').setValue('default m').setDescription('multiline')\n"
                                + "    uiDesc.addComboBoxInputField('combo', ['v1', 'v2', 'v3']).setMandatory(True).setDescription('select from list')\n");
        evaluator.configureUI(managedProperty);
        assertEquals(false, managedProperty.isOwnTab());

        List<IManagedInputWidgetDescription> inputWidgets =
                managedProperty.getUiDescription().getInputWidgetDescriptions();
        assertEquals(5, inputWidgets.size());
        checkInputFieldWidget(inputWidgets.get(0), ManagedInputFieldType.TEXT, "t1", null, null,
                false);
        checkInputFieldWidget(inputWidgets.get(1), ManagedInputFieldType.TEXT, "t2", "default 2",
                null, false);
        checkInputFieldWidget(inputWidgets.get(2), ManagedInputFieldType.TEXT, "t3", null,
                "description 3", false);
        checkInputFieldWidget(inputWidgets.get(3), ManagedInputFieldType.MULTILINE_TEXT, "multi",
                "default m", "multiline", false);
        checkInputFieldWidget(inputWidgets.get(4), ManagedInputFieldType.COMBO_BOX, "combo", null,
                "select from list", true);
        if (inputWidgets.get(4) instanceof ManagedComboBoxInputWidgetDescription)
        {
            ManagedComboBoxInputWidgetDescription comboBox =
                    (ManagedComboBoxInputWidgetDescription) inputWidgets.get(4);
            assertEquals("[v1, v2, v3]", comboBox.getOptions().toString());
        } else
        {
            fail("expected instance of "
                    + ManagedComboBoxInputWidgetDescription.class.getSimpleName() + ", got "
                    + inputWidgets.get(4).getClass().getSimpleName());
        }
    }

    @Test
    public void testUpdateFromUI()
    {
        IManagedProperty managedProperty = new ManagedProperty();
        IManagedUiDescription uiDescription = managedProperty.getUiDescription();
        uiDescription.addTextInputField("t1");
        uiDescription.addTextInputField("t2").setValue("v2");
        uiDescription.addMultilineTextInputField("multi").setValue("multi\nline\ninput");
        uiDescription.addComboBoxInputField("combo", new String[]
            { "cv1", "cv2", "cv3" }).setValue("cv1");
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator(
                        "def updateFromUI():\n"
                                + "    value = ''\n"
                                + "    for input in property.getUiDescription().getInputWidgetDescriptions():\n"
                                + "        inputValue = input.getValue();\n"
                                + "        if inputValue is None:\n "
                                + "            inputValue = 'null'\n"
                                + "        value = value + input.getLabel() + '=' + inputValue + '|'\n"
                                + "    property.setValue(value)\n");
        evaluator.updateFromUI(managedProperty);

        assertNotNull(managedProperty.getValue());
        String[] inputTokens = managedProperty.getValue().split("\\|");
        System.err.println(Arrays.toString(inputTokens));
        assertEquals("t1=null", inputTokens[0]);
        assertEquals("t2=v2", inputTokens[1]);
        assertEquals("multi=multi\nline\ninput", inputTokens[2]);
        assertEquals("combo=cv1", inputTokens[3]);
    }

    private void checkInputFieldWidget(IManagedInputWidgetDescription widget,
            ManagedInputFieldType expectedType, String expectedLabel, String expectedDefaultValue,
            String expectedDescription, boolean expectedMandatory)
    {
        assertEquals(expectedType, widget.getManagedInputFieldType());
        assertEquals(expectedLabel, widget.getLabel());
        assertEquals(expectedDefaultValue, widget.getValue());
        assertEquals(expectedDescription, widget.getDescription());
        assertEquals(expectedMandatory, widget.isMandatory());
    }

    @Test
    public void testAssertBatchColumnNames()
    {
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "");
        ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String> asList(), bindings);
        bindings.clear();
        bindings.put("A", "alpha");
        try
        {
            ManagedPropertyEvaluator
                    .assertBatchColumnNames("p", Arrays.<String> asList(), bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No subcolumns expected for property 'p': [A]", ex.getMessage());
        }
        try
        {
            ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String> asList("A", "B"),
                    bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Following columns are missed: [p:B]", ex.getMessage());
        }
        bindings.put("B", "beta");
        ManagedPropertyEvaluator.assertBatchColumnNames("p", Arrays.<String> asList("A", "B"),
                bindings);
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionButMissingUpdateFromBatchFunction()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A']");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' defined but not 'updateFromBatchInput'.",
                    ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichDoesNotReturnAList()
    {
        try
        {
            new ManagedPropertyEvaluator("def batchColumnNames():\n return 42\n"
                    + "def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("Function 'batchColumnNames' doesn't return a List "
                    + "but an object of type 'java.lang.Long': 42", ex.getMessage());
        }
    }

    @Test
    public void testScriptWithBatchColumnNamesFunctionWhichReturnLowerCaseNames()
    {
        try
        {
            new ManagedPropertyEvaluator(
                    "def batchColumnNames():\n return ['abc', 'A', 'e42', 42]\n"
                            + "def updateFromBatchInput():\n  None");
            fail("EvaluatorException expected");
        } catch (EvaluatorException ex)
        {
            assertEquals("The following batch column names "
                    + "as returned by function 'batchColumnNames' "
                    + "are not in upper case: [abc, e42]", ex.getMessage());
        }
    }

    @Test
    public void testGetBatchColumnNamesScript()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 42]\n"
                        + "def updateFromBatchInput():\n  None");
        assertEquals("[A, 42]", evaluator.getBatchColumnNames().toString());
    }

    @Test
    public void testUpdateFromBatchInputCallsAssertBatchColumnNames()
    {
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator("");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("A", "42");

        try
        {
            evaluator.updateFromBatchInput(property, bindings);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No subcolumns expected for property 'p': [A]", ex.getMessage());
        }
    }

    @Test
    public void testUpdateFromBatchInputWithNoScript()
    {
        ManagedPropertyEvaluator evaluator = new ManagedPropertyEvaluator("");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

    @Test
    public void testUpdateFromBatchInputWithNoColumns()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def updateFromBatchInput(bindings):\n"
                        + "  property.setValue(bindings.get(''))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("", "42");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

    @Test
    public void testUpdateFromBatchInputWithColumns()
    {
        ManagedPropertyEvaluator evaluator =
                new ManagedPropertyEvaluator("def batchColumnNames():\n return ['A', 'B']\n"
                        + "def updateFromBatchInput(bindings):\n"
                        + "  property.setValue(bindings.get('A') + bindings.get('B'))");
        ManagedProperty property = new ManagedProperty();
        property.setPropertyTypeCode("p");
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("A", "4");
        bindings.put("B", "2");

        evaluator.updateFromBatchInput(property, bindings);

        assertEquals("42", property.getValue());
    }

}
