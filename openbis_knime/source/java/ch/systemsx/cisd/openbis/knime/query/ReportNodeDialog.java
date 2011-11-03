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

package ch.systemsx.cisd.openbis.knime.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ReportNodeDialog extends AbstractDescriptionBasedNodeDialog<ReportDescription>
{
    private static final String DELIMITER = ", ";
    private JTextComponent dataSetCodeFields;

    ReportNodeDialog()
    {
        super("Report Settings");
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel, JComboBox reportComboBox)
    {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        addField(panel, "Reports", reportComboBox);
        JPanel textFieldWithButton = new JPanel(new BorderLayout());
        dataSetCodeFields = new JTextField();
        textFieldWithButton.add(dataSetCodeFields, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSet(createFacade());
                }
            });
        textFieldWithButton.add(button, BorderLayout.EAST);
        addField(panel, "Data Set Codes", textFieldWithButton);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(panel, BorderLayout.NORTH);
        queryPanel.add(northPanel, BorderLayout.CENTER);
    }

    @Override
    protected List<ReportDescription> getSortedDescriptions(IQueryApiFacade facade)
    {
        List<ReportDescription> descriptions = facade.listTableReportDescriptions();
        Collections.sort(descriptions, new Comparator<ReportDescription>()
            {
                public int compare(ReportDescription o1, ReportDescription o2)
                {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            });
        return descriptions;
    }
    
    @Override
    protected String getDescriptionKey()
    {
        return ReportNodeModel.REPORT_DESCRIPTION_KEY;
    }

    @Override
    protected void loadMoreSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        String[] dataSetCodes;
        try
        {
            dataSetCodes = settings.getStringArray(ReportNodeModel.DATA_SET_CODES_KEY);
        } catch (InvalidSettingsException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        StringBuilder builder = new StringBuilder();
        for (String dataSetCode : dataSetCodes)
        {
            if (builder.length() > 0)
            {
                builder.append(DELIMITER);
            }
            builder.append(dataSetCode);
        }
        dataSetCodeFields.setText(builder.toString());
    }

    @Override
    protected void saveMoreSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        String dataSetCodes = dataSetCodeFields.getText();
        if (dataSetCodes == null || dataSetCodes.trim().length() == 0)
        {
            throw new InvalidSettingsException("Data set code hasn't been specified.");
        }
        settings.addStringArray(ReportNodeModel.DATA_SET_CODES_KEY, dataSetCodes.split(" *, *"));
    }

    private void chooseDataSet(IQueryApiFacade facade)
    {
        ReportDescription description = getSelectedDescriptionOrNull();
        if (description == null)
        {
            return;
        }
        List<DataSet> dataSets = loadDataSets(description, facade);
        JTable table = new JTable(createTableModel(dataSets));
        table.setPreferredScrollableViewportSize(new Dimension(600, 400));
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Choose a data set:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
        JTextField filterField = createFilterField(sorter);
        filterPanel.add(filterField, BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(getPanel(), panel);
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length > 0)
        {
            StringBuilder builder = new StringBuilder();
            for (int rowIndex : selectedRows)
            {
                if (builder.length() > 0)
                {
                    builder.append(DELIMITER);
                }
                builder.append(dataSets.get(sorter.convertRowIndexToModel(rowIndex)).getCode());
            }
            dataSetCodeFields.setText(builder.toString());
        }
    }

    private JTextField createFilterField(final TableRowSorter<TableModel> sorter)
    {
        final JTextField filterField = new JTextField();
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                newFilter();
            }
            public void insertUpdate(DocumentEvent e) {
                newFilter();
            }
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

    private List<DataSet> loadDataSets(ReportDescription description, IQueryApiFacade facade)
    {
        List<String> dataSetTypes = description.getDataSetTypes();
        IGeneralInformationService service = facade.getGeneralInformationService();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
        for (String dataSetType : dataSetTypes)
        {
            MatchClause clause =
                    MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, dataSetType);
            searchCriteria.addMatchClause(clause);
        }
        List<DataSet> dataSets = service.searchForDataSets(facade.getSessionToken(), searchCriteria);
        return dataSets;
    }

    private TableModel createTableModel(final List<DataSet> dataSets)
    {
        TableModelBuilder builder = new TableModelBuilder();
        for (DataSet dataSet : dataSets)
        {
            builder.add(dataSet);
        }
        return builder.getTableModel();
    }
    
}
