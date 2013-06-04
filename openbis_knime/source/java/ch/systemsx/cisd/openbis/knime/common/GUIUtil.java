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

package ch.systemsx.cisd.openbis.knime.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * GUI utility functions.
 *
 * @author Franz-Josef Elmer
 */
public class GUIUtil
{
    public static List<DataSet> getSelectedDataSets(Component parentComponent, Cursor oldCursor,
            List<DataSet> dataSets, boolean singleSelection)
    {
        JTable table = new JTable(createTableModel(dataSets));
        table.setPreferredScrollableViewportSize(new Dimension(900, 500));
        JTableHeader tableHeader = table.getTableHeader();
        final TableColumnModel columnModel = tableHeader.getColumnModel();
        final TableCellRenderer renderer = tableHeader.getDefaultRenderer();
        tableHeader.setDefaultRenderer(new TableCellRenderer()
            {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, 
                        boolean hasFocus, int row, int column)
                {
                    Component component = renderer.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    if (component instanceof JComponent)
                    {
                        String title = columnModel.getColumn(column).getHeaderValue().toString();
                        ((JComponent) component).setToolTipText(title);
                    }
                    return component;
                }
            });
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
        JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        parentComponent.setCursor(oldCursor);
        JDialog dialog = optionPane.createDialog(parentComponent, "Data Sets");
        dialog.setResizable(true);
        dialog.setVisible(true);
        List<DataSet> result = new ArrayList<DataSet>();
        Object value = optionPane.getValue();
        if (new Integer(JOptionPane.OK_OPTION).equals(value))
        {
            int[] selectedRows = table.getSelectedRows();
            for (int rowIndex : selectedRows)
            {
                result.add(dataSets.get(sorter.convertRowIndexToModel(rowIndex)));
            }
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
