/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;

/**
 * Utility class containing functions helpful with dealing with grid custom filters or columns.
 * 
 * @author Izabela Adamczyk
 */
public class GridExpressionUtils
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, GridExpressionUtils.class);

    private static final String FILTER_EVALUATION_ERROR_MSG =
            "Problem occured during applying the filter.<br><br>Check that all provided parameter values are correct. "
                    + "If everything seems fine contact filter registrator or instance admin about a possible bug in the filter definition.";

    private static final String EVALUATION_SERIOUS_ERROR_MSG = "Serious problem occured during ";

    private static final String FILTER_EVALUATION_SERIOUS_ERROR_MSG =
            EVALUATION_SERIOUS_ERROR_MSG + "applying the filter: ";

    private static final String COLUMN_EVALUATION_SERIOUS_ERROR_TEMPLATE =
            "Error: calculating the value of a custom column '%s' failed, contact your administrator: ";

    private static final String COLUMN_EVALUATION_ERROR_TEMPLATE =
            COLUMN_EVALUATION_SERIOUS_ERROR_TEMPLATE + "invalid column definition. ";

    /**
     * Applies the filter described by <code>customFilterInfo</code> to
     * <code>allRows<code> and returns the result.
     */
    public static <T> GridRowModels<T> applyCustomFilter(final GridRowModels<T> rows,
            Set<IColumnDefinition<T>> availableColumns, CustomFilterInfo<T> customFilterInfo)
    {
        GridRowModels<T> filtered = new GridRowModels<T>(rows.getCustomColumnsMetadata());
        String expression = StringEscapeUtils.unescapeHtml(customFilterInfo.getExpression());
        Set<ParameterWithValue> parameters = customFilterInfo.getParameters();
        try
        {
            RowCalculator<T> calculator =
                    new RowCalculator<T>(availableColumns, expression, parameters);
            for (GridRowModel<T> rowData : rows)
            {
                calculator.setRowData(rowData);
                if (calculator.evalToBoolean())
                {
                    filtered.add(rowData);
                }
            }
        } catch (Exception ex)
        {
            throw createInvalidFilterException(ex);
        }
        return filtered;
    }

    private static UserFailureException createInvalidFilterException(Exception ex)
    {
        String msg;
        String details = null;
        if (ex instanceof EvaluatorException)
        {
            msg = FILTER_EVALUATION_ERROR_MSG;
            details = ex.getMessage();
        } else
        {
            msg = FILTER_EVALUATION_SERIOUS_ERROR_MSG + ex;
        }
        logColumnCalculationError(ex, msg, details);
        return new UserFailureException(msg, details);
    }

    public static <T> GridRowModels<T> evalCustomColumns(final List<T> allRows,
            List<GridCustomColumn> customColumns, Set<IColumnDefinition<T>> availableColumns)
    {
        GridRowModels<T> result = new GridRowModels<T>(extractColumnInfos(customColumns));
        Map<String, RowCalculator<T>> calculators = new HashMap<String, RowCalculator<T>>();
        for (GridCustomColumn customColumn : customColumns)
        {
            RowCalculator<T> rowCalculator = createRowCalculator(availableColumns, customColumn);
            calculators.put(customColumn.getCode(), rowCalculator);
        }
        for (T rowData : allRows)
        {
            HashMap<String, PrimitiveValue> customColumnValues =
                    new HashMap<String, PrimitiveValue>();
            for (GridCustomColumn customColumn : customColumns)
            {
                String columnId = customColumn.getCode();
                RowCalculator<T> calculator = calculators.get(columnId);
                PrimitiveValue value = evalCustomColumn(rowData, customColumn, calculator);
                customColumnValues.put(columnId, value);
            }
            result.add(new GridRowModel<T>(rowData, customColumnValues));
        }
        return result;
    }

    private static <T> RowCalculator<T> createRowCalculator(
            Set<IColumnDefinition<T>> availableColumns, GridCustomColumn customColumn)
    {
        String expression = StringEscapeUtils.unescapeHtml(customColumn.getExpression());
        try
        {
            return new RowCalculator<T>(availableColumns, expression);
        } catch (Exception ex)
        {
            throw new UserFailureException(createCustomColumnErrorMessage(customColumn, ex));
        }
    }

    private static List<GridCustomColumnInfo> extractColumnInfos(
            List<GridCustomColumn> customColumns)
    {
        List<GridCustomColumnInfo> result = new ArrayList<GridCustomColumnInfo>();
        for (GridCustomColumn column : customColumns)
        {
            GridCustomColumnInfo columnInfo =
                    new GridCustomColumnInfo(column.getCode(), column.getName(), column
                            .getDescription());
            result.add(columnInfo);
        }
        return result;
    }

    private static <T> PrimitiveValue evalCustomColumn(T rowData, GridCustomColumn customColumn,
            RowCalculator<T> calculator)
    {
        // NOTE: we do not allow a calculated column to reference other calculated columns. It's
        // a simplest way to ensure that there are no cyclic dependencies between custom
        // columns. To allow custom columns dependencies we would have to ensure that
        // dependencies create a DAG. Then the columns should be evaluated in a topological
        // order.
        GridRowModel<T> rowDataWithEmptyCustomColumns =
                new GridRowModel<T>(rowData, new HashMap<String, PrimitiveValue>());
        try
        {
            calculator.setRowData(rowDataWithEmptyCustomColumns);

            return calculator.getTypedResult();
        } catch (Exception ex)
        {
            return new PrimitiveValue(createCustomColumnErrorMessage(customColumn, ex));
        }
    }

    private static String createCustomColumnErrorMessage(GridCustomColumn customColumn, Exception ex)
    {
        String msg;
        String details = null;
        String columnDesc = customColumn.getName() + " (" + customColumn.getCode() + ")";
        if (ex instanceof EvaluatorException)
        {
            msg = String.format(COLUMN_EVALUATION_ERROR_TEMPLATE, columnDesc);
            details = ex.getMessage();
        } else
        {
            msg = String.format(COLUMN_EVALUATION_SERIOUS_ERROR_TEMPLATE, columnDesc) + ex;
        }
        return msg + " DETAILS: " + details;
    }

    private static void logColumnCalculationError(Exception ex, String msg, String details)
    {
        if (operationLog.isInfoEnabled())
        {
            // we do not log this as an error, because it can be only user fault (wrong column
            // definition)
            operationLog.info(msg + " DETAILS: " + details, ex);
        }
    }
}
