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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;

/**
 * The property type assignment panel.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeAssignmentForm extends LayoutContainer
{
    private static final String PREFIX = "property-type-assignment_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private static final String PROPERTY_TYPE_ID_SUFFIX = ID_PREFIX + "property_type";

    private static final String SAMPLE_TYPE_ID_SUFFIX = ID_PREFIX + "sample_type";

    private static final String EXPERIMENT_TYPE_ID_SUFFIX = ID_PREFIX + "experiment_type";

    private static final String MANDATORY_CHECKBOX_ID_PREFIX = ID_PREFIX + "mandatory_checkbox";

    public static final String SAVE_BUTTON_ID_PREFIX = ID_PREFIX + "save-button";

    protected static final String DEFAULT_VALUE_ID_PREFIX = ID_PREFIX + "default_value";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private ExperimentTypeSelectionWidget experimentTypeSelectionWidget;

    private PropertyTypeSelectionWidget propertyTypeSelectionWidget;

    private CheckBox mandatoryCheckbox;

    private InfoBox infoBox;

    private FormPanel formPanel;

    static final int LABEL_WIDTH = 130;

    static final int FIELD_WIDTH = 400;

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
        add(formPanel = creatFormPanel());
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
                    new PropertyTypeSelectionWidget(viewContext, PROPERTY_TYPE_ID_SUFFIX
                            + entityKind.name());
            propertyTypeSelectionWidget
                    .addListener(Events.Focus, new InfoBoxResetListener(infoBox));
            propertyTypeSelectionWidget.setAllowBlank(false);
            propertyTypeSelectionWidget
                    .setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
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
                throw new IllegalArgumentException("Unsupported entity kind");
        }

    }

    private final void createFormFields()
    {
        mandatoryCheckbox = new CheckBox();
        mandatoryCheckbox.setId(MANDATORY_CHECKBOX_ID_PREFIX + entityKind.name());
        mandatoryCheckbox.setFieldLabel("Mandatory");
        mandatoryCheckbox.setValue(false);
        mandatoryCheckbox.addListener(Events.Change, new InfoBoxResetListener(infoBox));
        getTypeSelectionWidget();
        getPropertyTypeWidget();
    }

    private final FormPanel creatFormPanel()
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
        saveButton.setId(SAVE_BUTTON_ID_PREFIX + entityKind.name());
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
                    formPanel.reset();
                }
            });
        panel.addButton(resetButton);
        panel.addButton(saveButton);
        return panel;
    }

    private final void submitForm()
    {
        if (formPanel.isValid())
        {
            viewContext.getService().assignPropertyType(entityKind,
                    propertyTypeSelectionWidget.tryGetSelectedPropertyTypeCode(),
                    getSelectedEntityCode(), mandatoryCheckbox.getValue(), getDefaultValue(),
                    new AssignPropertyTypeCallback(viewContext));
        }
    }

    private String getDefaultValue()
    {
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
                throw new IllegalArgumentException("Unsupported entity kind");
        }
    }

    private final void addFormFields()
    {
        formPanel.add(getPropertyTypeWidget());
        formPanel.add(getTypeSelectionWidget());
        formPanel.add(mandatoryCheckbox);
    }

    //
    // FormPanel
    //

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        createFormFields();
        addFormFields();
    }

    //
    // Helper classes
    //

    final static class InfoBoxResetListener implements Listener<FieldEvent>
    {
        private final InfoBox infoBox;

        InfoBoxResetListener(final InfoBox infoBox)
        {
            assert infoBox != null : "Unspecified info box.";
            this.infoBox = infoBox;
        }

        //
        // Listener
        //

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

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {
            final String message =
                    createSuccessfullAssignmentInfo(propertyTypeSelectionWidget
                            .tryGetSelectedPropertyTypeCode(), entityKind.name() + " type",
                            getSelectedEntityCode());
            infoBox.displayInfo(message);
            formPanel.reset();
        }
    }

}
