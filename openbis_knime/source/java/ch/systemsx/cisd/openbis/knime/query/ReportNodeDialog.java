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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

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
    private JTextComponent dataSetCodeField;

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
        dataSetCodeField = new JTextField();
        textFieldWithButton.add(dataSetCodeField, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseDataSet(createFacade());
                }
            });
        textFieldWithButton.add(button, BorderLayout.EAST);
        addField(panel, "Data Set Code", textFieldWithButton);
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
        String dataSetCode = settings.getString(ReportNodeModel.DATA_SET_CODE_KEY, "");
        dataSetCodeField.setText(dataSetCode);
    }

    @Override
    protected void saveMoreSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        String dataSetCode = dataSetCodeField.getText();
        if (dataSetCode == null || dataSetCode.trim().length() == 0)
        {
            throw new InvalidSettingsException("Data set code hasn't been specified.");
        }
        settings.addString(ReportNodeModel.DATA_SET_CODE_KEY, dataSetCode);
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
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Choose a data set:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(getPanel(), panel);
        int row = table.getSelectedRow();
        if (row >= 0)
        {
            dataSetCodeField.setText(dataSets.get(row).getCode());
        }
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
        final boolean showTypes = countDataSetTypes(dataSets) > 1;
        TableModel tableModel = new AbstractTableModel()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }

                public int getRowCount()
                {
                    return dataSets.size();
                }

                public int getColumnCount()
                {
                    return showTypes ? 3 : 2;
                }

                @Override
                public String getColumnName(int columnIndex) 
                {
                    if (showTypes)
                    {
                        switch (columnIndex)
                        {
                            case 0:
                                return "Experiment";
                            case 1:
                                return "Code";
                            default:
                                return "Type";
                        }
                    }
                    switch (columnIndex)
                    {
                        case 0:
                            return "Experiment";
                        default:
                            return "Code";
                    }
                }
                
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    DataSet dataSet = dataSets.get(rowIndex);
                    if (showTypes)
                    {
                        switch (columnIndex)
                        {
                            case 0:
                                return dataSet.getExperimentIdentifier();
                            case 1:
                                return dataSet.getCode();
                            default:
                                return dataSet.getDataSetTypeCode();
                        }
                    }
                    switch (columnIndex)
                    {
                        case 0:
                            return dataSet.getExperimentIdentifier();
                        default:
                            return dataSet.getCode();
                    }
                }
            };
            
        return tableModel;
    }
    
    private int countDataSetTypes(List<DataSet> dataSets)
    {
        Set<String> types = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            types.add(dataSet.getDataSetTypeCode());
        }
        return types.size();
    }
    
}
