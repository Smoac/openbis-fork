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

import java.util.Map;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ITableModel;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Super class of all aggregation commands.
 *
 * @author Franz-Josef Elmer
 */
public class AggregationCommand
{
    public final void handleRequest(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor tableBuilder)
    {
        try
        {
            Object requestKey = parameters.get(Constants.REQUEST_KEY);
            if (Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST.equals(requestKey))
            {
                defineParameters(new ParameterDescriptionsBuilder(tableBuilder));
            } else
            {
                aggregate(parameters, tableBuilder);
            }
        } catch (Throwable ex)
        {
            ITableModel tableModel = tableBuilder.getTableModel();
            if (tableModel instanceof TableModel)
            {
                // Put complete stack trace into the table if we can clear it
                TableModel tm = (TableModel) tableModel;
                tm.getHeader().clear();
                tm.getRows().clear();
                tableBuilder.addHeader(Constants.EXCEPTION_COLUMN);
                tableBuilder.addHeader(Constants.STACK_TRACE_CLASS_COLUMN);
                tableBuilder.addHeader(Constants.STACK_TRACE_METHOD_NAME_COLUMN);
                tableBuilder.addHeader(Constants.STACK_TRACE_FILE_NAME_COLUMN);
                tableBuilder.addHeader(Constants.STACK_TRACE_LINE_NUMBER_COLUMN);
                for (Throwable throwable = ex; throwable != null; throwable = throwable.getCause())
                {
                    tableBuilder.addFullRow(throwable.toString(), "", "", "", "");
                    StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                    for (StackTraceElement stackTraceElement : stackTraceElements)
                    {
                        tableBuilder.addFullRow("", stackTraceElement.getClassName(),
                                stackTraceElement.getMethodName(),
                                stackTraceElement.getFileName(),
                                Integer.toString(stackTraceElement.getLineNumber()));
                    }
                }
            } else
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }
    
    /**
     * Defines parameters and their type by using specified {@link ParameterDescriptionsBuilder}.
     * Should be overwritten by subclasses.
     */
    protected void defineParameters(ParameterDescriptionsBuilder parameters)
    {
    }
    
    /**
     * Aggregates data in tabular form based on specified parameter values.
     * Should be overwritten by subclasses.
     */
    protected void aggregate(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor tableBuilder)
    {
    }
    
}
