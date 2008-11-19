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
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * The <i>generic</i> sample registration panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends FormPanel
{
    private static final String ETPT = "PROPERTY_TYPE";

    private static boolean SELECT_GROUP_BY_DEFAULT = true;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private GroupSelectionWidget groupSelectionWidget;

    private ArrayList<Field<?>> propertyFields;

    private TextField<String> parentContainer;

    private TextField<String> parentGenerator;

    private TextField<String> code;

    private CheckBox sharedCheckbox;

    private MultiField<Field<?>> groupMultiField;

    private static final String MANDATORY_LABEL_SEPARATOR = ": *";

    private static final String CODE_REGEX = "[a-zA-Z0-9_]+";

    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, SampleType sampleType)
    {
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        configureForm();
        createFormFields();
    }

    private void createFormFields()
    {
        code = new CodeField("Sample code", true);

        groupSelectionWidget = new GroupSelectionWidget(viewContext);
        groupSelectionWidget.setEnabled(SELECT_GROUP_BY_DEFAULT);

        sharedCheckbox = new CheckBox();
        sharedCheckbox.setBoxLabel("Shared");
        sharedCheckbox.setValue(SELECT_GROUP_BY_DEFAULT == false);
        sharedCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    groupSelectionWidget
                            .setEnabled(sharedCheckbox.getValue().booleanValue() == false);
                }
            });

        groupMultiField = new MultiField<Field<?>>();
        groupMultiField.setFieldLabel("Group");
        groupMultiField.add(sharedCheckbox);
        groupMultiField.add(groupSelectionWidget);
        groupMultiField.setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
        groupMultiField.setValidator(new Validator<Field<?>, MultiField<Field<?>>>()
            {
                public String validate(MultiField<Field<?>> field, String value)
                {
                    if (sharedCheckbox.getValue() == false
                            && groupSelectionWidget.tryGetSelected() == null)
                    {
                        return "Group must be chosen or shared selected";
                    }
                    return null;
                }
            });

        parentGenerator = new VarcharField("Generated from sample", false);

        parentContainer = new VarcharField("Part of sample", false);

        propertyFields = new ArrayList<Field<?>>();
        for (final SampleTypePropertyType stpt : sampleType.getSampleTypePropertyTypes())
        {
            propertyFields.add(createProperty(stpt));
        }
    }

    private void configureForm()
    {
        setHScrollPosition(20);
        setHeaderVisible(false);
        setBodyBorder(false);
        setLabelWidth(150);
        setFieldWidth(350);
        setButtonAlign(HorizontalAlignment.LEFT);
        final ButtonBar bb = new ButtonBar();
        bb.setCellSpacing(20);
        Button resetButton = new Button("Reset");
        resetButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    resetForm();
                }
            });
        bb.add(resetButton);

        Button saveButton = new Button("Save");
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    submitForm();
                }
            });
        bb.add(saveButton);
        setButtonBar(bb);
    }

    private String createSampleIdentifier()
    {
        boolean shared = sharedCheckbox.getValue();
        Group g = groupSelectionWidget.tryGetSelected();
        String c = code.getValue();
        StringBuilder sb = new StringBuilder("/");
        if (shared == false)
        {
            if (g == null)
            {
                throw new UserFailureException("Group not chosen");
            }
            sb.append(g.getCode() + "/");
        }
        sb.append(c);
        return sb.toString().toUpperCase();
    }

    private void submitForm()
    {
        if (isValid())
        {
            SampleToRegister sampleToRegister =
                    new SampleToRegister(createSampleIdentifier(), sampleType.getCode(),
                            parentGenerator.getValue(), parentContainer.getValue());
            for (Field<?> field : propertyFields)
            {
                if (field.getValue() != null)
                {
                    SampleTypePropertyType stpt = field.getData(ETPT);
                    SampleProperty sp = new SampleProperty();
                    sp.setValue(field.getValue().toString());
                    sp.setEntityTypePropertyType(stpt);
                    sampleToRegister.addProperty(sp);
                }
            }
            viewContext.getService().registerSample(sampleToRegister,
                    new AbstractAsyncCallback<Void>(viewContext)
                        {

                            @Override
                            protected void process(Void result)
                            {
                                MessageBox mb = new MessageBox();
                                mb.setIcon(MessageBox.INFO);
                                mb.setButtons(MessageBox.OK);
                                mb.setModal(true);
                                mb.setTitle("Registration successfull");
                                mb.setMessage("Sample '" + createSampleIdentifier()
                                        + "' successfully registered.");
                                mb.show();
                                resetForm();
                            }
                        });
        }
    }

    private void resetForm()
    {
        createFormFields();
        addFormFields();
        layout();
    }

    @Override
    protected void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    public void addFormFields()
    {
        removeAll();
        add(code);
        add(groupMultiField);
        add(parentGenerator);
        add(parentContainer);
        for (final Field<?> propertyField : propertyFields)
        {
            add(propertyField);
        }
    }

    private Field<?> createProperty(final SampleTypePropertyType stpt)
    {
        final Field<?> field;
        final DataTypes dataType = stpt.getPropertyType().getDataType().getCode();
        boolean isMandatory = stpt.isMandatory();
        String label = stpt.getPropertyType().getLabel();
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
        field.setTitle(stpt.getPropertyType().getCode());
        return field;
    }

    class CheckBoxField extends CheckBox
    {
        public CheckBoxField(String label, boolean mandatory)
        {
            setFieldLabel(label);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
            }
        }
    }

    class DateFormField extends DateField
    {
        DateFormField(String label, boolean mandatory)
        {
            setFieldLabel(label);
            setValidateOnBlur(true);
            setAutoValidate(true);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
                setAllowBlank(false);
            }
        }
    }

    static class ControlledVocabullaryField extends AdapterField
    {

        public ControlledVocabullaryField(String label, boolean mandatory,
                List<VocabularyTerm> terms)
        {
            super(createListBox(terms));
            setFieldLabel(label);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
            }
        }

        private static final ListBox createListBox(List<VocabularyTerm> terms)
        {
            ListBox box = new ListBox();
            for (VocabularyTerm term : terms)
            {
                box.addItem(term.getCode());
            }
            return box;
        }

    }

    static class BasicTextField<T> extends TextField<T>
    {

        BasicTextField(String label, boolean mandatory)
        {
            setFieldLabel(label);
            setMaxLength(1024);
            setValidateOnBlur(true);
            setAutoValidate(true);
            if (mandatory)
            {
                setLabelSeparator(MANDATORY_LABEL_SEPARATOR);
                setAllowBlank(false);
            }
        }
    }

    static class VarcharField extends BasicTextField<String>
    {
        VarcharField(String label, boolean mandatory)
        {
            super(label, mandatory);
        }
    }

    static class CodeField extends VarcharField
    {

        CodeField(String label, boolean mandatory)
        {
            super(label, mandatory);
            setRegex(CODE_REGEX);
        }
    }

    static class RealField extends BasicTextField<Double>
    {
        RealField(String label, boolean mandatory)
        {
            super(label, mandatory);
            setValidator(new Validator<Double, Field<Double>>()
                {

                    public String validate(Field<Double> field, String val)
                    {
                        try
                        {
                            Double.parseDouble(val);
                            return null;
                        } catch (NumberFormatException e)
                        {
                            return "Real number required";
                        }
                    }
                });
        }
    }

    static class IntegerField extends BasicTextField<Integer>
    {
        IntegerField(String label, boolean mandatory)
        {
            super(label, mandatory);
            setValidator(new Validator<Integer, Field<Integer>>()
                {

                    public String validate(Field<Integer> field, String val)
                    {
                        try
                        {
                            Integer.parseInt(val);
                            return null;
                        } catch (NumberFormatException e)
                        {
                            return "Integer required";
                        }
                    }
                });
        }
    }

}
