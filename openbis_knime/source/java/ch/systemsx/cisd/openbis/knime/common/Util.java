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
import java.util.List;

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

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.util.KnimeEncryption;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;

/**
 * Utility methods.
 *
 * @author Franz-Josef Elmer
 */
public class Util
{
    public static final String VARIABLE_PREFIX = "openbis.";
    
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
            return KnimeEncryption.decrypt(settings.getString(PASSWORD_KEY, ""));
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

}
