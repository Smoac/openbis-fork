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

package ch.systemsx.cisd.openbis.knime;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.QueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TestNodeDialog extends NodeDialogPane
{
    private JTextField urlField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JComboBox queryComboBox;

    TestNodeDialog()
    {
        super();
        addTab("openBIS Settings", createGUI());
    }

    private JPanel createGUI()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(0, 2));
        form.add(new JLabel("openBIS URL:"));
        urlField = new JTextField();
        form.add(urlField);
        form.add(new JLabel("User:"));
        userField = new JTextField();
        form.add(userField);
        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        form.add(passwordField);
        form.add(new JLabel("Queries:"));
        queryComboBox = new JComboBox();
        form.add(queryComboBox);
        panel.add(form, BorderLayout.NORTH);
        JButton button = new JButton("connect");
        button.addActionListener(new ActionListener()
            {
                
                public void actionPerformed(ActionEvent e)
                {
                    QueryApiFacade facade = QueryApiFacade.create(urlField.getText(), userField.getText(), passwordField.getText());
                    List<QueryDescription> queries = facade.listQueries();
                    for (QueryDescription queryDescription : queries)
                    {
                        queryComboBox.addItem(queryDescription.getName());
                    }
                    // TODO Auto-generated method stub
                    
                }
            });
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        // TODO Auto-generated method stub

    }

}
