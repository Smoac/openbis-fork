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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_HEIGHT;
import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.BUTTON_WIDTH;
import static ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClient.LABEL_WIDTH;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetMetadataPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private final JTextField sampleIdText;

    private final JButton sampleClearButton;

    private final JTextField experimentIdText;

    private final JButton experimentClearButton;

    private final JComboBox dataSetTypeComboBox;

    private final JPanel dataSetTypePanel;

    private final JButton dataSetFileButton;

    public DataSetMetadataPanel()
    {
        super();
        setLayout(new GridBagLayout());

        // Initialize the fields in the gui
        sampleIdText = new JTextField();
        sampleClearButton = new JButton("Clear");
        experimentIdText = new JTextField();
        experimentClearButton = new JButton("Clear");
        dataSetTypeComboBox = new JComboBox();
        dataSetTypePanel = new JPanel();
        dataSetFileButton = new JButton("");

        createGui();
    }

    private void createGui()
    {
        // The sample row
        JLabel label = new JLabel("Sample:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        sampleIdText.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        sampleClearButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        sampleClearButton.setToolTipText("Attach the data set to a sample.");
        sampleClearButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                }
            });
        addRow(0, label, sampleIdText, sampleClearButton);

        // The experiment row
        label = new JLabel("Experiment:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        experimentIdText.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        experimentClearButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        experimentClearButton.setToolTipText("Attach the data set to an experiment.");
        experimentClearButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                }
            });
        addRow(1, label, experimentIdText, experimentClearButton);

        // The data set type row
        label = new JLabel("Data Set Type:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        dataSetTypeComboBox.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        dataSetTypeComboBox.setToolTipText("Select the data set type for the experiment");
        for (String dataSetType : getDataSetTypes())
        {
            dataSetTypeComboBox.addItem(dataSetType);
        }
        dataSetTypeComboBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    CardLayout cardLayout = (CardLayout) dataSetTypePanel.getLayout();
                    cardLayout.show(dataSetTypePanel, (String) e.getItem());
                }
            });
        addRow(2, label, dataSetTypeComboBox);

        createDataSetTypePanel();
        addRow(3, dataSetTypePanel);

        // The file row
        label = new JLabel("File:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        dataSetFileButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        dataSetFileButton.setToolTipText("The file to upload.");
        dataSetFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                }
            });
        addRow(4, label, dataSetFileButton);
    }

    private void createDataSetTypePanel()
    {
        dataSetTypePanel.setLayout(new CardLayout());
        for (String dataSetType : getDataSetTypes())
        {
            JPanel typeView = new JPanel();
            typeView.add(new JLabel(dataSetType), BorderLayout.CENTER);
            dataSetTypePanel.add(typeView, dataSetType);
        }
    }

    private void addRow(int rowy, Component label, Component field, Component button)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = rowy;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(label, c);
        c.gridx = 1;
        c.weightx = 0.5;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(field, c);
        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 0);
        add(button, c);
    }

    private void addRow(int rowy, Component label, Component field)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = rowy;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(label, c);
        c.gridx = 1;
        c.weightx = 0.5;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(field, c);
    }

    private void addRow(int rowy, Component comp)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = rowy;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0;
        add(comp, c);
    }

    private List<String> getDataSetTypes()
    {
        String[] dataSetTypes =
            { "Data Set Type 1", "Data Set Type 2", "Data Set Type 3" };
        return Arrays.asList(dataSetTypes);
    }
}
