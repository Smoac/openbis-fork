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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTOBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetPropertiesPanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    /**
     * An adaptor to convert VocabularyTerms into something that can be put into combo boxes.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static final class VocabularyTermAdaptor
    {
        private final VocabularyTerm term;

        private VocabularyTermAdaptor(VocabularyTerm term)
        {
            this.term = term;
        }

        @Override
        public String toString()
        {
            return term.getLabel();
        }
    }

    private final DataSetType dataSetType;

    private final DataSetUploadClientModel clientModel;

    private final HashMap<String, JComponent> formFields = new HashMap<String, JComponent>();

    private final HashMap<String, JLabel> labels = new HashMap<String, JLabel>();

    private NewDataSetInfo newDataSetInfo;

    public DataSetPropertiesPanel(DataSetType dataSetType, DataSetUploadClientModel clientModel)
    {
        super();
        this.dataSetType = dataSetType;
        this.clientModel = clientModel;
        setLayout(new GridBagLayout());
        createGui();
    }

    public NewDataSetInfo getDataSetInfo()
    {
        return newDataSetInfo;
    }

    public void setNewDataSetInfo(NewDataSetInfo dataSetInfo)
    {
        this.newDataSetInfo = dataSetInfo;
        syncGui();
    }

    /**
     * Return all editable widgets.
     */
    protected List<JComponent> getAllEditableWidgets()
    {
        ArrayList<JComponent> editableWidgets = new ArrayList<JComponent>(formFields.values());
        return editableWidgets;
    }

    private void createGui()
    {
        // Add the fields, two per row, to the GUI; ignore the groups for now
        int row = 0;
        int col = 0;
        List<PropertyTypeGroup> groups = dataSetType.getPropertyTypeGroups();
        for (PropertyTypeGroup group : groups)
        {
            for (PropertyType propertyType : group.getPropertyTypes())
            {
                addFormFieldForPropertyType(row, col, propertyType);
                if (++col > 1)
                {
                    // advance to the next row
                    ++row;
                    col = 0;
                }
            }
        }
    }

    private void addFormFieldForPropertyType(int row, int col, final PropertyType propertyType)
    {
        String labelString = getLabelStringForPropertyType(propertyType);
        JLabel label = new JLabel(labelString + ":", JLabel.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));
        label.setToolTipText(propertyType.getDescription());
        if (propertyType.isMandatory())
        {
            // Set the font to be bold/italic for required fields.
            label.setFont(label.getFont().deriveFont(Font.BOLD | Font.ITALIC));
        }

        final JComponent formField;
        if (propertyType instanceof ControlledVocabularyPropertyType)
        {
            formField = createComboBox((ControlledVocabularyPropertyType) propertyType);
        } else if (propertyType.getDataType() == DataTypeCode.BOOLEAN)
        {
            formField = createCheckBox(propertyType);
        } else
        {
            formField = createTextField(propertyType);
        }
        formField.setToolTipText(propertyType.getDescription());
        formField.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        addFormField(col, row, label, formField);
        labels.put(propertyType.getCode(), label);
        formFields.put(propertyType.getCode(), formField);
    }

    private JTextField createTextField(final PropertyType propertyType)
    {
        final JTextField textField = new JTextField();
        textField.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setPropertyValue(propertyType, textField.getText());
                }

            });
        textField.addFocusListener(new FocusListener()
            {
                public void focusLost(FocusEvent e)
                {
                    setPropertyValue(propertyType, textField.getText());
                }

                public void focusGained(FocusEvent e)
                {
                    // Do nothing
                }
            });
        return textField;
    }

    private JComboBox createComboBox(final ControlledVocabularyPropertyType propertyType)
    {
        final JComboBox comboBox = new JComboBox();
        for (VocabularyTerm term : propertyType.getTerms())
        {
            comboBox.addItem(new VocabularyTermAdaptor(term));
        }
        comboBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    setPropertyValue(propertyType,
                            ((VocabularyTermAdaptor) e.getItem()).term.getCode());
                }

            });
        return comboBox;
    }

    private JComponent createCheckBox(final PropertyType propertyType)
    {
        final JCheckBox checkBox = new JCheckBox();

        checkBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setPropertyValue(propertyType,
                            checkBox.isSelected() ? PropertyUtils.Boolean.TRUE.toString()
                                    : PropertyUtils.Boolean.FALSE.toString());
                }

            });
        return checkBox;
    }

    private String getLabelStringForPropertyType(PropertyType propertyType)
    {
        StringBuilder label = new StringBuilder();
        label.append(propertyType.getLabel());
        if (propertyType.isMandatory())
        {
            label.append("*");
        }
        return label.toString();
    }

    private void addFormField(int colx, int rowy, Component label, Component field)
    {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internally, we consume two columns, one for the label, one for the field
        c.gridx = colx * 2;
        c.gridy = rowy;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(label, c);
        c.gridx = c.gridx + 1;
        c.weightx = 0.5;
        c.insets = new Insets((rowy > 0) ? 5 : 0, 0, 0, 5);
        add(field, c);
    }

    private void setPropertyValue(PropertyType propertyType, String text)
    {
        if (null == newDataSetInfo)
        {
            return;
        }

        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, String> props = metadata.getProperties();
        HashMap<String, String> newProps = new HashMap<String, String>(props);
        if (null == text || text.trim().length() < 1)
        {
            newProps.remove(propertyType.getCode());
        } else
        {
            newProps.put(propertyType.getCode(), text);
        }
        metadata.setProperties(newProps);

        clientModel.validateNewDataSetInfoAndNotifyObservers(newDataSetInfo);
        syncErrors();
        clientModel.notifyObserversOfChanges(newDataSetInfo);
    }

    private void syncGui()
    {
        NewDataSetDTOBuilder builder = newDataSetInfo.getNewDataSetBuilder();
        NewDataSetMetadataDTO metadata = builder.getDataSetMetadata();
        Map<String, String> props = metadata.getProperties();
        for (String propertyTypeCode : formFields.keySet())
        {
            String propertyValue = props.get(propertyTypeCode);
            JComponent formField = formFields.get(propertyTypeCode);
            if (formField instanceof JTextField)
            {
                JTextField textField = (JTextField) formField;
                textField.setText(propertyValue);
            } else if (formField instanceof JComboBox)
            {
                JComboBox comboBox = (JComboBox) formField;
                for (int i = 0; i < comboBox.getItemCount(); ++i)
                {
                    VocabularyTermAdaptor adaptor = (VocabularyTermAdaptor) comboBox.getItemAt(i);
                    if (adaptor.term.getCode().equals(propertyValue))
                    {
                        comboBox.setSelectedIndex(i);
                    }
                }
            }
        }
    }

    public void syncErrors()
    {
        // Clear all errors first
        for (String key : labels.keySet())
        {
            clearError(labels.get(key), formFields.get(key));
        }

        List<ValidationError> errors = newDataSetInfo.getValidationErrors();
        for (ValidationError error : errors)
        {
            switch (error.getTarget())
            {
                case DATA_SET_OWNER:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_TYPE:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_FILE:
                    // These are handled by the Metadata Panel
                    break;

                case DATA_SET_PROPERTY:
                    JLabel label = labels.get(error.getPropertyCodeOrNull());
                    JComponent formField = formFields.get(error.getPropertyCodeOrNull());
                    displayError(label, formField, error);
                    break;
            }
        }
    }

    private void displayError(JLabel label, JComponent component, ValidationError error)
    {
        // Not all errors are applicable to this panel
        if (null == label || null == component)
        {
            return;
        }
        UiUtilities.displayError(label, component, error);
    }

    private void clearError(JLabel label, JComponent component)
    {
        UiUtilities.clearError(label, component);
        component.setToolTipText(label.getToolTipText());
    }

}
