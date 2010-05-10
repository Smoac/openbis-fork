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

import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.PASSWORD_KEY;
import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.QUERY_DESCRIPTION_KEY;
import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.URL_KEY;
import static ch.systemsx.cisd.openbis.knime.query.QueryNodeModel.USER_KEY;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryNodeDialog extends NodeDialogPane
{
    private static final NodeLogger log = NodeLogger.getLogger(QueryNodeDialog.class);
    
    private JTextField urlField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JComboBox queryComboBox;
    private JPanel parametersPanel;
    private ParameterBindings parameterBindings = new ParameterBindings();
    private Map<String, JTextField> parameterFields = new HashMap<String, JTextField>();

    QueryNodeDialog()
    {
        super();
        addTab("Query Settings", createGUI());
    }
    
    private JComponent createGUI()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 450));
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection Parameters"));
        urlField = addField(connectionPanel, "openBIS URL", new JTextField(20));
        userField = addField(connectionPanel, "User", new JTextField(20));
        passwordField = addField(connectionPanel, "Password", new JPasswordField(20));
        JButton button = new JButton("connect");
        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    connectToOpenBIS();
                }
            });
        connectionPanel.add(button, createLast());
        panel.add(connectionPanel, BorderLayout.NORTH);
        JPanel queryPanel = new JPanel(new BorderLayout());
        panel.add(queryPanel, BorderLayout.CENTER);
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
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        urlField.setText(settings.getString(URL_KEY, ""));
        userField.setText(settings.getString(USER_KEY, ""));
        passwordField.setText(Util.getDecryptedPassword(settings));
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
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        settings.addString(URL_KEY, urlField.getText().trim());
        settings.addString(USER_KEY, userField.getText().trim());
        settings.addString(PASSWORD_KEY, Util.getEncryptedPassword(passwordField.getPassword()));
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

    private <T extends JComponent> T addField(JPanel panel, String label, T field)
    {
        panel.add(new JLabel(label + ":"), createFirst());
        panel.add(field, createLast());
        return field;
    }

    private GridBagConstraints createLast()
    {
        GridBagConstraints last = createFirst(); 
        last.gridwidth = GridBagConstraints.REMAINDER;
        return last;
    }
    
    private GridBagConstraints createFirst()
    {
        GridBagConstraints constraints = new GridBagConstraints(); 
        constraints.anchor = GridBagConstraints.WEST; 
        constraints.fill = GridBagConstraints.HORIZONTAL; 
        constraints.insets = new Insets(2, 3, 2, 3);
        return constraints;
    }
    
    private void connectToOpenBIS()
    {
        String url = urlField.getText();
        String userID = userField.getText();
        String password = passwordField.getText();
        IQueryApiFacade facade = FacadeFactory.create(url, userID, password);
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
