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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetMetadataPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    private final JFrame mainWindow;

    private final DataSetUploadClientModel clientModel;

    private final JTextField ownerIdText;

    private final ButtonGroup ownerButtonGroup;

    private final JComboBox dataSetTypeComboBox;

    private final JPanel dataSetTypePanel;

    private final JButton dataSetFileButton;

    private final JRadioButton experimentButton;

    private final JRadioButton sampleButton;

    private final HashMap<String, DataSetPropertiesPanel> propertiesPanels =
            new HashMap<String, DataSetPropertiesPanel>();

    private NewDataSetInfo newDataSetInfo;

    public DataSetMetadataPanel(DataSetUploadClientModel clientModel, JFrame mainWindow)
    {
        super();
        setLayout(new GridBagLayout());

        // Initialize internal state
        this.clientModel = clientModel;
        this.mainWindow = mainWindow;

        // Initialize the fields in the gui
        ownerIdText = new JTextField();
        ownerButtonGroup = new ButtonGroup();
        experimentButton = new JRadioButton("Experiment");
        sampleButton = new JRadioButton("Sample");
        ownerButtonGroup.add(experimentButton);
        ownerButtonGroup.add(sampleButton);

        dataSetTypeComboBox = new JComboBox();
        dataSetTypePanel = new JPanel();
        dataSetFileButton = new JButton("");

        createGui();
    }

    public void setNewDataSetInfo(NewDataSetInfo newDataSetInfo)
    {
        this.newDataSetInfo = newDataSetInfo;
        syncGui();
    }

    private void syncGui()
    {
        if (null == newDataSetInfo)
        {
            ownerIdText.setText("");
            updateFileLabel();
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        ownerIdText.setText(builder.getDataSetOwnerIdentifier());
        switch (builder.getDataSetOwnerType())
        {
            case EXPERIMENT:
                ownerButtonGroup.setSelected(experimentButton.getModel(), true);
                break;
            case SAMPLE:
                ownerButtonGroup.setSelected(sampleButton.getModel(), true);
                break;
        }

        String dataSetTypeOrNull = builder.getDataSetMetadata().tryDataSetType();
        dataSetTypeComboBox.setSelectedIndex(clientModel.getIndexOfDataSetType(dataSetTypeOrNull));
        if (null != dataSetTypeOrNull)
        {
            CardLayout cardLayout = (CardLayout) dataSetTypePanel.getLayout();
            cardLayout.show(dataSetTypePanel, dataSetTypeOrNull);
        }

        for (DataSetPropertiesPanel propertiesPanel : propertiesPanels.values())
        {
            propertiesPanel.setNewDataSetInfo(newDataSetInfo);
        }

        updateFileLabel();
    }

    private void createGui()
    {
        // The file row
        JLabel label = new JLabel("File:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        dataSetFileButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        dataSetFileButton.setToolTipText("The file to upload.");
        dataSetFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (null == newDataSetInfo)
                    {
                        return;
                    }
                    final File newDirOrNull =
                            ch.systemsx.cisd.common.gui.FileChooserUtils.tryChooseFileOrDirectory(
                                    getMainWindow(), newDataSetInfo.getNewDataSetBuilder()
                                            .getFile());
                    if (newDirOrNull != null)
                    {
                        newDataSetInfo.getNewDataSetBuilder().setFile(newDirOrNull);
                        updateFileLabel();
                        notifyObserversOfChanges();
                    }
                }

            });
        addRow(1, label, dataSetFileButton);

        // The owner row
        label = new JLabel("Owner:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        ownerIdText.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        ownerIdText.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    setOwnerId(ownerIdText.getText());
                }

            });

        experimentButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setOwnerType(DataSetOwnerType.EXPERIMENT);

                }
            });
        experimentButton.setSelected(true);
        sampleButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setOwnerType(DataSetOwnerType.SAMPLE);
                }
            });

        addRow(2, label, ownerIdText, ownerButtonGroup);

        // The data set type row
        label = new JLabel("Data Set Type:", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        dataSetTypeComboBox.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        dataSetTypeComboBox.setToolTipText("Select the data set type for the experiment");
        for (DataSetType dataSetType : getDataSetTypes())
        {
            dataSetTypeComboBox.addItem(dataSetType.getCode());
        }
        dataSetTypeComboBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    setDataSetType((String) e.getItem());
                    CardLayout cardLayout = (CardLayout) dataSetTypePanel.getLayout();
                    cardLayout.show(dataSetTypePanel, (String) e.getItem());
                }
            });
        addRow(3, label, dataSetTypeComboBox);

        createDataSetTypePanel();
        addRow(4, dataSetTypePanel);
    }

    private void setDataSetType(String dataSetType)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        newDataSetInfo.getNewDataSetBuilder().getDataSetMetadata()
                .setDataSetTypeOrNull(dataSetType);
        clientModel.notifyObserversOfChanges(newDataSetInfo);
    }

    protected void updateFileLabel()
    {
        if (null == newDataSetInfo)
        {
            dataSetFileButton.setText("");
            return;
        }
        File file = newDataSetInfo.getNewDataSetBuilder().getFile();
        String filePath = (null != file) ? file.getAbsolutePath() : "";
        dataSetFileButton.setText(filePath);
    }

    private void createDataSetTypePanel()
    {
        dataSetTypePanel.setLayout(new CardLayout());
        for (DataSetType dataSetType : getDataSetTypes())
        {
            DataSetPropertiesPanel typeView = new DataSetPropertiesPanel(dataSetType, clientModel);
            dataSetTypePanel.add(typeView, dataSetType.getCode());
            propertiesPanels.put(dataSetType.getCode(), typeView);
        }
    }

    private void addRow(int rowy, Component label, Component field, ButtonGroup buttonGroup)
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

        AbstractButton button;
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while (buttons.hasMoreElements())
        {
            button = buttons.nextElement();
            ++c.gridx;
            c.weightx = 0;
            c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, buttons.hasMoreElements() ? 5 : 0);
            add(button, c);
        }
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
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(comp, c);
    }

    private List<DataSetType> getDataSetTypes()
    {
        return clientModel.getDataSetTypes();
    }

    private Frame getMainWindow()
    {
        return mainWindow;
    }

    private void notifyObserversOfChanges()
    {
        clientModel.notifyObserversOfChanges(newDataSetInfo);
    }

    private void setOwnerType(DataSetOwnerType type)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        builder.setDataSetOwnerType(type);

        notifyObserversOfChanges();
    }

    protected void setOwnerId(String text)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        builder.setDataSetOwnerIdentifier(text);
        notifyObserversOfChanges();
    }
}
