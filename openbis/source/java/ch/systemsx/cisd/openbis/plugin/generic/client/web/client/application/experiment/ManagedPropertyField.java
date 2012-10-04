/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property.ManagedPropertyFormHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;

/**
 * @author Franz-Josef Elmer
 */
public class ManagedPropertyField extends Field<List<Map<String, String>>>
{
    private static final int SPACING = 3;

    private static final class Section
    {
        private final Map<String, TextField<?>> inputFields;

        Section(Map<String, TextField<?>> inputFields)
        {
            this.inputFields = inputFields;
        }
    }

    private final IViewContext<?> viewContext;

    private final List<IManagedInputWidgetDescription> widgetDescriptions;

    private final VerticalPanel verticalPanel;

    private List<Section> sections = new ArrayList<Section>();

    public ManagedPropertyField(IViewContext<?> viewContext, String label, boolean isMandatory,
            List<IManagedInputWidgetDescription> widgetDescriptions)
    {
        this.viewContext = viewContext;
        this.widgetDescriptions = widgetDescriptions;
        verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlign(HorizontalAlignment.RIGHT);

        Button addButton = new Button("Add More");
        addButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    addNewSection();
                }
            });
        addButton.setToolTip("Add a new section.");
        verticalPanel.add(addButton);
        addNewSection();
    }

    public Widget getWidget()
    {
        return verticalPanel;
    }

    @Override
    public List<Map<String, String>> getValue()
    {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Section section : sections)
        {
            HashMap<String, String> row = new HashMap<String, String>();
            for (Entry<String, TextField<?>> entry : section.inputFields.entrySet())
            {
                row.put(entry.getKey(), entry.getValue().getRawValue());
            }
            list.add(row);
        }
        return list;
    }

    private void addNewSection()
    {
        FormPanel formPanel = new FormPanel();
        formPanel.setLabelWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH - SPACING - 2);
        formPanel.setFieldWidth(AbstractRegistrationForm.DEFAULT_FIELD_WIDTH);
        formPanel.setHeaderVisible(false);
        Map<String, TextField<?>> inputFieldsByCode = new HashMap<String, TextField<?>>();
        ManagedPropertyFormHelper formHelper =
                new ManagedPropertyFormHelper(viewContext, formPanel, inputFieldsByCode);
        formHelper.fillForm(widgetDescriptions);
        final HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(SPACING);
        horizontalPanel.add(formPanel);
        Button removeButton = new Button("-");
        removeButton.setToolTip("Delete this section.");
        final Section section = new Section(inputFieldsByCode);
        removeButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    MessageBox.confirm("Delete Section",
                            "Do you really want to delete this section?",
                            new Listener<MessageBoxEvent>()
                                {
                                    @Override
                                    public void handleEvent(MessageBoxEvent be)
                                    {
                                        Button buttonClicked = be.getButtonClicked();
                                        if (Dialog.YES.equals(buttonClicked.getItemId()))
                                        {
                                            sections.remove(section);
                                            verticalPanel.remove(horizontalPanel);
                                            verticalPanel.layout();
                                        }
                                    }
                                });
                }
            });
        horizontalPanel.add(removeButton);
        sections.add(section);
        verticalPanel.insert(horizontalPanel, verticalPanel.getItemCount() - 1);
        verticalPanel.layout();
    }
}
