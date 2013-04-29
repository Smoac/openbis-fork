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

package ch.systemsx.cisd.openbis.knime.common;

import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.PASSWORD_KEY;
import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.URL_KEY;
import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.USER_KEY;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractOpenBisNodeDialog extends NodeDialogPane
{
    protected NodeLogger logger;

    protected JTextField urlField;

    protected JTextField userField;

    protected JPasswordField passwordField;

    protected AbstractOpenBisNodeDialog(String tabTitle)
    {
        logger = NodeLogger.getLogger(getClass());
        addTab(tabTitle, createTab());
    }

    private Component createTab()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection Parameters"));
        urlField = addField(connectionPanel, "openBIS URL", new JTextField(20));
        userField = addField(connectionPanel, "User", new JTextField(20));
        passwordField = addField(connectionPanel, "Password", new JPasswordField(20));
        JButton button = new JButton("connect");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    updateQueryForm(createFacade());
                }
            });
        connectionPanel.add(button, createLast());
        panel.add(connectionPanel, BorderLayout.NORTH);
        JPanel queryPanel = new JPanel(new BorderLayout());
        defineQueryForm(queryPanel);
        panel.add(queryPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(600, 450));

        return scrollPane;
    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        urlField.setText(settings.getString(URL_KEY, ""));
        userField.setText(settings.getString(USER_KEY, ""));
        passwordField.setText(Util.getDecryptedPassword(settings));
        loadAdditionalSettingsFrom(settings, specs);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        settings.addString(URL_KEY, urlField.getText().trim());
        settings.addString(USER_KEY, userField.getText().trim());
        settings.addString(PASSWORD_KEY, Util.getEncryptedPassword(passwordField.getPassword()));
        saveAdditionalSettingsTo(settings);
    }

    protected void showException(Throwable throwable)
    {
        logger.error("Exception", throwable);
        JOptionPane.showMessageDialog(getPanel(), throwable.toString(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    protected abstract void defineQueryForm(JPanel queryPanel);

    protected abstract void updateQueryForm(IQueryApiFacade facade);

    protected abstract void loadAdditionalSettingsFrom(NodeSettingsRO settings,
            PortObjectSpec[] specs) throws NotConfigurableException;

    protected abstract void saveAdditionalSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException;

    protected IQueryApiFacade createFacade()
    {
        try
        {
            String url = urlField.getText();
            String userID = userField.getText();
            String password = new String(passwordField.getPassword());
            IQueryApiFacade facade = FacadeFactory.create(url, userID, password);
            return facade;
        } catch (RuntimeException ex)
        {
            showException(ex);
            throw ex;
        }
    }

    protected <T extends JComponent> T addField(Container panel, String label, T field)
    {
        return addField(panel, label, field, false);
    }

    protected <T extends JComponent> T addField(Container panel, String label, T field,
            boolean mandatory)
    {
        panel.add(new JLabel(label + (mandatory ? ":*" : ":")), createFirst());
        panel.add(field, createLast());
        return field;
    }
    
    protected GridBagConstraints createLast()
    {
        GridBagConstraints last = createFirst();
        last.gridwidth = GridBagConstraints.REMAINDER;
        return last;
    }

    protected GridBagConstraints createFirst()
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 3, 2, 3);
        return constraints;
    }

}
