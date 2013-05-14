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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.internal.NamedSequence;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * @author Franz-Josef Elmer
 */
public class AggregationCommandTest extends AssertJUnit
{
    private static final class MockAggregationCommand extends AggregationCommand
    {
        private final Throwable aggregateThrowableOrNull;

        MockAggregationCommand(Throwable aggregateThrowableOrNull)
        {
            this.aggregateThrowableOrNull = aggregateThrowableOrNull;
        }

        private ParameterDescriptionsBuilder recordedParametersBuilder;

        private Map<String, Object> recordedParameters;

        private ISimpleTableModelBuilderAdaptor recordedTableBuilder;

        @Override
        protected void defineParameters(ParameterDescriptionsBuilder parameters)
        {
            this.recordedParametersBuilder = parameters;
        }

        @Override
        protected void aggregate(Map<String, Object> parameters, ISimpleTableModelBuilderAdaptor tableBuilder)
        {
            this.recordedParameters = parameters;
            this.recordedTableBuilder = tableBuilder;
            if (aggregateThrowableOrNull instanceof Error)
            {
                throw (Error) aggregateThrowableOrNull;
            }
            if (aggregateThrowableOrNull instanceof RuntimeException)
            {
                throw (RuntimeException) aggregateThrowableOrNull;
            }
        }
    }

    private Mockery context;

    private Map<String, Object> parameters;

    private ISimpleTableModelBuilderAdaptor tableBuilder;

    private IRowBuilderAdaptor rowBuilder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        tableBuilder = context.mock(ISimpleTableModelBuilderAdaptor.class);
        rowBuilder = context.mock(IRowBuilderAdaptor.class);
        parameters = new HashMap<String, Object>();
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefineParameters()
    {
        MockAggregationCommand command = new MockAggregationCommand(null);
        parameters.put(Constants.REQUEST_KEY, Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST);
        context.checking(new Expectations()
            {
                {
                    one(tableBuilder).addHeader(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN);
                    one(tableBuilder).addHeader(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN);

                    one(tableBuilder).addRow();
                    will(returnValue(rowBuilder));

                    one(rowBuilder).setCell(Constants.PARAMETER_DESCRIPTION_NAME_COLUMN, "Name");
                }
            });

        command.handleRequest(parameters, tableBuilder);
        command.recordedParametersBuilder.parameter("Name");

        assertEquals(null, command.recordedParameters);
        assertEquals(null, command.recordedTableBuilder);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAggregate()
    {
        MockAggregationCommand command = new MockAggregationCommand(null);
        
        command.handleRequest(parameters, tableBuilder);
        
        assertEquals(null, command.recordedParametersBuilder);
        assertSame(parameters, command.recordedParameters);
        assertSame(tableBuilder, command.recordedTableBuilder);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAggregateFails()
    {
        final IllegalArgumentException nestedException = new IllegalArgumentException();
        final RuntimeException exception = new RuntimeException("Oohps!", nestedException);
        MockAggregationCommand command = new MockAggregationCommand(exception);
        final List<TableModelColumnHeader> headers 
                = new ArrayList<TableModelColumnHeader>(Arrays.asList(new TableModelColumnHeader()));
        List<ISerializableComparable> row = Arrays.<ISerializableComparable> asList(new StringTableCell("hi"));
        final List<TableModelRow> rows = new ArrayList<TableModelRow>(Arrays.asList(new TableModelRow(row)));
        final NamedSequence sequence = new NamedSequence("table");
        context.checking(new Expectations()
            {
                {
                    one(tableBuilder).getTableModel();
                    will(returnValue(new TableModel(headers, rows)));
                    
                    one(tableBuilder).addHeader(Constants.EXCEPTION_COLUMN);
                    inSequence(sequence);
                    one(tableBuilder).addHeader(Constants.STACK_TRACE_CLASS_COLUMN);
                    inSequence(sequence);
                    one(tableBuilder).addHeader(Constants.STACK_TRACE_METHOD_NAME_COLUMN);
                    inSequence(sequence);
                    one(tableBuilder).addHeader(Constants.STACK_TRACE_FILE_NAME_COLUMN);
                    inSequence(sequence);
                    one(tableBuilder).addHeader(Constants.STACK_TRACE_LINE_NUMBER_COLUMN);
                    inSequence(sequence);
                    
                    one(tableBuilder).addFullRow(exception.toString(), "", "", "", "");
                    inSequence(sequence);
                    for (StackTraceElement element : exception.getStackTrace())
                    {
                        one(tableBuilder).addFullRow("", element.getClassName(), element.getMethodName(),
                                element.getFileName(), Integer.toString(element.getLineNumber()));
                        inSequence(sequence);
                    }
                    one(tableBuilder).addFullRow(nestedException.toString(), "", "", "", "");
                    inSequence(sequence);
                    for (StackTraceElement element : nestedException.getStackTrace())
                    {
                        one(tableBuilder).addFullRow("", element.getClassName(), element.getMethodName(),
                                element.getFileName(), Integer.toString(element.getLineNumber()));
                        inSequence(sequence);
                    }
                }
            });
        
        command.handleRequest(parameters, tableBuilder);
        
        assertEquals(0, headers.size());
        assertEquals(0, rows.size());
        context.assertIsSatisfied();
    }

}
