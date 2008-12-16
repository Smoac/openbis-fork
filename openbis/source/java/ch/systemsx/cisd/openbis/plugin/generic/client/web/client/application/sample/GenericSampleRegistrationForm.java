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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ControlledVocabullaryField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DateFormField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.RealField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample registration panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "generic-sample-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String CODE_FIELD_ID = ID_PREFIX + "code";

    public static final String SHARED_CHECKBOX_ID = ID_PREFIX + "shared";

    public static final String CONTAINER_FIELD_ID = ID_PREFIX + "container";

    public static final String PARENT_FIELD_ID = ID_PREFIX + "parent";

    private static final String ETPT = "PROPERTY_TYPE";

    private static boolean SELECT_GROUP_BY_DEFAULT = true;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private GroupSelectionWidget groupSelectionWidget;

    private ArrayList<Field<?>> propertyFields;

    private TextField<String> container;

    private TextField<String> parent;

    private TextField<String> codeField;

    private CheckBox sharedCheckbox;

    private MultiField<Field<?>> groupMultiField;

    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext, ID_PREFIX);
        this.viewContext = viewContext;
        this.sampleType = sampleType;
    }

    private final void createFormFields()
    {
        codeField = new CodeField(viewContext.getMessage(Dict.CODE));
        codeField.setId(CODE_FIELD_ID);
        codeField.addListener(Events.Focus, new AbstractRegistrationForm.InfoBoxResetListener(
                infoBox));

        groupSelectionWidget = new GroupSelectionWidget(viewContext.getCommonViewContext());
        groupSelectionWidget.setEnabled(SELECT_GROUP_BY_DEFAULT);

        sharedCheckbox = new CheckBox();
        sharedCheckbox.setId(SHARED_CHECKBOX_ID);
        sharedCheckbox.setBoxLabel("Shared");
        sharedCheckbox.setValue(SELECT_GROUP_BY_DEFAULT == false);
        sharedCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                //
                // Listener
                //

                public final void handleEvent(final FieldEvent be)
                {
                    groupSelectionWidget
                            .setEnabled(sharedCheckbox.getValue().booleanValue() == false);
                }
            });

        groupMultiField = new MultiField<Field<?>>();
        groupMultiField.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        groupMultiField.add(sharedCheckbox);
        groupMultiField.add(groupSelectionWidget);
        groupMultiField.setLabelSeparator(VarcharField.MANDATORY_LABEL_SEPARATOR);
        groupMultiField.setValidator(new Validator<Field<?>, MultiField<Field<?>>>()
            {
                //
                // Validator
                //

                public final String validate(final MultiField<Field<?>> field, final String value)
                {
                    if (sharedCheckbox.getValue() == false
                            && groupSelectionWidget.tryGetSelectedGroup() == null)
                    {
                        return "Group must be chosen or shared selected";
                    }
                    return null;
                }
            });

        parent = new VarcharField(viewContext.getMessage(Dict.GENERATED_FROM_SAMPLE), false);
        parent.setId(PARENT_FIELD_ID);

        container = new VarcharField(viewContext.getMessage(Dict.PART_OF_SAMPLE), false);
        container.setId(CONTAINER_FIELD_ID);

        propertyFields = new ArrayList<Field<?>>();
        for (final SampleTypePropertyType stpt : sampleType.getSampleTypePropertyTypes())
        {
            propertyFields.add(createProperty(stpt));

        }
    }

    private final String createSampleIdentifier()
    {
        final boolean shared = sharedCheckbox.getValue();
        final Group group = groupSelectionWidget.tryGetSelectedGroup();
        final String code = codeField.getValue();
        final StringBuilder builder = new StringBuilder("/");
        if (shared == false)
        {
            builder.append(group.getCode() + "/");
        }
        builder.append(code);
        return builder.toString().toUpperCase();
    }

    private final String valueToString(final Object value)
    {
        if (value == null)
        {
            return null;
        } else if (value instanceof Date)
        {
            return DateRenderer.renderDate((Date) value);
        } else
        {
            return value.toString();
        }
    }

    private final void addFormFields()
    {
        formPanel.add(codeField);
        formPanel.add(groupMultiField);
        formPanel.add(parent);
        formPanel.add(container);
        for (final Field<?> propertyField : propertyFields)
        {
            formPanel.add(propertyField);
        }
    }

    private final Field<?> createProperty(final SampleTypePropertyType stpt)
    {
        final Field<?> field;
        final DataTypeCode dataType = stpt.getPropertyType().getDataType().getCode();
        final boolean isMandatory = stpt.isMandatory();
        final String label = stpt.getPropertyType().getLabel();
        switch (dataType)
        {
            case BOOLEAN:
                field = new CheckBoxField(label, isMandatory);
                break;
            case VARCHAR:
                field = new VarcharField(label, isMandatory);
                break;
            case TIMESTAMP:
                field = new DateFormField(label, isMandatory);
                break;
            case CONTROLLEDVOCABULARY:
                field =
                        new ControlledVocabullaryField(label, isMandatory, stpt.getPropertyType()
                                .getVocabulary().getTerms());
                break;
            case INTEGER:
                field = new IntegerField(label, isMandatory);
                break;
            case REAL:
                field = new RealField(label, isMandatory);
                break;
            default:
                field = new VarcharField(label, isMandatory);
                break;
        }
        field.setData(ETPT, stpt);
        final String propertyTypeCode = stpt.getPropertyType().getCode();
        field.setTitle(propertyTypeCode);
        field.setId(createFormFieldId(propertyTypeCode));
        return field;
    }

    private final static String createFormFieldId(final String propertyTypeCode)
    {
        return ID_PREFIX + propertyTypeCode.toLowerCase().replace(".", "-").replace("_", "-");
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    public final void submitValidForm()
    {
        final NewSample newSample =
                new NewSample(createSampleIdentifier(), sampleType, StringUtils.trimToNull(parent
                        .getValue()), StringUtils.trimToNull(container.getValue()));
        final List<SampleProperty> properties = new ArrayList<SampleProperty>();
        for (final Field<?> field : propertyFields)
        {
            if (field.getValue() != null)
            {
                final SampleTypePropertyType stpt = field.getData(ETPT);
                final SampleProperty sampleProperty = new SampleProperty();
                sampleProperty.setValue(valueToString(field.getValue()));
                sampleProperty.setEntityTypePropertyType(stpt);
                properties.add(sampleProperty);
            }
        }
        newSample.setProperties(properties.toArray(SampleProperty.EMPTY_ARRAY));
        viewContext.getService().registerSample(newSample, new RegisterSampleCallback(viewContext));
    }

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

    public final class RegisterSampleCallback extends AbstractAsyncCallback<Void>
    {

        RegisterSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo(final boolean shared,
                final String code, final Group group)
        {
            if (shared)
            {
                return "Shared sample <b>" + code + "</b> successfully registered";

            } else
            {
                return "Sample <b>" + code + "</b> successfully registered in group <b>"
                        + group.getCode() + "</b>";
            }
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {
            final String message =
                    createSuccessfullRegistrationInfo(sharedCheckbox.getValue(), codeField
                            .getValue(), groupSelectionWidget.tryGetSelectedGroup());
            infoBox.displayInfo(message);
            formPanel.reset();
        }
    }

}
