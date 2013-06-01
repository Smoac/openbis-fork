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
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
import ch.systemsx.cisd.openbis.knime.common.AbstractDescriptionBasedNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
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
        JPanel panel = new JPanel(new GridBagLayout());
        addField(panel, "Reports", reportComboBox);
        JPanel textFieldWithButton = new JPanel(new BorderLayout());
        dataSetCodeFields = new JTextField(20);
        textFieldWithButton.add(dataSetCodeFields, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(new ActionListener()
            {
                @Override
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

        if (descriptions != null)
        {
            Set<String> keys = new HashSet<String>();
            List<ReportDescription> uniqueDescriptions = new ArrayList<ReportDescription>();

            for (ReportDescription description : descriptions)
            {
                if (keys.contains(description.getKey()) == false)
                {
                    uniqueDescriptions.add(description);
                    keys.add(description.getKey());
                }
            }

            Collections.sort(uniqueDescriptions, new Comparator<ReportDescription>()
                {
                    @Override
                    public int compare(ReportDescription o1, ReportDescription o2)
                    {
                        return o1.getLabel().compareToIgnoreCase(o2.getLabel());
                    }
                });

            return uniqueDescriptions;
        } else
        {
            return Collections.emptyList();
        }
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
        JPanel panel = getPanel();
        Cursor cursor = panel.getCursor();
        try
        {
            panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            List<DataSet> dataSets = loadDataSets(description, facade);
            List<DataSet> selectedDataSets = Util.getSelectedDataSets(getPanel(), cursor, dataSets, false);
            if (selectedDataSets.isEmpty())
            {
                return;
            }
            StringBuilder builder = new StringBuilder();
            for (DataSet dataSet : selectedDataSets)
            {
                
                if (builder.length() > 0)
                {
                    builder.append(DELIMITER);
                }
                builder.append(dataSet.getCode());
            }
            dataSetCodeFields.setText(builder.toString());
        } finally
        {
            panel.setCursor(cursor);
        }
    }

    private List<DataSet> loadDataSets(ReportDescription description, IQueryApiFacade facade)
    {
        List<String> dataSetTypes = description.getDataSetTypes();
        IGeneralInformationService service = facade.getGeneralInformationService();
        List<DataSet> allDataSets = new ArrayList<DataSet>();
        for (String dataSetType : dataSetTypes)
        {
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);
            MatchClause clause =
                    MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, dataSetType);
            searchCriteria.addMatchClause(clause);
            List<DataSet> dataSets = service.searchForDataSets(facade.getSessionToken(), searchCriteria);
            logger.info(dataSets.size() + " data sets of type " + dataSetType);
            allDataSets.addAll(dataSets);
        }
        return allDataSets;        
    }
}
