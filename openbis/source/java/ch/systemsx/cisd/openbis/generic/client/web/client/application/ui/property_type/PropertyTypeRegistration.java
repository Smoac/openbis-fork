/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.VocabularyModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationFieldSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Vocabulary;

/**
 * A {@link LayoutContainer} extension for registering a new property type.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeRegistration extends AbstractRegistrationForm
{
    private static final String PREFIX = "property-type-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField propertyTypeCodeField;

    private VarcharField propertyTypeLabelField;

    private VarcharField propertyTypeDescriptionField;

    private DataTypeSelectionWidget dataTypeSelectionWidget;

    private VocabularySelectionWidget vocabularySelectionWidget;

    private VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    public PropertyTypeRegistration(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_PREFIX, DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        addFields();
    }

    private final VocabularyRegistrationFieldSet createVocabularyRegistrationFieldSet()
    {
        final VocabularyRegistrationFieldSet fieldSet =
                new VocabularyRegistrationFieldSet(viewContext, getId() + "_", labelWidth,
                        fieldWitdh - 40);
        fieldSet.setVisible(false);
        return fieldSet;
    }

    private final CodeField createCodeField()
    {
        final CodeField codeField =
                new CodeField(viewContext, viewContext.getMessage(Dict.CODE),
                        CodeField.CODE_PATTERN_WITH_DOT);
        return codeField;
    }

    private final void addFields()
    {
        formPanel.add(propertyTypeCodeField = createCodeField());
        formPanel.add(propertyTypeLabelField = createPropertyTypeLabelField());
        formPanel.add(propertyTypeDescriptionField =
                createDescriptionField(viewContext.getMessage(Dict.DESCRIPTION), true));
        formPanel.add(dataTypeSelectionWidget = createDataTypeSelectionWidget());
        vocabularyRegistrationFieldSet = createVocabularyRegistrationFieldSet();
        formPanel.add(vocabularySelectionWidget = createVocabularySelectionWidget());
        formPanel.add(vocabularyRegistrationFieldSet);
    }

    private final DataTypeSelectionWidget createDataTypeSelectionWidget()
    {
        final DataTypeSelectionWidget selectionWidget =
                new DataTypeSelectionWidget(viewContext, true);
        selectionWidget.addSelectionChangedListener(new DataTypeSelectionChangedListener());
        return selectionWidget;
    }

    private final VocabularySelectionWidget createVocabularySelectionWidget()
    {
        final VocabularySelectionWidget selectionWidget =
                new VocabularySelectionWidgetForPropertyTypeRegistration(viewContext,
                        vocabularyRegistrationFieldSet);
        selectionWidget.setVisible(false);
        return selectionWidget;
    }

    private final VarcharField createDescriptionField(final String descriptionLabel,
            final boolean mandatory)
    {
        final VarcharField varcharField = new VarcharField(descriptionLabel, mandatory);
        varcharField.setMaxLength(80);
        return varcharField;
    }

    private final VarcharField createPropertyTypeLabelField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.LABEL), true);
        varcharField.setMaxLength(40);
        return varcharField;
    }

    private final String getPropertyTypeCode()
    {
        final String prepend = PropertyType.USER_NAMESPACE_CODE_PREPEND;
        final String value = propertyTypeCodeField.getValue();
        if (value.toUpperCase().startsWith(prepend))
        {
            return value;
        }
        return prepend + value;
    }

    private final PropertyType createPropertyType()
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setCode(getPropertyTypeCode());
        propertyType.setLabel(propertyTypeLabelField.getValue());
        propertyType.setDescription(propertyTypeDescriptionField.getValue());
        final DataType selectedDataType = dataTypeSelectionWidget.tryGetSelectedDataType();
        propertyType.setDataType(selectedDataType);
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(selectedDataType.getCode()))
        {
            final VocabularyModel vocabulary =
                    GWTUtils.tryGetSingleSelectedModel(vocabularySelectionWidget);
            if (VocabularyModel.NEW_VOCABULARY_CODE.equals(vocabulary
                    .get(ModelDataPropertyNames.CODE)))
            {
                propertyType.setVocabulary(vocabularyRegistrationFieldSet.createVocabulary());
            } else
            {
                propertyType.setVocabulary((Vocabulary) vocabulary
                        .get(ModelDataPropertyNames.OBJECT));
            }
        }
        return propertyType;
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final PropertyType propertyType = createPropertyType();
        viewContext.getService().registerPropertyType(propertyType,
                new PropertyTypeRegistrationCallback(viewContext, propertyType));
    }

    //
    // Helper classes
    //

    private final class DataTypeSelectionChangedListener extends
            SelectionChangedListener<DataTypeModel>
    {

        //
        // SelectionChangedListener
        //

        @Override
        public final void selectionChanged(final SelectionChangedEvent<DataTypeModel> se)
        {
            final DataTypeModel selectedItem = se.getSelectedItem();
            final boolean visible;
            if (selectedItem != null)
            {
                visible =
                        selectedItem.get(ModelDataPropertyNames.CODE).equals(
                                DataTypeCode.CONTROLLEDVOCABULARY.name());
            } else
            {
                visible = false;
            }
            vocabularySelectionWidget.reset();
            vocabularySelectionWidget.setVisible(visible);
            vocabularySelectionWidget.setAllowBlank(!visible);
        }
    }

    public final class PropertyTypeRegistrationCallback extends AbstractAsyncCallback<Void>
    {
        private final PropertyType propertyType;

        PropertyTypeRegistrationCallback(final IViewContext<?> viewContext,
                final PropertyType propertyType)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
            this.propertyType = propertyType;
        }

        private final String createMessage()
        {
            return "Property type <b>" + propertyType.getCode() + "</b> successfully registered.";
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createMessage());
            formPanel.reset();
        }
    }

}
