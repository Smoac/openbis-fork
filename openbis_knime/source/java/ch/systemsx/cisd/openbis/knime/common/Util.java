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

package ch.systemsx.cisd.openbis.knime.common;

import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.PASSWORD_KEY;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.util.KnimeEncryption;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.knime.server.Constants;
import ch.systemsx.cisd.openbis.knime.server.FieldType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Utility methods.
 *
 * @author Franz-Josef Elmer
 */
public class Util
{
    public static final String VARIABLE_PREFIX = "openbis.";

    private static final String[] EXPECTED_COLUMNS = { Constants.PARAMETER_DESCRIPTION_NAME_COLUMN,
            Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN };
    
    public static <D extends Serializable> byte[] serializeDescription(D descriptionOrNull)
    {
        if (descriptionOrNull == null)
        {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            new ObjectOutputStream(baos).writeObject(descriptionOrNull);
            return baos.toByteArray();
        } catch (IOException ex)
        {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <D extends Serializable> D deserializeDescription(byte[] serializeDescriptionOrNull)
    {
        if (serializeDescriptionOrNull == null)
        {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(serializeDescriptionOrNull);
        try
        {
            return (D) new ObjectInputStream(bais).readObject();
        } catch (Exception ex)
        {
            return null;
        }
    }
    
    public static List<FieldDescription> getFieldDescriptions(IQueryApiFacade facade,
            AggregatedDataImportDescription description, NodeLogger logger)
    {
        List<FieldDescription> fieldDescriptions = new ArrayList<FieldDescription>();
        Map<String, Object> serviceParameters = new HashMap<String, Object>();
        serviceParameters.put(Constants.REQUEST_KEY, Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST);
        QueryTableModel report =
                createReportFromAggregationService(facade, description, serviceParameters);
        List<QueryTableColumn> columns = report.getColumns();
        if (columns.size() != EXPECTED_COLUMNS.length)
        {
            throw createException(description, columns.size() + " columns instead of " + EXPECTED_COLUMNS.length);
        }
        for (int i = 0; i < EXPECTED_COLUMNS.length; i++)
        {
            String expectedColumn = EXPECTED_COLUMNS[i];
            String column = columns.get(i).getTitle();
            if (expectedColumn.equals(column) == false)
            {
                throw createException(description, (i + 1) + ". column is '" + column
                        + "' instead of '" + expectedColumn + "'.");
            }
        }
        List<Serializable[]> rows = report.getRows();
        for (Serializable[] row : rows)
        {
            if (row == null || row.length == 0 || row[0] == null)
            {
                throw createException(description, "Empty row.");
            }
            String name = String.valueOf(row[0]);
            if (StringUtils.isBlank(name))
            {
                throw createException(description, "Unspecified parameter name.");
            }
            FieldType fieldType = FieldType.VARCHAR;
            String fieldParameters = "";
            if (row.length > 1)
            {
                Serializable parameter = row[1];
                if (parameter != null)
                {
                    String type = String.valueOf(parameter);
                    int indexOfSeparator = type.indexOf(':');
                    if (indexOfSeparator >= 0)
                    {
                        fieldParameters = type.substring(indexOfSeparator + 1);
                        type = type.substring(0, indexOfSeparator);
                    }
                    try
                    {
                        fieldType = FieldType.valueOf(type.trim().toUpperCase());
                    } catch (IllegalArgumentException ex)
                    {
                        logger.warn("Unknown field type '" + type + "' using " + fieldType + " instead.");
                    }
                }
            }
            fieldDescriptions.add(new FieldDescription(name, fieldType, fieldParameters));
        }
        return fieldDescriptions;
    }

    private static RuntimeException createException(AggregatedDataImportDescription description, String msg)
    {
        String service = description.getAggregationServiceDescription().getServiceKey();
        return new IllegalArgumentException("Invalid response of aggregation service '" + service + "' when invoked with parameter "
                + Constants.REQUEST_KEY + " = " + Constants.GET_PARAMETER_DESCRIPTIONS_REQUEST + ":\n" + msg);
    }

    public static ColumnType getColumnType(QueryTableColumnDataType dataType)
    {
        switch (dataType)
        {
            case DOUBLE: return ColumnType.DOUBLE;
            case LONG: return ColumnType.LONG;
            default: return ColumnType.STRING;
        }
    }

    public static String getDecryptedPassword(NodeSettingsRO settings)
    {
        try
        {
            String encryptedPassword = settings.getString(PASSWORD_KEY, "");
            return StringUtils.isBlank(encryptedPassword) ? "" : KnimeEncryption.decrypt(encryptedPassword);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public static String getEncryptedPassword(char[] bytes)
    {
        try
        {
            return KnimeEncryption.encrypt(bytes);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    public static List<DataSet> getSelectedDataSets(Component parentComponent,
            List<DataSet> dataSets, boolean singleSelection)
    {
        JTable table = new JTable(createTableModel(dataSets));
        table.setPreferredScrollableViewportSize(new Dimension(600, 400));
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(sorter);
        ListSelectionModel selectionModel = table.getSelectionModel();
        if (singleSelection)
        {
            selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else
        {
            selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Choose a data set:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
        JTextField filterField = createFilterField(sorter);
        filterPanel.add(filterField, BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(parentComponent, panel);
        int[] selectedRows = table.getSelectedRows();
        List<DataSet> result = new ArrayList<DataSet>();
        for (int rowIndex : selectedRows)
        {
            result.add(dataSets.get(sorter.convertRowIndexToModel(rowIndex)));
        }
        return result;
    }
    
    private static TableModel createTableModel(final List<DataSet> dataSets)
    {
        TableModelBuilder builder = new TableModelBuilder();
        for (DataSet dataSet : dataSets)
        {
            builder.add(dataSet);
        }
        return builder.getTableModel();
    }
    
    private static JTextField createFilterField(final TableRowSorter<TableModel> sorter)
    {
        final JTextField filterField = new JTextField();
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                newFilter();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                newFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                newFilter();
            }

                private void newFilter()
                {
                    final String text = filterField.getText().toLowerCase();
                    RowFilter<TableModel, Object> rf = new RowFilter<TableModel, Object>()
                        {
                            @Override
                            public boolean include(
                                    Entry<? extends TableModel, ? extends Object> entry)
                            {
                                for (int i = 0, n = entry.getValueCount(); i < n; i++)
                                {
                                    if (entry.getStringValue(i).toLowerCase().indexOf(text) >= 0)
                                    {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };
                    sorter.setRowFilter(rf);
                }
            });
        return filterField;
    }

    public static QueryTableModel createReportFromAggregationService(IQueryApiFacade facade,
            ParameterBindings parameterBindings, AggregatedDataImportDescription description)
    {
        Map<String, Object> serviceParameters = new HashMap<String, Object>();
        for (Entry<String, String> entry : parameterBindings.getBindings().entrySet())
        {
            serviceParameters.put(entry.getKey(), entry.getValue());
        }
        return createReportFromAggregationService(facade, description, serviceParameters);
    }

    public static QueryTableModel createReportFromAggregationService(IQueryApiFacade facade,
            AggregatedDataImportDescription description, Map<String, Object> serviceParameters)
    {
        QueryTableModel result = facade.createReportFromAggregationService(
                description.getAggregationServiceDescription(), serviceParameters);
        assertNoError(result, description);
        return result;
    }
    
    private static void assertNoError(QueryTableModel result, AggregatedDataImportDescription description)
    {
        List<QueryTableColumn> columns = result.getColumns();
        if (columns.size() != 5 || columns.get(0).getTitle().equals(Constants.EXCEPTION_COLUMN) == false)
        {
            return;
        }
        ExceptionReplicate rootException = null;
        ExceptionReplicate previousException = null;
        ExceptionReplicate currentExecption = null;
        List<Serializable[]> rows = result.getRows();
        List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();
        for (Serializable[] row : rows)
        {
            Serializable exception = row[0];
            if ("".equals(exception) == false)
            {
                currentExecption = new ExceptionReplicate(String.valueOf(row[0]));
                if (previousException != null)
                {
                    previousException.setStackTrace(stackTrace.toArray(new StackTraceElement[0]));
                    previousException.setCause(currentExecption);
                    stackTrace.clear();
                } else
                {
                    rootException = currentExecption;
                }
                previousException = currentExecption;
            } else if (currentExecption != null)
            {
                int lineNumber;
                try
                {
                    lineNumber = Integer.parseInt(String.valueOf(row[4]));
                } catch (NumberFormatException ex)
                {
                    lineNumber = 0;
                }
                StackTraceElement stackTraceElement = new StackTraceElement(String.valueOf(row[1]), 
                        String.valueOf(row[2]), String.valueOf(row[3]), 
                        lineNumber);
                stackTrace.add(stackTraceElement);
            }
        }
        if (currentExecption != null && stackTrace.isEmpty() == false)
        {
            currentExecption.setStackTrace(stackTrace.toArray(new StackTraceElement[0]));
        }
        if (rootException != null)
        {
            throw rootException;
        }
    }

    private static final class ExceptionReplicate extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private static String extractMessage(String toStringText)
        {
            int indexOfColon = toStringText.indexOf(":");
            return indexOfColon < 0 ? "" : toStringText.substring(indexOfColon + 1).trim();
        }
        
        private final String toStringText;
        private ExceptionReplicate cause;

        ExceptionReplicate(String toStringText)
        {
            super(extractMessage(toStringText));
            this.toStringText = toStringText;
        }
        
        public void setCause(ExceptionReplicate cause)
        {
            this.cause = cause;
        }

        @Override
        public Throwable getCause()
        {
            return cause;
        }
        
        @Override
        public String toString()
        {
            return toStringText;
        }
    }

}
