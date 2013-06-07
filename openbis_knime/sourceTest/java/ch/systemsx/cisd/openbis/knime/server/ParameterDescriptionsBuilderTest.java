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

package ch.systemsx.cisd.openbis.knime.server;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ParameterDescriptionsBuilderTest extends AssertJUnit
{
    private Mockery context;

    private ISimpleTableModelBuilderAdaptor tableBuilder;

    private IRowBuilderAdaptor rowBuilder;

    private ParameterDescriptionsBuilder builder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        tableBuilder = context.mock(ISimpleTableModelBuilderAdaptor.class);
        rowBuilder = context.mock(IRowBuilderAdaptor.class);
        context.checking(new Expectations()
            {
                {
                    one(tableBuilder).addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
                    one(tableBuilder).addHeader(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN);
                }
            });
        builder = new ParameterDescriptionsBuilder(tableBuilder);
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddParameter()
    {
        prepareAddRow("Name");
        
        builder.parameter("Name");
        
        context.assertIsSatisfied();
    }

    @Test
    public void testAddParameterOfTypeText()
    {
        prepareAddRow("Variable");
        prepareSetType(FieldType.VARCHAR);
        
        builder.parameter("Variable").text();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddParameterOfTypeExperiment()
    {
        prepareAddRow("Variable");
        prepareSetType(FieldType.EXPERIMENT);
        
        builder.parameter("Variable").experiment();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddParameterOfTypeSample()
    {
        prepareAddRow("Variable");
        prepareSetType(FieldType.SAMPLE);
        
        builder.parameter("Variable").sample();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddParameterOfTypeDataSet()
    {
        prepareAddRow("Variable");
        prepareSetType(FieldType.DATA_SET);
        
        builder.parameter("Variable").dataSet();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddSameParameterTwice()
    {
        prepareAddRow("Variable");
        builder.parameter("Variable");
        
        try
        {
            builder.parameter("Variable");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("There is already a parameter with name 'Variable'.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    private void prepareAddRow(final String parameterName)
    {
        context.checking(new Expectations()
            {
                {
                    one(tableBuilder).addRow();
                    will(returnValue(rowBuilder));
                    
                    one(rowBuilder).setCell(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN, parameterName);
                }
            });
    }
    
    private void prepareSetType(final FieldType fieldType)
    {
        context.checking(new Expectations()
            {
                {
                    one(rowBuilder).setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, fieldType.toString());
                }
            });
    }

}
