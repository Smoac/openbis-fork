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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm.InfoBoxResetListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * The property type assignment panel.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeAssignmentForm extends LayoutContainer implements
        IDatabaseModificationObserver
{
    private static final String UNSUPPORTED_ENTITY_KIND = "Unsupported entity kind";

    private static final int LABEL_WIDTH = 130;

    private static final int FIELD_WIDTH = 400;

    private static final String PREFIX = "property-type-assignment_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTY_TYPE_ID_SUFFIX = "property_type";

    public static final String SAMPLE_TYPE_ID_SUFFIX = ID_PREFIX + "sample_type";

    public static final String EXPERIMENT_TYPE_ID_SUFFIX = ID_PREFIX + "experiment_type";

    public static final String MATERIAL_TYPE_ID_SUFFIX = ID_PREFIX + "material_type";

    public static final String DATA_SET_TYPE_ID_SUFFIX = ID_PREFIX + "data_set_type";

    public static final String MANDATORY_CHECKBOX_ID_SUFFIX = "mandatory_checkbox";

    public static final String SAVE_BUTTON_ID_SUFFIX = "save-button";

    protected static final String DEFAULT_VALUE_ID_PART = "default_value";

    protected static final String SECTION_VALUE_ID_PART = "section_value";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private ExperimentTypeSelectionWidget experimentTypeSelectionWidget;

    private MaterialTypeSelectionWidget materialTypeSelectionWidget;

    private DataSetTypeSelectionWidget dataSetTypeSelectionWidget;

    private PropertyTypeSelectionWidget propertyTypeSelectionWidget;

    private DatabaseModificationAwareField<?> defaultValueField;

    private CheckBox mandatoryCheckbox;

    private SectionSelectionWidget sectionSelectionWidget;

    private EntityTypePropertyTypeSelectionWidget etptSelectionWidget;

    private Button saveButton;

    private final InfoBox infoBox;

    private final FormPanel formPanel;

    private final CompositeDatabaseModificationObserver modificationManager;

    private final EntityKind entityKind;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, EntityKind entityKind)
    {
        PropertyTypeAssignmentForm form = new PropertyTypeAssignmentForm(viewContext, entityKind);
        return new DatabaseModificationAwareComponent(form, form.modificationManager);
    }

    private PropertyTypeAssignmentForm(final IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        this.entityKind = entityKind;
        this.modificationManager = new CompositeDatabaseModificationObserver();
        setLayout(new FlowLayout(5));
        setId(createId(entityKind));
        this.viewContext = viewContext;
        setScrollMode(Scroll.AUTO);
        add(infoBox = createInfoBox());
        add(formPanel = createFormPanel());
    }

    public static final String createId(EntityKind entityKind)
    {
        return ID_PREFIX + entityKind.name();
    }

    private String createChildId(String childSuffix)
    {
        return getId() + childSuffix;
    }

    private final static InfoBox createInfoBox()
    {
        final InfoBox infoBox = new InfoBox();
        return infoBox;
    }

    private PropertyTypeSelectionWidget getPropertyTypeWidget()
    {
        if (propertyTypeSelectionWidget == null)
        {
            propertyTypeSelectionWidget =
                    new PropertyTypeSelectionWidget(viewContext,
                            createChildId(PROPERTY_TYPE_ID_SUFFIX));
            propertyTypeSelectionWidget
                    .addListener(Events.Focus, new InfoBoxResetListener(infoBox));
            FieldUtil.markAsMandatory(propertyTypeSelectionWidget);
            propertyTypeSelectionWidget.addListener(Events.SelectionChange,
                    new Listener<BaseEvent>()
                        {
                            public void handleEvent(BaseEvent be)
                            {
                                updatePropertyTypeRelatedFields();
                            }
                        });
        }
        return propertyTypeSelectionWidget;
    }

    private DropDownList<?, ?> getTypeSelectionWidget()
    {
        DropDownList<?, ?> result = null;
        switch (entityKind)
        {
            case EXPERIMENT:
                if (experimentTypeSelectionWidget == null)
                {
                    experimentTypeSelectionWidget =
                            new ExperimentTypeSelectionWidget(viewContext,
                                    EXPERIMENT_TYPE_ID_SUFFIX);
                    experimentTypeSelectionWidget.addListener(Events.Focus,
                            new InfoBoxResetListener(infoBox));
                    FieldUtil.markAsMandatory(experimentTypeSelectionWidget);
                }
                result = experimentTypeSelectionWidget;
                break;
            case SAMPLE:
                if (sampleTypeSelectionWidget == null)
                {
                    sampleTypeSelectionWidget =
                            new SampleTypeSelectionWidget(viewContext, SAMPLE_TYPE_ID_SUFFIX, false);
                    sampleTypeSelectionWidget.addListener(Events.Focus, new InfoBoxResetListener(
                            infoBox));
                    FieldUtil.markAsMandatory(sampleTypeSelectionWidget);
                }
                result = sampleTypeSelectionWidget;
                break;
            case MATERIAL:
                if (materialTypeSelectionWidget == null)
                {
                    materialTypeSelectionWidget =
                            new MaterialTypeSelectionWidget(viewContext, MATERIAL_TYPE_ID_SUFFIX);
                    materialTypeSelectionWidget.addListener(Events.Focus, new InfoBoxResetListener(
                            infoBox));
                    FieldUtil.markAsMandatory(materialTypeSelectionWidget);
                }
                result = materialTypeSelectionWidget;
                break;
            case DATA_SET:
                if (dataSetTypeSelectionWidget == null)
                {
                    dataSetTypeSelectionWidget =
                            new DataSetTypeSelectionWidget(viewContext, DATA_SET_TYPE_ID_SUFFIX);
                    dataSetTypeSelectionWidget.addListener(Events.Focus, new InfoBoxResetListener(
                            infoBox));
                    FieldUtil.markAsMandatory(dataSetTypeSelectionWidget);
                }
                result = dataSetTypeSelectionWidget;
                break;
        }
        if (result == null)
        {
            throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
        } else
        {
            result.addListener(Events.SelectionChange, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        updatePropertyTypeEntityTypeRelatedFields();
                    }
                });
            return result;
        }
    }

    private CheckBox getMandatoryCheckbox()
    {
        if (mandatoryCheckbox == null)
        {
            mandatoryCheckbox = new CheckBox();
            mandatoryCheckbox.setId(createChildId(MANDATORY_CHECKBOX_ID_SUFFIX));
            mandatoryCheckbox.setFieldLabel(viewContext.getMessage(Dict.MANDATORY));
            mandatoryCheckbox.setValue(false);
            mandatoryCheckbox.addListener(Events.Change, new InfoBoxResetListener(infoBox));
        }
        return mandatoryCheckbox;
    }

    private final FormPanel createFormPanel()
    {
        final FormPanel panel = new FormPanel();
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setWidth(LABEL_WIDTH + FIELD_WIDTH + 40);
        panel.setLabelWidth(LABEL_WIDTH);
        panel.setFieldWidth(FIELD_WIDTH);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        saveButton = new Button(viewContext.getMessage(Dict.BUTTON_SAVE));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(createChildId(SAVE_BUTTON_ID_SUFFIX));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    submitForm();
                }
            });
        final Button resetButton = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    resetForm();
                }
            });
        panel.addButton(resetButton);
        panel.addButton(saveButton);
        return panel;
    }

    private String getSectionValue()
    {
        if (sectionSelectionWidget != null)
        {
            return sectionSelectionWidget.getSimpleValue();
        }
        return null;
    }

    private String getDefaultValue()
    {
        if (defaultValueField != null)
        {
            return PropertyFieldFactory.valueToString(defaultValueField.get().getValue());
        }
        return null;
    }

    private String getSelectedEntityCode()
    {
        return tryGetSelectedEntityType().getCode();
    }

    /**
     * extracts ordinal of an entity type property type after which new property will be added
     */
    private Long getPreviousETPTOrdinal()
    {
        return etptSelectionWidget.getSelectedEntityTypePropertyTypeOrdinal();
    }

    private EntityType tryGetSelectedEntityType()
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return experimentTypeSelectionWidget.tryGetSelectedExperimentType();
            case SAMPLE:
                return sampleTypeSelectionWidget.tryGetSelectedSampleType();
            case MATERIAL:
                return materialTypeSelectionWidget.tryGetSelectedMaterialType();
            case DATA_SET:
                return dataSetTypeSelectionWidget.tryGetSelectedDataSetType();
        }
        throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
    }

    private final void addFormFields()
    {
        PropertyTypeSelectionWidget propertyTypeWidget = getPropertyTypeWidget();
        DropDownList<?, ?> typeSelectionWidget = getTypeSelectionWidget();
        formPanel.add(propertyTypeWidget);
        formPanel.add(typeSelectionWidget);
        formPanel.add(getMandatoryCheckbox());
        updatePropertyTypeRelatedFields();

        modificationManager.addObserver(propertyTypeWidget);
        modificationManager.addObserver(typeSelectionWidget);
        if (defaultValueField != null)
        {
            modificationManager.addObserver(defaultValueField);
        }
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    public final class AssignPropertyTypeCallback extends AbstractAsyncCallback<String>
    {
        AssignPropertyTypeCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<String>(infoBox));
            saveButton.disable();
        }

        @Override
        protected final void process(final String result)
        {
            infoBox.displayInfo(result);
            resetForm();
            saveButton.enable();
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            super.finishOnFailure(caught);
            saveButton.enable();
        }
    }

    private void updatePropertyTypeRelatedFields()
    {
        hidePropertyTypeRelatedFields();
        final PropertyType propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
        if (propertyType != null)
        {
            String fieldId =
                    createChildId(DEFAULT_VALUE_ID_PART + propertyType.isInternalNamespace()
                            + propertyType.getSimpleCode());
            DatabaseModificationAwareField<?> fieldHolder =
                    PropertyFieldFactory.createField(propertyType, false, viewContext
                            .getMessage(Dict.DEFAULT_VALUE), fieldId, null, viewContext);
            fieldHolder.get().setToolTip(viewContext.getMessage(Dict.DEFAULT_VALUE_TOOLTIP));
            defaultValueField = fieldHolder;
            defaultValueField.get().show();
            formPanel.add(defaultValueField.get());
        }
        layout();
        updatePropertyTypeEntityTypeRelatedFields();
    }

    private void hidePropertyTypeRelatedFields()
    {
        if (defaultValueField != null)
        {
            Field<?> field = defaultValueField.get();
            field.hide();
            formPanel.remove(field);
            defaultValueField = null;
        }
        hideEntityTypePropertyTypeRelatedFields();
    }

    //

    private void updatePropertyTypeEntityTypeRelatedFields()
    {
        hideEntityTypePropertyTypeRelatedFields();
        final PropertyType propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
        final EntityType entityType = tryGetSelectedEntityType();
        if (propertyType != null && entityType != null)
        {
            final List<EntityTypePropertyType<?>> etpts =
                    new ArrayList<EntityTypePropertyType<?>>(entityType.getAssignedPropertyTypes());
            sectionSelectionWidget = createSectionSelectionWidget(etpts);
            formPanel.add(sectionSelectionWidget);
            etptSelectionWidget = createETPTSelectionWidget(etpts);
            formPanel.add(etptSelectionWidget);
        }
        layout();
    }

    private void hideEntityTypePropertyTypeRelatedFields()
    {
        if (sectionSelectionWidget != null && etptSelectionWidget != null)
        {
            sectionSelectionWidget.hide();
            etptSelectionWidget.hide();
            formPanel.remove(sectionSelectionWidget);
            formPanel.remove(etptSelectionWidget);
            sectionSelectionWidget = null;
            etptSelectionWidget = null;
        }
    }

    private EntityTypePropertyTypeSelectionWidget createETPTSelectionWidget(
            List<EntityTypePropertyType<?>> etpts)
    {
        // by default - append
        etpts.add(0, null); // null will be transformed into '(top)'
        final String lastCode =
                (etpts.size() > 1) ? etpts.get(etpts.size() - 1).getPropertyType().getCode()
                        : EntityTypePropertyTypeSelectionWidget.TOP_ITEM_CODE;
        final EntityTypePropertyTypeSelectionWidget result =
                new EntityTypePropertyTypeSelectionWidget(viewContext, getId(), etpts, lastCode);
        FieldUtil.setMandatoryFlag(result, true);
        return result;
    }

    private SectionSelectionWidget createSectionSelectionWidget(
            List<EntityTypePropertyType<?>> etpts)
    {
        return SectionSelectionWidget.create(viewContext, etpts);
    }

    //

    private final void submitForm()
    {
        if (formPanel.isValid())
        {
            viewContext.getService().assignPropertyType(entityKind,
                    propertyTypeSelectionWidget.tryGetSelectedPropertyTypeCode(),
                    getSelectedEntityCode(), getMandatoryCheckbox().getValue(), getDefaultValue(),
                    getSectionValue(), getPreviousETPTOrdinal(),
                    new AssignPropertyTypeCallback(viewContext));
        }
    }

    private void resetForm()
    {
        formPanel.reset();
        updatePropertyTypeRelatedFields();
        // need to refresh list of assigned property types
        getTypeSelectionWidget().refreshStore();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return modificationManager.getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        modificationManager.update(observedModifications);
    }

}
