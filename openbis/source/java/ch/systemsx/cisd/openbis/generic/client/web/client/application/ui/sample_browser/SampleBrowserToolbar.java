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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The sample browser toolbar.
 * 
 * @author Izabela  Adamczyk
 * @author Christian Ribeaud
 */
class SampleBrowserToolbar extends ToolBar
{

    final SampleBrowserGrid grid;

    final SampleTypeSelectionWidget sampleTypeSelectionWidget;

    final GroupSelectionWidget groupSelectionWidget;

    final ToolbarController controller;

    private CheckBox instanceCheckbox;

    private CheckBox groupCheckbox;

    private Button submitButton;

    private ColumnChooser columnChooser;

    public SampleBrowserToolbar(GenericViewContext viewContext, SampleBrowserGrid rightPanel)
    {
        this.grid = rightPanel;
        sampleTypeSelectionWidget = new SampleTypeSelectionWidget(viewContext);
        groupSelectionWidget = new GroupSelectionWidget(viewContext);
        instanceCheckbox = new CheckBox();
        groupCheckbox = new CheckBox();
        groupCheckbox.setValue(true);
        columnChooser = new ColumnChooser();
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);

        controller =
                new ToolbarController(sampleTypeSelectionWidget, groupSelectionWidget,
                        instanceCheckbox, groupCheckbox, submitButton, columnChooser);

        final SelectionChangedListener<ModelData> listener =
                new SelectionChangedListener<ModelData>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<ModelData> se)
                        {
                            controller.refreshSubmitButton();

                        }
                    };
        sampleTypeSelectionWidget.addSelectionChangedListener(listener);
        groupSelectionWidget.addSelectionChangedListener(listener);
        final Listener<BaseEvent> clickListener = new Listener<BaseEvent>()
            {

                public void handleEvent(BaseEvent be)
                {
                    controller.refreshSubmitButton();
                }
            };
        instanceCheckbox.addListener(Event.ONCLICK, clickListener);
        groupCheckbox.addListener(Event.ONCLICK, clickListener);
        groupCheckbox.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {

                public void handleEvent(BaseEvent be)
                {
                    controller.showOrHideGroupList();
                }
            });

        sampleTypeSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<ModelData>()
                    {
                        @Override
                        public void selectionChanged(SelectionChangedEvent<ModelData> se)
                        {
                            controller.refreshColumnChooser();

                        }
                    });

        refresh();
    }

    private void display()
    {
        setBorders(true);
        removeAll();
        add(new LabelToolItem("Sample type:"));
        add(new AdapterToolItem(sampleTypeSelectionWidget));

        add(new SeparatorToolItem());

        add(new LabelToolItem("Instance:"));
        add(new AdapterToolItem(instanceCheckbox));

        add(new SeparatorToolItem());

        add(new LabelToolItem("Group:"));
        add(new AdapterToolItem(groupCheckbox));
        add(new AdapterToolItem(groupSelectionWidget));

        add(new SeparatorToolItem());

        add(columnChooser);

        add(new SeparatorToolItem());

        add(new AdapterToolItem(submitButton));
        layout();
    }

    private Button createSubmitButton()
    {
        final Button refreshButton = new Button("Refresh", new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {

                    final SampleType selectedType = sampleTypeSelectionWidget.tryGetSelected();
                    final String selectedGroupCode =
                            groupSelectionWidget.tryGetSelected() == null ? null
                                    : groupSelectionWidget.tryGetSelected().getCode();

                    final Boolean includeGroup = groupCheckbox.getValue();
                    final Boolean includeInstance = instanceCheckbox.getValue();

                    StringBuilder errorReport = new StringBuilder();
                    boolean error =
                            checkForMissingInformation(selectedType, selectedGroupCode,
                                    includeGroup, includeInstance, errorReport);
                    if (error)
                    {
                        MessageBox.alert("Missing information", errorReport.toString(), null);
                    } else
                    {
                        grid
                                .refresh(selectedType, selectedGroupCode, includeGroup,
                                        includeInstance);
                    }
                }
            });
        return refreshButton;
    }

    private boolean checkForMissingInformation(final SampleType selectedType,
            final String selectedGroupCode, final Boolean includeGroup,
            final Boolean includeInstance, StringBuilder sb)
    {
        boolean error = false;
        if (includeGroup && selectedGroupCode == null)
        {
            sb.append("Group code ");
            error = true;
        }
        if (selectedType == null)
        {
            if (error)
            {
                sb.append("and sample type ");
            } else
            {
                sb.append("Sample type ");
                error = true;
            }
        }
        if (error)
        {
            sb.append("not selected. ");
        }
        if (includeGroup == false && includeInstance == false)
        {
            sb.append("Neither GROUP nor INSTANCE checkbox selected.");
            error = true;
        }
        return error;
    }

    public void refresh()
    {
        display();
        sampleTypeSelectionWidget.refresh();
        groupSelectionWidget.refresh();
    }

}