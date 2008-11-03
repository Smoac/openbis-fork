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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * @author Izabela Adamczyk
 */
public class SampleRegistrationMock extends ContentPanel
{

    private final SampleTypeSelectionWidget sampleTypeSelection;

    public SampleRegistrationMock(final GenericViewContext viewContext)
    {
        setHeading("Sample registration mock");
        setBodyBorder(false);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, true);
        sampleTypeSelection.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    final SampleType selectedType = sampleTypeSelection.tryGetSelected();
                    if (selectedType != null)
                    {
                        removeAll();
                        add(new SampleRegistrationForm(viewContext, selectedType));
                        layout();
                    }
                }
            });
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Sample type:"));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        setTopComponent(toolBar);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        createUI();
    }

    void createUI()
    {

    }

    static class SampleRegistrationForm extends FormPanel
    {

        class SamplePicker
        {

        }

        /**
         * Provides a sample code input field with a {@link SamplePicker}.
         */
        class SampleField extends TriggerField<String>
        {
            public SampleField()
            {
                setTriggerStyle("x-form-search-trigger");
                addListener(Event.ONCLICK, new Listener<BaseEvent>()
                    {

                        public void handleEvent(final BaseEvent be)
                        {
                            MessageBox
                                    .alert(
                                            "Choose sample",
                                            "List of samples will appear if the magnifying glass clicked...",
                                            null);
                        }
                    });
            }
        }

        private final TextField<String> type;

        private final GroupSelectionWidget groupSelectionWidget;

        private final SampleType sampleType;

        SampleRegistrationForm(final GenericViewContext viewContext, final SampleType sampleType)
        {
            this.sampleType = sampleType;
            addDummyProperties();
            groupSelectionWidget = new GroupSelectionWidget(viewContext);
            type = new TextField<String>();
            type.setValue(sampleType.getCode());
            type.setFieldLabel("Sample type");
            type.disable();
            setHeaderVisible(false);
            setBodyBorder(false);
            setLabelWidth(150);
            setButtonAlign(HorizontalAlignment.LEFT);
            final ButtonBar bb = new ButtonBar();
            bb.setCellSpacing(20);
            bb.add(new Button("Save and use as template"));
            bb.add(new Button("Save"));
            setButtonBar(bb);

        }

        @Override
        protected void onRender(final Element target, final int index)
        {
            super.onRender(target, index);
            refresh();
        }

        public void refresh()
        {

            add(type);

            final TextField<String> code = new TextField<String>();
            code.setFieldLabel("Sample code");
            add(code);

            add(groupSelectionWidget);

            final SampleField parentGenerator = new SampleField();
            parentGenerator.setFieldLabel("Generated from sample");
            add(parentGenerator);

            final SampleField parentContainer = new SampleField();
            parentContainer.setFieldLabel("Part of sample");
            add(parentContainer);

            for (final SampleTypePropertyType stpt : sampleType.getSampleTypePropertyTypes())
            {

                add(PropertyFieldFactory.createProperty(stpt));
            }

        }

        static class PropertyFieldFactory
        {
            public static Field<?> createProperty(final SampleTypePropertyType stpt)
            {
                final Field<?> field;
                // 
                // This should depend on data type in property type :
                //
                final String code = stpt.getPropertyType().getSimpleCode();
                if (code.equals("DESCRIPTION")) // text area/text field (number validation?)
                {
                    field = new TextArea();
                } else if (code.equals("VALID_UNTIL")) // date
                {
                    field = new DateField();
                } else if (code.equals("SOFT_MATERIAL")) // boolean
                {
                    field = new CheckBox();
                } else if (code.equals("COLOR")) // controlled vocabulary
                {
                    final ListBox list = new ListBox();
                    list.addItem("White");
                    list.addItem("Red");
                    list.addItem("Green");
                    list.addItem("Blue");
                    list.addItem("Black");
                    field = new AdapterField(list);
                } else
                {
                    field = new TextField<String>();
                }
                field.setFieldLabel(stpt.getPropertyType().getLabel());
                return field;
            }
        }

        private void addDummyProperties()
        {
            final ArrayList<SampleTypePropertyType> list = new ArrayList<SampleTypePropertyType>();
            final SampleTypePropertyType sampleTypePropertyType = new SampleTypePropertyType();
            sampleTypePropertyType.setMandatory(true);
            final PropertyType propertyType = new PropertyType();
            propertyType.setLabel("Description");
            propertyType.setInternalNamespace(false);
            propertyType.setSimpleCode("DESCRIPTION");
            sampleTypePropertyType.setPropertyType(propertyType);
            list.add(sampleTypePropertyType);

            final SampleTypePropertyType sampleTypePropertyType2 = new SampleTypePropertyType();
            sampleTypePropertyType2.setMandatory(false);
            final PropertyType propertyType2 = new PropertyType();
            propertyType2.setLabel("Valid until");
            propertyType2.setInternalNamespace(false);
            propertyType2.setSimpleCode("VALID_UNTIL");
            sampleTypePropertyType2.setPropertyType(propertyType2);
            list.add(sampleTypePropertyType2);

            final SampleTypePropertyType sampleTypePropertyType3 = new SampleTypePropertyType();
            sampleTypePropertyType3.setMandatory(false);
            final PropertyType propertyType3 = new PropertyType();
            propertyType3.setLabel("Soft material");
            propertyType3.setInternalNamespace(false);
            propertyType3.setSimpleCode("SOFT_MATERIAL");
            sampleTypePropertyType3.setPropertyType(propertyType3);
            list.add(sampleTypePropertyType3);

            final SampleTypePropertyType sampleTypePropertyType4 = new SampleTypePropertyType();
            sampleTypePropertyType4.setMandatory(false);
            final PropertyType propertyType4 = new PropertyType();
            propertyType4.setLabel("Color");
            propertyType4.setInternalNamespace(false);
            propertyType4.setSimpleCode("COLOR");
            sampleTypePropertyType4.setPropertyType(propertyType4);
            list.add(sampleTypePropertyType4);

            sampleType.setSampleTypePropertyTypes(list);
        }

    }
}
