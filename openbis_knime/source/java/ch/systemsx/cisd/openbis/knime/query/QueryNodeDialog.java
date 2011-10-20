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

package ch.systemsx.cisd.openbis.knime.query;

import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.QUERY_DESCRIPTION_KEY;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * @author Franz-Josef Elmer
 */
public class QueryNodeDialog extends AbstractOpenBisNodeDialog
{
    private static final NodeLogger log = NodeLogger.getLogger(QueryNodeDialog.class);

    private JComboBox queryComboBox;

    private JPanel parametersPanel;

    private ParameterBindings parameterBindings = new ParameterBindings();

    private Map<String, JTextField> parameterFields = new HashMap<String, JTextField>();

    QueryNodeDialog()
    {
        super("Query Settings");
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel)
    {
        queryComboBox = new JComboBox();
        queryComboBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    QueryDescription queryDescription = (QueryDescription) e.getItem();
                    updateParametersPanel(queryDescription);
                    log.info(queryDescription);
                }
            });
        JPanel querySelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        querySelectionPanel.add(new JLabel("Query:"));
        querySelectionPanel.add(queryComboBox);
        queryPanel.add(querySelectionPanel, BorderLayout.NORTH);
        parametersPanel = new JPanel(new GridLayout(0, 2));
        parametersPanel.setBorder(BorderFactory.createTitledBorder("Query Parameters"));
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(parametersPanel, BorderLayout.NORTH);
        queryPanel.add(northPanel, BorderLayout.CENTER);
    }

    @Override
    protected void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        byte[] bytes = settings.getByteArray(QUERY_DESCRIPTION_KEY, null);
        QueryDescription queryDescriptionOrNull = Util.deserializeQueryDescription(bytes);
        parameterBindings.loadValidatedSettingsFrom(settings);
        if (queryDescriptionOrNull != null && queryComboBox.getItemCount() == 0)
        {
            queryComboBox.addItem(queryDescriptionOrNull);
            queryComboBox.setSelectedIndex(0);
        }
    }

    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        byte[] bytes = Util.serializeQueryDescription(getSelectedQueryDescriptionOrNull());
        settings.addByteArray(QUERY_DESCRIPTION_KEY, bytes);
        parameterBindings.removeAllBindings();
        Set<Entry<String, JTextField>> entrySet = parameterFields.entrySet();
        for (Entry<String, JTextField> entry : entrySet)
        {
            parameterBindings.bind(entry.getKey(), entry.getValue().getText());
        }
        parameterBindings.saveSettingsTo(settings);
    }

    private QueryDescription getSelectedQueryDescriptionOrNull()
    {
        Object selectedItem = queryComboBox.getSelectedItem();
        return selectedItem == null ? null : (QueryDescription) selectedItem;
    }

    @Override
    protected void updateQueryForm(IQueryApiFacade facade)
    {
        List<QueryDescription> queries = facade.listQueries();
        Collections.sort(queries, new Comparator<QueryDescription>()
            {
                public int compare(QueryDescription d1, QueryDescription d2)
                {
                    return d1.getName().compareTo(d2.getName());
                }
            });
        QueryDescription selectedQueryDescription = getSelectedQueryDescriptionOrNull();
        queryComboBox.removeAllItems();
        for (QueryDescription queryDescription : queries)
        {
            queryComboBox.addItem(queryDescription);
            if (queryDescription.equals(selectedQueryDescription))
            {
                queryComboBox.setSelectedItem(queryDescription);
            }
        }
    }

    private void updateParametersPanel(QueryDescription queryDescription)
    {
        log.info("update parameters panel for '" + queryDescription + "' which has "
                + queryDescription.getParameters().size() + " parameters.");
        parametersPanel.removeAll();
        parameterFields.clear();
        List<String> parameters = queryDescription.getParameters();
        parametersPanel.setVisible(parameters.isEmpty() == false);
        for (String parameter : parameters)
        {
            JTextField field = parameterFields.get(parameter);
            if (field == null)
            {
                field = new JTextField(15);
                parameterFields.put(parameter, field);
            }
            String value = parameterBindings.tryToGetBinding(parameter);
            if (value != null)
            {
                field.setText(value);
            }
            addField(parametersPanel, parameter, field);
        }
        parametersPanel.invalidate();
        parametersPanel.getParent().validate();
    }

}
