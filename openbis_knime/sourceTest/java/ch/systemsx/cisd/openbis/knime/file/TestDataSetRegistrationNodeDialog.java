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

package ch.systemsx.cisd.openbis.knime.file;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.knime.core.node.NodeSettings;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeModel;
import ch.systemsx.cisd.openbis.knime.common.Util;

/**
 * Stand alone application for testing {@link DataSetRegistrationNodeDialog}.
 *
 * @author Franz-Josef Elmer
 */
public class TestDataSetRegistrationNodeDialog
{
    private static final class NodeManager
    {
        private final JPanel panel;
        private NodeDialog nodeDialog;
        private byte[] settings;

        NodeManager(JPanel panel)
        {
            this.panel = panel;
        }
        
        void createNode()
        {
            try
            {
                if (nodeDialog != null)
                {
                    panel.remove(nodeDialog.getPanel());
                }
                nodeDialog = new NodeDialog();
                if (settings != null)
                {
                    nodeDialog.loadSettingsFrom(new ByteArrayInputStream(settings));
                }
                panel.add(nodeDialog.getPanel(), BorderLayout.CENTER);
                panel.invalidate();
                panel.getParent().validate();
            } catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(nodeDialog.getPanel(), ex.toString());
            }
        }
        
        void saveSettings()
        {
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                nodeDialog.saveSettingsTo(out);
                System.out.println(out.toString());
                settings = out.toByteArray();
            } catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(nodeDialog.getPanel(), ex.toString());
            }
        }
    }
    private static final class NodeDialog extends DataSetRegistrationNodeDialog
    {
        NodeDialog() throws NotConfigurableException
        {
            super(new OpenbisServiceFacadeFactory());
            NodeSettings settings = new NodeSettings("");
            settings.addString(AbstractOpenBisNodeModel.URL_KEY, "http://localhost:8888");
            settings.addString(AbstractOpenBisNodeModel.USER_KEY, "test");
            settings.addString(AbstractOpenBisNodeModel.PASSWORD_KEY,
                    Util.getEncryptedPassword("a".toCharArray()));
            loadSettingsFrom(settings, (PortObjectSpec[]) null);
        }
    }

    public static void main(String[] args) throws NotConfigurableException
    {
        LogInitializer.init();
        JFrame frame = new JFrame("test");
        JPanel panel = new JPanel(new BorderLayout());
        frame.getContentPane().add(panel);
        final NodeManager nodeManager = new NodeManager(panel);
        nodeManager.createNode();
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    nodeManager.saveSettings();
                }
            });
        buttonPanel.add(saveButton);
        JButton readButton = new JButton("Read");
        readButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    nodeManager.createNode();
                }
            });
        buttonPanel.add(readButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

}
