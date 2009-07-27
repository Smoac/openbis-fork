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
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.AbstractVocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A {@link LayoutContainer} extension for registering a new property type. It also extends
 * {@link AbstractVocabularyRegistrationForm} because vocabulary registration is possible through
 * this form too.
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
public final class PropertyTypeRegistrationForm extends AbstractRegistrationForm
{
    public static final String ID = GenericConstants.ID_PREFIX + "property-type-registration_form";

    private final CodeField propertyTypeCodeField;

    private final VarcharField propertyTypeLabelField;

    private final MultilineVarcharField propertyTypeDescriptionField;

    private final DataTypeSelectionWidget dataTypeSelectionWidget;

    private final VocabularySelectionWidget vocabularySelectionWidget;

    private final MaterialTypeSelectionWidget materialTypeSelectionWidget;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        PropertyTypeRegistrationForm form = new PropertyTypeRegistrationForm(viewContext);
        IDatabaseModificationObserver observer = form.createDatabaseModificationObserver();
        return new DatabaseModificationAwareComponent(form, observer);
    }

    private PropertyTypeRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID);
        this.viewContext = viewContext;

        this.propertyTypeCodeField = createPropertyTypeCodeField();
        this.propertyTypeLabelField = createPropertyTypeLabelField();
        this.propertyTypeDescriptionField = createPropertyTypeDescriptionField();
        this.dataTypeSelectionWidget = createDataTypeSelectionWidget();
        this.vocabularySelectionWidget = createVocabularySelectionWidget();
        this.materialTypeSelectionWidget = createMaterialTypeSelectionField(viewContext);

        vocabularySelectionWidget.setVisible(false);
        materialTypeSelectionWidget.setVisible(false);

        formPanel.add(propertyTypeCodeField);
        formPanel.add(propertyTypeLabelField);
        formPanel.add(propertyTypeDescriptionField);
        formPanel.add(dataTypeSelectionWidget);
        formPanel.add(vocabularySelectionWidget);
        formPanel.add(materialTypeSelectionWidget);
    }

    private static MaterialTypeSelectionWidget createMaterialTypeSelectionField(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        String label = viewContext.getMessage(Dict.ALLOW_ANY_TYPE);
        MaterialTypeSelectionWidget chooser =
                MaterialTypeSelectionWidget.createWithAdditionalOption(viewContext, label, ID);
        FieldUtil.markAsMandatory(chooser);
        return chooser;
    }

    private final CodeField createPropertyTypeCodeField()
    {
        final CodeField codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE));
        codeField.setId(getId() + "_code");
        return codeField;
    }

    private final VarcharField createPropertyTypeLabelField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.LABEL), true);
        varcharField.setId(getId() + "_label");
        varcharField.setMaxLength(40);
        return varcharField;
    }

    private final MultilineVarcharField createPropertyTypeDescriptionField()
    {
        return new DescriptionField(viewContext, true, getId());
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
        VocabularySelectionWidget result = new VocabularySelectionWidget(viewContext);
        FieldUtil.markAsMandatory(result);
        return result;
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
        propertyType.setVocabulary(tryGetSelectedVocabulary(selectedDataType));
        propertyType.setMaterialType(tryGetSelectedMaterialTypeProperty(selectedDataType));
        return propertyType;
    }

    private MaterialType tryGetSelectedMaterialTypeProperty(DataType selectedDataType)
    {
        if (DataTypeCode.MATERIAL.equals(selectedDataType.getCode()))
        {
            return materialTypeSelectionWidget.tryGetSelected();
        } else
        {
            return null;
        }
    }

    private Vocabulary tryGetSelectedVocabulary(DataType selectedDataType)
    {
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(selectedDataType.getCode()))
        {
            return (Vocabulary) GWTUtils.tryGetSingleSelected(vocabularySelectionWidget);
        } else
        {
            return null;
        }
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
            DataTypeModel selectedItem = se.getSelectedItem();

            boolean isVocabularySelected =
                    isDataTypeCodeSelected(selectedItem, DataTypeCode.CONTROLLEDVOCABULARY);
            changeDataTypeSelectorVisibility(vocabularySelectionWidget, isVocabularySelected);

            boolean isMaterialTypeSelected =
                    isDataTypeCodeSelected(selectedItem, DataTypeCode.MATERIAL);
            changeDataTypeSelectorVisibility(materialTypeSelectionWidget, isMaterialTypeSelected);
        }

        private void changeDataTypeSelectorVisibility(TextField<?> field, final boolean isVisible)
        {
            field.reset();
            field.setVisible(isVisible);
            field.setAllowBlank(!isVisible);
        }
    }

    private static boolean isDataTypeCodeSelected(final DataTypeModel selectedItemOrNull,
            DataTypeCode dataTypeCode)
    {
        return selectedItemOrNull != null
                && selectedItemOrNull.get(ModelDataPropertyNames.CODE).equals(dataTypeCode.name());
    }

    public final class PropertyTypeRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final PropertyType propertyType;

        PropertyTypeRegistrationCallback(final IViewContext<?> viewContext,
                final PropertyType propertyType)
        {
            super(viewContext);
            this.propertyType = propertyType;
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Void result)
        {
            return "Property type <b>" + propertyType.getCode() + "</b> successfully registered.";
        }
    }

    public IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        observer.addObserver(vocabularySelectionWidget);
        observer.addObserver(materialTypeSelectionWidget);
        return observer;
    }

}
