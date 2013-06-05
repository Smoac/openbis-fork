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

import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.CREDENTIALS_KEY;
import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.PASSWORD_KEY;
import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.URL_KEY;
import static ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel.USER_KEY;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.Credentials;
import org.knime.core.node.workflow.ICredentials;
import org.knime.workbench.core.KNIMECorePlugin;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * Abstract super class of all openBIS KNIME nodes.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractOpenBisNodeDialog extends NodeDialogPane
{
    private static final class Key
    {
        private final String url;
        private final String userID;
        private final String password;

        Key(String url, String userID, String password)
        {
            this.url = url;
            this.userID = userID;
            this.password = password;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Key == false)
            {
                return false;
            }
            Key key = (Key) obj;
            return String.valueOf(key.url).equals(String.valueOf(url))
                    && String.valueOf(key.userID).equals(String.valueOf(userID))
                    && String.valueOf(key.password).equals(String.valueOf(password));
        }
        
        @Override
        public int hashCode()
        {
            int result = String.valueOf(url).hashCode();
            result = 37 * result + String.valueOf(userID).hashCode();
            result = 37 * result + String.valueOf(password).hashCode();
            return result;
        }
    }
    
    private Map<Key, IQueryApiFacade> facades = new HashMap<Key, IQueryApiFacade>();

    protected NodeLogger logger;

    private JComboBox urlField;
    
    private JComboBox credentialsField;

    private JTextField userField;

    private JPasswordField passwordField;

    private final IOpenbisServiceFacadeFactory serviceFacadeFactory;
    
    protected AbstractOpenBisNodeDialog(String tabTitle)
    {
        this(tabTitle, new OpenbisServiceFacadeFactory());
    }
    
    protected AbstractOpenBisNodeDialog(String tabTitle, IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        this.serviceFacadeFactory = serviceFacadeFactory;
        logger = NodeLogger.getLogger(getClass());
        addTab(tabTitle, createTab());
    }

    private Component createTab()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection Parameters"));
        urlField = addField(connectionPanel, "openBIS URL", new JComboBox());
        urlField.setToolTipText("Additional URLs can be defined on the KNIME openBIS preference page.");
        credentialsField = addField(connectionPanel, "Credentials", new JComboBox());
        credentialsField.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent event)
                {
                    boolean enable = "".equals(event.getItem());
                    enableField(userField, enable);
                    enableField(passwordField, enable);
                }
            });
        credentialsField.setToolTipText("<html>Additional credentials can be defined by the menu item 'Workflow Credentials...'<br>"
                + "of the context menu of workflow in the KNIME Explorer.<br><br>"
                + "For security reasons it is not recommended to use the fields 'User' and 'Password'<br>"
                + "because the password is only encrypted if the KNIME Master Key has been defined.");
        userField = addField(connectionPanel, "User", new JTextField(20));
        passwordField = addField(connectionPanel, "Password", new JPasswordField(20));
        JButton button = new JButton("connect");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    connectServer();
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
    protected final void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        String[] urls = getUrls();
        urlField.removeAllItems();
        for (String url : urls)
        {
            urlField.addItem(url);
        }
        urlField.setSelectedItem(settings.getString(URL_KEY, ""));
        credentialsField.removeAllItems();
        credentialsField.addItem("");
        Collection<String> credentialsNames = getAllCredentialsNames();
        for (String credentialsName : credentialsNames)
        {
            credentialsField.addItem(credentialsName);
        }
        String credential = settings.getString(CREDENTIALS_KEY, "");
        credentialsField.setSelectedItem(credential);
        userField.setText(settings.getString(USER_KEY, ""));
        passwordField.setText(Util.getDecryptedPassword(settings));
        loadAdditionalSettingsFrom(settings, specs);
    }

    protected Collection<String> getAllCredentialsNames()
    {
        return getCredentialsNames();
    }

    protected String[] getUrls()
    {
        IPreferenceStore preferenceStore = KNIMECorePlugin.getDefault().getPreferenceStore();
        return preferenceStore.getString(OpenBisPreferencePage.OPENBIS_URLS_KEY).split("\n");
    }

    @Override
    protected final void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException
    {
        settings.addString(URL_KEY, getUrl());
        Object selectedCredentials = credentialsField.getSelectedItem();
        String credentialsName = selectedCredentials == null ? "" : selectedCredentials.toString();
        settings.addString(CREDENTIALS_KEY, credentialsName);
        if (StringUtils.isBlank(credentialsName))
        {
            settings.addString(USER_KEY, userField.getText().trim());
            settings.addString(PASSWORD_KEY, Util.getEncryptedPassword(passwordField.getPassword()));
        }
        saveAdditionalSettingsTo(settings);
    }

    protected void showException(Throwable throwable)
    {
        logger.error("Exception", throwable);
        final String message = createMessage(throwable);
        final JPanel panel = getPanel();
        Window windowAncestor = SwingUtilities.getWindowAncestor(panel);
        if (windowAncestor != null && windowAncestor.isVisible())
        {
            EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JOptionPane.showMessageDialog(panel, message, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
        }
    }

    private String createMessage(Throwable throwable)
    {
        String message = throwable.toString();
        String[] lines = message.split("\n");
        int maxNumberOfLines = 10;
        if (lines.length < maxNumberOfLines)
        {
            return message;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < maxNumberOfLines; i++)
        {
            builder.append(lines[i]).append('\n');
        }
        builder.append("...\n\nSee KNIME log for the complete error message.");
        return builder.toString();
    }

    protected abstract void defineQueryForm(JPanel queryPanel);

    protected abstract void updateQueryForm(IQueryApiFacade queryFacade);

    protected abstract void loadAdditionalSettingsFrom(NodeSettingsRO settings,
            PortObjectSpec[] specs) throws NotConfigurableException;

    protected abstract void saveAdditionalSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException;
    
    private void connectServer()
    {
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final IQueryApiFacade queryFacade = createFacade();
                    EventQueue.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    updateQueryForm(queryFacade);
                                } catch (Throwable ex)
                                {
                                    showException(ex);
                                }
                            }
                        });
                }
            }).start();
    }

    @Override
    public void onCancel()
    {
        logoutAndRemoveAllFacades();
    }

    @Override
    public void onClose()
    {
        logoutAndRemoveAllFacades();
    }

    private void logoutAndRemoveAllFacades()
    {
        for (IQueryApiFacade facade : facades.values())
        {
            facade.logout();
        }
        facades.clear();
    }

    protected IQueryApiFacade createFacade()
    {
        try
        {
            String url = getUrl();
            ICredentials credentials = getCredentials();
            String userID = credentials.getLogin();
            String password = credentials.getPassword();
            Key key = new Key(url, userID, password);
            IQueryApiFacade facade = facades.get(key);
            if (facade == null)
            {
                JPanel panel = getPanel();
                Cursor cursor = panel.getCursor();
                try
                {
                    panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    facade = FacadeFactory.create(url, userID, password);
                } finally
                {
                    panel.setCursor(cursor);
                }
                facades.put(key, facade);
            }
            return facade;
        } catch (RuntimeException ex)
        {
            showException(ex);
            throw ex;
        }
    }

    protected IOpenbisServiceFacade createOpenbisFacade(String sessionToken)
    {
        try
        {
            return serviceFacadeFactory.createFacade(getUrl(), sessionToken);
        } catch (RuntimeException ex)
        {
            showException(ex);
            throw ex;
        }
    }

    protected String getUrl()
    {
        Object selectedItem = urlField.getSelectedItem();
        if (selectedItem == null)
        {
            throw new IllegalArgumentException("Missing URL.");
        }
        return selectedItem.toString();
    }
    
    protected ICredentials getCredentials()
    {
        Object selectedItem = credentialsField.getSelectedItem();
        if (selectedItem == null || selectedItem.toString().trim().length() == 0)
        {
            String user = userField.getText();
            if (StringUtils.isBlank(user))
            {
                throw new IllegalArgumentException("Unspecified credentials or missing user.");
            }
            char[] password = passwordField.getPassword();
            if (password == null || password.length == 0)
            {
                throw new IllegalArgumentException("Unspecified credentials or missing password.");
            }
            return new Credentials("_", user, new String(password));
        }
        return getCredentialsProvider().get(selectedItem.toString());
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

    protected void enableField(JComponent field, boolean enable)
    {
        field.setEnabled(enable);
        Component[] components = field.getParent().getComponents();
        for (int i = 1; i < components.length; i++)
        {
            if (components[i] == field)
            {
                components[i - 1].setEnabled(enable);
                break;
            }
        }
    }

}
