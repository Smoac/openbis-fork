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
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;
import ch.systemsx.cisd.openbis.knime.common.AbstractOpenBisNodeDialog;
import ch.systemsx.cisd.openbis.knime.common.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.knime.common.EntityChooser;
import ch.systemsx.cisd.openbis.knime.common.Util;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetRegistrationNodeDialog extends AbstractOpenBisNodeDialog
{
    private static final class ValidationHandler
    {
        private final StringBuilder builder = new StringBuilder();
        private final SimplePropertyValidator validator;

        ValidationHandler(SimplePropertyValidator validator)
        {
            this.validator = validator;
        }
        
        String getErrorMessage()
        {
            return builder.toString();
        }
        
        void validate(PropertyType propertyType, String value)
        {
            if (value == null || value.trim().length() == 0)
            {
                if (propertyType.isMandatory())
                {
                    addErrorMessage(propertyType, "Missing mandatory value");
                }
            } else
            {
                DataTypeCode dataType = propertyType.getDataType();
                if (validator.canValidate(dataType))
                {
                    try
                    {
                        validator.validatePropertyValue(dataType, value);
                    } catch (UserFailureException ex)
                    {
                        addErrorMessage(propertyType, ex.getMessage());
                    }
                }
            }
        }
        
        private void addErrorMessage(PropertyType propertyType, String message)
        {
            builder.append("Invalid property '").append(propertyType.getLabel());
            builder.append("': ").append(message).append(".\n");
        }
    }
    
    private static final class DataSetTypeAdapter
    {
        private final DataSetType dataSetType;

        DataSetTypeAdapter(DataSetType dataSetType)
        {
            this.dataSetType = dataSetType;
        }

        public DataSetType getDataSetType()
        {
            return dataSetType;
        }

        @Override
        public String toString()
        {
            return dataSetType.getCode();
        }
    }
    
    private static final class TermAdapter
    {
        private final VocabularyTerm vocabularyTerm;

        TermAdapter(VocabularyTerm vocabularyTerm)
        {
            this.vocabularyTerm = vocabularyTerm;
        }

        public VocabularyTerm getVocabularyTerm()
        {
            return vocabularyTerm;
        }

        @Override
        public String toString()
        {
            return getVocabularyTerm().getLabel();
        }
    }

    private static enum FieldFactory
    {
        VOCABULARY_FIELD_FACTORY()
        {
            @Override
            boolean canCreate(PropertyType propertyType)
            {
                return propertyType instanceof ControlledVocabularyPropertyType;
            }

            @Override
            JComponent create(PropertyType propertyType, String valueOrNull)
            {
                List<VocabularyTerm> terms =
                        ((ControlledVocabularyPropertyType) propertyType).getTerms();
                JComboBox comboBox = new JComboBox();
                for (VocabularyTerm vocabularyTerm : terms)
                {
                    TermAdapter termAdapter = new TermAdapter(vocabularyTerm);
                    comboBox.addItem(termAdapter);
                    if (vocabularyTerm.getCode().equals(valueOrNull))
                    {
                        comboBox.setSelectedItem(termAdapter);
                    }
                }
                return comboBox;
            }
        },
        CHECK_BOX_FIELD_FACTORY()
        {
            @Override
            boolean canCreate(PropertyType propertyType)
            {
                return propertyType.getDataType() == DataTypeCode.BOOLEAN;
            }
            
            @Override
            JComponent create(PropertyType propertyType, String valueOrNull)
            {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.parseBoolean(valueOrNull));
                return checkBox;
            }
        },
        FIELD_FACTORY()
        {
            @Override
            boolean canCreate(PropertyType propertyType)
            {
                return true;
            }
            
            @Override
            JComponent create(PropertyType propertyType, String valueOrNull)
            {
                JTextField textField = new JTextField(10);
                if (valueOrNull != null)
                {
                    textField.setText(valueOrNull);
                }
                return textField;
            }
        },
        ;
        
        abstract boolean canCreate(PropertyType propertyType);
        
        abstract JComponent create(PropertyType propertyType, String valueOrNull);
    }
    
    private JComboBox ownerTypeComboBox;
    private JTextField ownerField;
    private JComboBox dataSetTypeComboBox;
    private Map<String, JComponent> propertyFieldRepository = new HashMap<String, JComponent>();
    private Map<String, JComponent> propertyFields = new HashMap<String, JComponent>();

    private JComponent propertiesLabel;

    protected DataSetRegistrationNodeDialog(IOpenbisServiceFacadeFactory serviceFacadeFactory)
    {
        super("Data Set Registration Settings", serviceFacadeFactory);
    }

    @Override
    protected void defineQueryForm(JPanel queryPanel)
    {
        JPanel fields = new JPanel(new GridBagLayout());
        ownerTypeComboBox = new JComboBox(DataSetOwnerType.values());
        ownerTypeComboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent event)
                {
                    setOwnerToolTip();
                    ownerField.setText("");
                }
            });
        addField(fields, "Owner type", ownerTypeComboBox)
            .setToolTipText("The type of owner the new data set will directly be linked to.");
        ownerField = new JTextField(20);
        setOwnerToolTip();
        JPanel textFieldWithButton = new JPanel(new BorderLayout());
        textFieldWithButton.add(ownerField, BorderLayout.CENTER);
        JButton button = new JButton("...");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    chooseOwner(createFacade());
                }
            });
        textFieldWithButton.add(button, BorderLayout.EAST);
        addField(fields, "Owner", textFieldWithButton)
                .setToolTipText("Owner is the experiment/sample/data set the new data set will directly be linked to.");
        dataSetTypeComboBox = new JComboBox();
        addField(fields, "Data Set Type", dataSetTypeComboBox);
        propertiesLabel = new JSeparator();
        fields.add(propertiesLabel, createLast());
        queryPanel.add(fields, BorderLayout.NORTH);
    }
    
    private void setOwnerToolTip()
    {
        DataSetOwnerType ownerType = getSelectedOwnerType();
        String ownerTypeName = null;
        switch (ownerType)
        {
            case EXPERIMENT: ownerTypeName = "an experiment"; break;
            case SAMPLE: ownerTypeName = "a sample"; break;
            case DATA_SET: ownerTypeName = "a data set"; break;
        }
        ownerField.setToolTipText("Choose " + ownerTypeName + " or keep it empty if flow variable "
                + Util.VARIABLE_PREFIX + ownerType.name() + " should be used.");
    }

    private DataSetOwnerType getSelectedOwnerType()
    {
        return (DataSetOwnerType) ownerTypeComboBox.getSelectedItem();
    }
    
    private void chooseOwner(IQueryApiFacade facade)
    {
        DataSetOwnerType ownerType = getSelectedOwnerType();
        String sessionToken = facade.getSessionToken();
        IGeneralInformationService service = facade.getGeneralInformationService();
        String ownerOrNull =
                new EntityChooser(ownerField, ownerType, true, sessionToken, service).getOwnerOrNull();
        if (ownerOrNull != null)
        {
            ownerField.setText(ownerOrNull);
        }
    }

    @Override
    protected void updateQueryForm(IQueryApiFacade queryFacade)
    {
        dataSetTypeComboBox.removeAllItems();
        List<DataSetType> dataSetTypes = createOpenbisFacade().listDataSetTypes();
        for (DataSetType dataSetType : dataSetTypes)
        {
            dataSetTypeComboBox.addItem(new DataSetTypeAdapter(dataSetType));
        }
        dataSetTypeComboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent event)
                {
                    changeDataSetType(((DataSetTypeAdapter) event.getItem()).getDataSetType(),
                            new HashMap<String, String>());
                }
            });
        if (dataSetTypes.isEmpty() == false)
        {
            dataSetTypeComboBox.setSelectedIndex(0);
            changeDataSetType(dataSetTypes.get(0), new HashMap<String, String>());
        }
    }
    
    private void changeDataSetType(DataSetType dataSetType, Map<String, String> properties)
    {
        Container parent = propertiesLabel.getParent();
        Component[] components = parent.getComponents();
        boolean removing = false;
        for (Component component : components)
        {
            if (removing)
            {
                parent.remove(component);
            } else if (component == propertiesLabel)
            {
                removing = true;
            }
        }
        propertyFields.clear();
        List<PropertyTypeGroup> propertyTypeGroups = dataSetType.getPropertyTypeGroups();
        for (PropertyTypeGroup propertyTypeGroup : propertyTypeGroups)
        {
            List<PropertyType> propertyTypes = propertyTypeGroup.getPropertyTypes();
            for (PropertyType propertyType : propertyTypes)
            {
                String code = propertyType.getCode();
                String valueOrNull = properties.get(code);
                JComponent field = getField(propertyType, valueOrNull);
                addField(parent, propertyType.getLabel(), field, propertyType.isMandatory());
                propertyFields.put(code, field);
            }
        }
        parent.invalidate();
        while (parent != null && parent instanceof JViewport == false)
        {
            parent = parent.getParent();
        }
        parent.validate();
    }
    
    private JComponent getField(PropertyType propertyType, String valueOrNull)
    {
        JComponent field = propertyFieldRepository.get(propertyType.getCode());
        if (field == null)
        {
            FieldFactory[] values = FieldFactory.values();
            for (FieldFactory fieldFactory : values)
            {
                if (fieldFactory.canCreate(propertyType))
                {
                    field = fieldFactory.create(propertyType, valueOrNull);
                    field.setToolTipText(propertyType.getDescription());
                    propertyFieldRepository.put(propertyType.getCode(), field);
                    break;
                }
            }
        }
        return field;
    }
        
    @Override
    protected void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
            throws NotConfigurableException
    {
        ownerTypeComboBox.setSelectedItem(DataSetOwnerType.valueOf(settings.getString(
                DataSetRegistrationNodeModel.OWNER_TYPE_KEY, DataSetOwnerType.EXPERIMENT.name())));
        ownerField.setText(settings.getString(DataSetRegistrationNodeModel.OWNER_KEY, ""));
        byte[] bytes = settings.getByteArray(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, null);
        DataSetType dataSetType = Util.deserializeDescription(bytes);
        if (dataSetType != null && dataSetTypeComboBox.getItemCount() == 0)
        {
            dataSetTypeComboBox.addItem(new DataSetTypeAdapter(dataSetType));
            dataSetTypeComboBox.setSelectedIndex(0);
            try
            {
                changeDataSetType(dataSetType, DataSetRegistrationNodeModel.getProperties(settings));
            } catch (InvalidSettingsException ex)
            {
                throw new NotConfigurableException("Couldn't get properties", ex);
            }
        }
    }
    
    @Override
    protected void saveAdditionalSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException
    {
        SimplePropertyValidator propertyValidator = new SimplePropertyValidator();
        settings.addString(DataSetRegistrationNodeModel.OWNER_TYPE_KEY,
                getSelectedOwnerType().name());
        settings.addString(DataSetRegistrationNodeModel.OWNER_KEY, ownerField.getText());
        DataSetTypeAdapter dataSetTypeAdapter =
                (DataSetTypeAdapter) dataSetTypeComboBox.getSelectedItem();
        if (dataSetTypeAdapter == null)
        {
            throw new InvalidSettingsException("Unspecified data set type.");
        }
        DataSetType dataSetType = dataSetTypeAdapter.getDataSetType();
        byte[] bytes = Util.serializeDescription(dataSetType);
        settings.addByteArray(DataSetRegistrationNodeModel.DATA_SET_TYPE_KEY, bytes);
        Map<String, PropertyType> propertyTypes = getPropertyTypes(dataSetType);
        List<String> propertyTypeCodes = new ArrayList<String>(propertyFields.size());
        List<String> propertyValues = new ArrayList<String>(propertyFields.size());
        ValidationHandler validationHandler = new ValidationHandler(propertyValidator);
        for (Entry<String, JComponent> entry : propertyFields.entrySet())
        {
            String propertyTypeCode = entry.getKey();
            propertyTypeCodes.add(propertyTypeCode);
            JComponent component = entry.getValue();
            String value = null;
            if (component instanceof JTextComponent)
            {
                JTextComponent textComponent = (JTextComponent) component;
                value = textComponent.getText();
            } else if (component instanceof JCheckBox)
            {
                JCheckBox checkBox = (JCheckBox) component;
                value = Boolean.toString(checkBox.isSelected());
            } else if (component instanceof JComboBox)
            {
                JComboBox comboBox = (JComboBox) component;
                value = ((TermAdapter) comboBox.getSelectedItem()).getVocabularyTerm().getCode();
            }
            PropertyType propertyType = propertyTypes.get(propertyTypeCode);
            validationHandler.validate(propertyType, value);
            propertyValues.add(value);
        }
        String errorMessage = validationHandler.getErrorMessage();
        if (errorMessage.length() > 0)
        {
            throw new InvalidSettingsException(errorMessage);
        }
        settings.addStringArray(DataSetRegistrationNodeModel.PROPERTY_TYPE_CODES_KEY,
                propertyTypeCodes.toArray(new String[0]));
        settings.addStringArray(DataSetRegistrationNodeModel.PROPERTY_VALUES_KEY,
                propertyValues.toArray(new String[0]));
    }
    
    private Map<String, PropertyType> getPropertyTypes(DataSetType dataSetType)
    {
        List<PropertyTypeGroup> propertyTypeGroups = dataSetType.getPropertyTypeGroups();
        Map<String, PropertyType> result = new HashMap<String, PropertyType>();
        for (PropertyTypeGroup propertyTypeGroup : propertyTypeGroups)
        {
            List<PropertyType> propertyTypes = propertyTypeGroup.getPropertyTypes();
            for (PropertyType propertyType : propertyTypes)
            {
                result.put(propertyType.getCode(), propertyType);
            }
        }
        return result;
    }

}
