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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetPropertiesPanel.Observer;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetMetadataPanel extends JPanel implements Observer
{
    private class AsynchronousValidator implements Runnable
    {
        public void run()
        {
            while (true)
            {
                try
                {
                    if (validationQueue.take())
                    {
                        // empty the queue
                        validationQueue.clear();

                        // perform the validation
                        validateAndNotifyObserversOfChanges();
                    }
                } catch (Throwable t)
                {
                    // ignore the error, thread cannot die
                }
            }
        }
    }

    private class ModificationDateChecker implements Runnable
    {
        private ValidatedFile file = null;

        public void run()
        {
            while (true)
            {
                try
                {
                    if (isVisible() && validationRequired())
                    {
                        validationQueue.add(Boolean.TRUE);
                    }
                    Thread.sleep(1000l);
                } catch (Throwable t)
                {
                    // ignore the error, thread cannot die
                }
            }
        }

        private synchronized boolean validationRequired()
        {
            if (file != null)
            {
                if (file.getFile().exists() && file.validationRequired())
                {
                    file.markValidation();
                    return true;
                }
            }
            return false;
        }

        public synchronized void observeFile(@SuppressWarnings("hiding") ValidatedFile file)
        {
            this.file = file;
        }

    }

    private static final String EMPTY_FILE_SELECTION = "";

    private static final long serialVersionUID = 1L;

    private final BlockingQueue<Boolean> validationQueue = new LinkedBlockingQueue<Boolean>();

    private final ModificationDateChecker modificationDateChecker = new ModificationDateChecker();

    private final JFrame mainWindow;

    private final DataSetUploadClientModel clientModel;

    private final JLabel ownerIdLabel;

    private final JTextField sampleIdText;

    private final ExperimentPickerPanel experimentPicker;

    private final JPanel ownerIdPanel;

    private final ButtonGroup ownerButtonGroup;

    private final JComboBox dataSetTypeComboBox;

    private final JPanel dataSetTypePanel;

    private final JLabel dataSetFileLabel;

    private final JComboBox dataSetFileComboBox;

    private final JButton dataSetFileButton;

    private final JButton dataSetFileRefreshButton;

    private final JRadioButton experimentButton;

    private final JRadioButton sampleButton;

    private final HashMap<String, DataSetPropertiesPanel> propertiesPanels =
            new HashMap<String, DataSetPropertiesPanel>();

    private final ErrorsPanel validationErrors;

    private NewDataSetInfo newDataSetInfo;

    public DataSetMetadataPanel(DataSetUploadClientModel clientModel, JFrame mainWindow)
    {
        super();

        new Thread(new AsynchronousValidator()).start();
        new Thread(modificationDateChecker).start();

        setLayout(new GridBagLayout());

        // Initialize internal state
        this.clientModel = clientModel;
        this.mainWindow = mainWindow;

        // Initialize the fields in the gui
        ownerIdLabel = new JLabel("Owner:", JLabel.TRAILING);
        sampleIdText = new JTextField();
        experimentPicker = new ExperimentPickerPanel(mainWindow, clientModel.getExperiments());

        ownerIdPanel = new JPanel(new CardLayout());

        ownerButtonGroup = new ButtonGroup();
        experimentButton = new JRadioButton("Experiment");
        sampleButton = new JRadioButton("Sample");
        ownerButtonGroup.add(experimentButton);
        ownerButtonGroup.add(sampleButton);

        ownerIdPanel.add(experimentPicker, experimentButton.getText());
        ownerIdPanel.add(sampleIdText, sampleButton.getText());

        dataSetTypeComboBox = new JComboBox();
        dataSetTypePanel = new JPanel();

        String[] initialOptions =
            { EMPTY_FILE_SELECTION };
        dataSetFileComboBox = new JComboBox(initialOptions);
        dataSetFileButton = new JButton("Browse...");
        dataSetFileRefreshButton = new JButton("Refresh");
        dataSetFileLabel = new JLabel("File:", JLabel.TRAILING);

        validationErrors = new ErrorsPanel(mainWindow);

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
            sampleIdText.setText(EMPTY_FILE_SELECTION);
            experimentPicker.setText(EMPTY_FILE_SELECTION);
            updateFileLabel();
            disableAllWidgets();
            return;
        }

        enableAllWidgets();

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        switch (builder.getDataSetOwnerType())
        {
            case EXPERIMENT:
                ownerButtonGroup.setSelected(experimentButton.getModel(), true);
                experimentPicker.setText(builder.getDataSetOwnerIdentifier());
                break;
            case SAMPLE:
                ownerButtonGroup.setSelected(sampleButton.getModel(), true);
                sampleIdText.setText(builder.getDataSetOwnerIdentifier());
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
        syncErrors();
    }

    private void enableAllWidgets()
    {
        for (JComponent widget : getAllEditableWidgets())
        {
            widget.setEnabled(true);
        }
    }

    private void disableAllWidgets()
    {
        for (JComponent widget : getAllEditableWidgets())
        {
            widget.setEnabled(false);
        }
    }

    private ArrayList<JComponent> getAllEditableWidgets()
    {
        ArrayList<JComponent> editableWidgets = new ArrayList<JComponent>();
        editableWidgets.add(sampleIdText);
        editableWidgets.add(experimentPicker);
        editableWidgets.add(dataSetFileButton);
        editableWidgets.add(experimentButton);
        editableWidgets.add(sampleButton);
        editableWidgets.add(experimentPicker);
        editableWidgets.add(dataSetTypeComboBox);

        for (DataSetPropertiesPanel panel : propertiesPanels.values())
        {
            editableWidgets.addAll(panel.getAllEditableWidgets());
        }

        return editableWidgets;
    }

    private void createGui()
    {
        // The file row
        dataSetFileLabel.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        dataSetFileComboBox.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        dataSetFileComboBox.addItemListener(new ItemListener()
            {

                public void itemStateChanged(ItemEvent e)
                {
                    if (null == newDataSetInfo)
                    {
                        return;
                    }

                    Object selectedItem = e.getItem();
                    if (selectedItem != newDataSetInfo.getNewDataSetBuilder().getFile())
                    {
                        if (null == selectedItem || EMPTY_FILE_SELECTION == selectedItem)
                        {
                            newDataSetInfo.getNewDataSetBuilder().setFile(null);
                            modificationDateChecker.observeFile(null);
                        } else
                        {
                            newDataSetInfo.getNewDataSetBuilder().setFile(
                                    ((ValidatedFile) selectedItem).getFile());
                            modificationDateChecker.observeFile((ValidatedFile) selectedItem);
                        }

                        validationQueue.add(Boolean.TRUE);
                    }
                }

            });
        dataSetFileButton.setPreferredSize(new Dimension(90, BUTTON_HEIGHT));
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
                        clientModel.userDidSelectFile(new ValidatedFile(newDirOrNull));
                        updateFileComboBoxList();
                        updateFileLabel();
                        validationQueue.add(Boolean.TRUE);
                    }
                }
            });
        dataSetFileRefreshButton.setPreferredSize(new Dimension(90, BUTTON_HEIGHT));
        dataSetFileRefreshButton.setToolTipText("File will be refreshed and revalidated");
        dataSetFileRefreshButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (null == newDataSetInfo
                            || newDataSetInfo.getNewDataSetBuilder().getFile() == null)
                    {
                        return;
                    }
                    validationQueue.add(Boolean.TRUE);
                }
            });
        addRow(1, dataSetFileLabel, dataSetFileComboBox, dataSetFileButton,
                dataSetFileRefreshButton);

        // The owner row
        ownerIdLabel.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));

        sampleIdText.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        sampleIdText.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    setOwnerId(sampleIdText.getText());
                }

            });
        sampleIdText.addFocusListener(new FocusListener()
            {

                public void focusLost(FocusEvent e)
                {
                    setOwnerId(sampleIdText.getText());
                }

                public void focusGained(FocusEvent e)
                {
                    // Do nothing
                }
            });

        experimentPicker.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        experimentPicker.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setOwnerId(experimentPicker.getText());
                }

            });
        experimentPicker.addFocusListener(new FocusListener()
            {
                public void focusLost(FocusEvent e)
                {
                    setOwnerId(experimentPicker.getText());
                }

                public void focusGained(FocusEvent e)
                {
                    // Do nothing
                }
            });

        experimentButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setOwnerType(DataSetOwnerType.EXPERIMENT);
                    setOwnerId(experimentPicker.getText());
                    CardLayout cardLayout = (CardLayout) ownerIdPanel.getLayout();
                    cardLayout.show(ownerIdPanel, experimentButton.getText());
                }
            });
        experimentButton.setSelected(true);
        sampleButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setOwnerType(DataSetOwnerType.SAMPLE);
                    setOwnerId(sampleIdText.getText());
                    CardLayout cardLayout = (CardLayout) ownerIdPanel.getLayout();
                    cardLayout.show(ownerIdPanel, sampleButton.getText());
                }
            });

        addRow(2, ownerIdLabel, ownerIdPanel, ownerButtonGroup);

        // The data set type row
        JLabel label = new JLabel("Data Set Type:", JLabel.TRAILING);
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
                    validationQueue.add(Boolean.TRUE);
                }
            });
        addRow(3, label, dataSetTypeComboBox);

        createDataSetTypePanel();
        addRow(4, dataSetTypePanel);

        addRow(5, validationErrors);
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

    private void updateFileComboBoxList()
    {
        dataSetFileComboBox.removeAllItems();
        ArrayList<ValidatedFile> files =
                new ArrayList<ValidatedFile>(clientModel.getUserSelectedFiles());
        for (ValidatedFile file : files)
        {
            dataSetFileComboBox.addItem(file);
        }
        dataSetFileComboBox.addItem(EMPTY_FILE_SELECTION);
    }

    protected void updateFileLabel()
    {
        if (null == newDataSetInfo)
        {
            // Select the empty string
            dataSetFileComboBox.setSelectedItem(EMPTY_FILE_SELECTION);
            return;
        }
        File file = newDataSetInfo.getNewDataSetBuilder().getFile();
        if (null != file)
        {
            dataSetFileComboBox.setSelectedItem(new ValidatedFile(file));
        } else
        {
            dataSetFileComboBox.setSelectedItem(EMPTY_FILE_SELECTION);
        }
    }

    private void createDataSetTypePanel()
    {

        dataSetTypePanel.setLayout(new CardLayout());
        for (DataSetType dataSetType : getDataSetTypes())
        {
            DataSetPropertiesPanel typeView = new DataSetPropertiesPanel(dataSetType, clientModel);
            typeView.registerObserver(this);
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

    private void addRow(int rowy, Component label, Component field, Component button,
            Component refreshButton)
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

        ++c.gridx;
        c.weightx = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(button, c);

        ++c.gridx;
        c.weightx = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 0);
        add(refreshButton, c);
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
        c.weighty = 1;
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

    private void validateAndNotifyObserversOfChanges()
    {
        validationErrors.waitCard();
        clientModel.validateNewDataSetInfoAndNotifyObservers(newDataSetInfo);
        syncErrors();
        validationErrors.showResult();
    }

    private void setOwnerType(DataSetOwnerType type)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        if (builder.getDataSetOwnerType() != type)
        {
            builder.setDataSetOwnerType(type);
            validationQueue.add(Boolean.TRUE);
        }
    }

    protected void setOwnerId(String text)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();

        if (false == text.equals(builder.getDataSetOwnerIdentifier()))
        {
            builder.setDataSetOwnerIdentifier(text);
            validationQueue.add(Boolean.TRUE);
        }
    }

    public synchronized void syncErrors()
    {
        // Clear all errors first
        clearError(ownerIdLabel, ownerIdPanel, null);
        clearError(dataSetFileLabel, dataSetFileComboBox, validationErrors);

        List<ValidationError> errors = newDataSetInfo.getValidationErrors();
        for (ValidationError error : errors)
        {
            switch (error.getTarget())
            {
                case DATA_SET_OWNER:
                    displayError(ownerIdLabel, ownerIdPanel, validationErrors, error);
                    break;

                case DATA_SET_TYPE:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_FILE:
                    displayError(dataSetFileLabel, dataSetFileComboBox, validationErrors, error);
                    break;

                case DATA_SET_PROPERTY:
                    // These are handled by the Properties Panel
                    break;
            }
        }

        for (DataSetPropertiesPanel panel : propertiesPanels.values())
        {
            panel.syncErrors();
        }
    }

    private void displayError(JLabel label, JComponent component, ErrorsPanel errorAreaOrNull,
            ValidationError error)
    {
        // Not all errors are applicable to this panel
        if (null == label || null == component)
        {
            return;
        }
        UiUtilities.displayError(label, component, errorAreaOrNull, error);
    }

    private void clearError(JLabel label, JComponent component, ErrorsPanel errorAreaOrNull)
    {
        UiUtilities.clearError(label, component, errorAreaOrNull);
        component.setToolTipText(label.getToolTipText());
    }

    public void update()
    {
        validationQueue.add(Boolean.TRUE);
    }

}
