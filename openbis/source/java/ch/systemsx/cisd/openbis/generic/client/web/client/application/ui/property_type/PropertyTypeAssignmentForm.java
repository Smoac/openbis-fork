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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.PropertyFieldFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * The property type assignment panel.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeAssignmentForm extends LayoutContainer
{
    private static final String UNSUPPORTED_ENTITY_KIND = "Unsupported entity kind";

    private static final int LABEL_WIDTH = 130;

    private static final int FIELD_WIDTH = 400;

    private static final String PREFIX = "property-type-assignment_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private static final String PROPERTY_TYPE_ID_SUFFIX = "property_type";

    private static final String SAMPLE_TYPE_ID_SUFFIX = ID_PREFIX + "sample_type";

    private static final String EXPERIMENT_TYPE_ID_SUFFIX = ID_PREFIX + "experiment_type";

    private static final String MANDATORY_CHECKBOX_ID_SUFFIX = "mandatory_checkbox";

    public static final String SAVE_BUTTON_ID_SUFFIX = "save-button";

    protected static final String DEFAULT_VALUE_ID_PART = "default_value";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private ExperimentTypeSelectionWidget experimentTypeSelectionWidget;

    private PropertyTypeSelectionWidget propertyTypeSelectionWidget;

    private Field<?> defaultValueField;

    private CheckBox mandatoryCheckbox;

    private InfoBox infoBox;

    private FormPanel formPanel;

    private EntityKind entityKind;

    public PropertyTypeAssignmentForm(final IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        this.entityKind = entityKind;
        setLayout(new FlowLayout(5));
        setId(ID_PREFIX + entityKind.name());
        this.viewContext = viewContext;
        setScrollMode(Scroll.AUTO);
        add(infoBox = createInfoBox());
        add(formPanel = createFormPanel());
    }

    private String createId(String childSuffix)
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
                    new PropertyTypeSelectionWidget(viewContext, createId(PROPERTY_TYPE_ID_SUFFIX));
            propertyTypeSelectionWidget
                    .addListener(Events.Focus, new InfoBoxResetListener(infoBox));
            propertyTypeSelectionWidget.setAllowBlank(false);
            propertyTypeSelectionWidget
                    .setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
            propertyTypeSelectionWidget.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        updateDefaultField();
                    }
                });
        }
        return propertyTypeSelectionWidget;
    }

    private ComboBox<?> getTypeSelectionWidget()
    {
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
                    experimentTypeSelectionWidget.setAllowBlank(false);
                    experimentTypeSelectionWidget
                            .setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
                }
                return experimentTypeSelectionWidget;
            case SAMPLE:
                if (sampleTypeSelectionWidget == null)
                {
                    sampleTypeSelectionWidget =
                            new SampleTypeSelectionWidget(viewContext, SAMPLE_TYPE_ID_SUFFIX, false);
                    sampleTypeSelectionWidget.addListener(Events.Focus, new InfoBoxResetListener(
                            infoBox));
                    sampleTypeSelectionWidget.setAllowBlank(false);
                    sampleTypeSelectionWidget
                            .setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
                }
                return sampleTypeSelectionWidget;
            default:
                throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
        }
    }

    private CheckBox getMandatoryCheckbox()
    {
        if (mandatoryCheckbox == null)
        {
            mandatoryCheckbox = new CheckBox();
            mandatoryCheckbox.setId(createId(MANDATORY_CHECKBOX_ID_SUFFIX));
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
        final Button saveButton = new Button(viewContext.getMessage(Dict.BUTTON_SAVE));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(createId(SAVE_BUTTON_ID_SUFFIX));
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

    private String getDefaultValue()
    {
        if (defaultValueField != null)
        {
            return PropertyFieldFactory.valueToString(defaultValueField.getValue());
        }
        return null;
    }

    private String getSelectedEntityCode()
    {
        switch (entityKind)
        {
            case EXPERIMENT:
                return experimentTypeSelectionWidget.tryGetSelectedExperimentType().getCode();
            case SAMPLE:
                return sampleTypeSelectionWidget.tryGetSelectedSampleType().getCode();
            default:
                throw new IllegalArgumentException(UNSUPPORTED_ENTITY_KIND);
        }
    }

    private final void addFormFields()
    {
        formPanel.add(getPropertyTypeWidget());
        formPanel.add(getTypeSelectionWidget());
        formPanel.add(getMandatoryCheckbox());
        updateDefaultField();
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    final static class InfoBoxResetListener implements Listener<FieldEvent>
    {
        private final InfoBox infoBox;

        InfoBoxResetListener(final InfoBox infoBox)
        {
            assert infoBox != null : "Unspecified info box.";
            this.infoBox = infoBox;
        }

        public final void handleEvent(final FieldEvent be)
        {
            infoBox.reset();
        }
    }

    public final class AssignPropertyTypeCallback extends AbstractAsyncCallback<Void>
    {
        AssignPropertyTypeCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullAssignmentInfo(String propertyTypeCode, String eKind,
                String entityTypeCode)
        {
            return "Property type <b>" + propertyTypeCode + "</b> successfully assigned to "
                    + eKind.toLowerCase() + " <b>" + entityTypeCode + "</b>.";
        }

        @Override
        protected final void process(final Void result)
        {
            final String message =
                    createSuccessfullAssignmentInfo(propertyTypeSelectionWidget
                            .tryGetSelectedPropertyTypeCode(), entityKind.name() + " type",
                            getSelectedEntityCode());
            infoBox.displayInfo(message);
            resetForm();
        }
    }

    private void updateDefaultField()
    {
        hideDefaultField();
        final PropertyType propertyType = propertyTypeSelectionWidget.tryGetSelectedPropertyType();
        if (propertyType != null)
        {
            Field<?> field =
                    PropertyFieldFactory.createField(propertyType, false, viewContext
                            .getMessage(Dict.DEFAULT_VALUE), createId(DEFAULT_VALUE_ID_PART
                            + propertyType.getCode()));
            defaultValueField = field;
            defaultValueField.show();
            formPanel.add(defaultValueField);
        }
        layout();
    }

    private void hideDefaultField()
    {
        if (defaultValueField != null)
        {
            defaultValueField.hide();
            formPanel.remove(defaultValueField);
            defaultValueField = null;
        }
    }

    private final void submitForm()
    {
        if (formPanel.isValid())
        {
            viewContext.getService().assignPropertyType(entityKind,
                    propertyTypeSelectionWidget.tryGetSelectedPropertyTypeCode(),
                    getSelectedEntityCode(), getMandatoryCheckbox().getValue(), getDefaultValue(),
                    new AssignPropertyTypeCallback(viewContext));
        }
    }

    private void resetForm()
    {
        formPanel.reset();
        updateDefaultField();
    }

}
