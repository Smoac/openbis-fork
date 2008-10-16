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
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
class SampleBrowserToolbar extends ToolBar
{

    final SampleBrowserGrid grid;

    final SampleTypeSelectionWidget selectSampleTypeCombo;

    final GroupSelectionWidget selectGroupCombo;

    final ToolbarController controller;

    private CheckBox includeInstanceCheckbox;

    private CheckBox includeGroupCheckbox;

    private Button submitButton;

    private ColumnChooser columnChooser;

    public SampleBrowserToolbar(GenericViewContext viewContext, SampleBrowserGrid rightPanel,
            CommonColumns commonColumns, ParentColumns parentColumns,
            PropertyColumns propertyColumns)
    {
        this.grid = rightPanel;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext);
        selectGroupCombo = new GroupSelectionWidget(viewContext);
        includeInstanceCheckbox = new CheckBox();
        includeGroupCheckbox = new CheckBox();
        includeGroupCheckbox.setValue(true);
        columnChooser = new ColumnChooser(commonColumns, parentColumns, propertyColumns);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        controller =
                new ToolbarController(selectSampleTypeCombo, selectGroupCombo,
                        includeInstanceCheckbox, includeGroupCheckbox, submitButton, columnChooser,
                        parentColumns, propertyColumns);
        addSelectSampleTypeListeners();
        addSelectGroupListeners();
        addIncludeInstanceListeners();
        addIncludeGroupListeners();
        refresh();
    }

    private void addIncludeGroupListeners()
    {
        includeGroupCheckbox.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    controller.resetPropertyCache(includeGroupCheckbox.getValue().booleanValue());
                    controller.refreshSubmitButton();
                    controller.showOrHideGroupList();
                }
            });
    }

    private void addIncludeInstanceListeners()
    {
        includeInstanceCheckbox.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    controller
                            .resetPropertyCache(includeInstanceCheckbox.getValue().booleanValue());
                    controller.refreshSubmitButton();
                }
            });
    }

    private void addSelectGroupListeners()
    {
        selectGroupCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    controller.resetPropertyCache(true);
                    controller.refreshSubmitButton();
                }
            });
    }

    private void addSelectSampleTypeListeners()
    {
        selectSampleTypeCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    controller.rebuildColumnChooser();
                    controller.refreshSubmitButton();
                }
            });
    }

    private void display()
    {
        setBorders(true);
        removeAll();
        add(new LabelToolItem("Sample type:"));
        add(new AdapterToolItem(selectSampleTypeCombo));

        add(new SeparatorToolItem());

        add(new LabelToolItem("Shared:"));
        add(new AdapterToolItem(includeInstanceCheckbox));

        add(new SeparatorToolItem());

        add(new LabelToolItem("Group:"));
        add(new AdapterToolItem(includeGroupCheckbox));
        add(new AdapterToolItem(selectGroupCombo));

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

                    final SampleType selectedType = selectSampleTypeCombo.tryGetSelected();
                    final String selectedGroupCode =
                            selectGroupCombo.tryGetSelected() == null ? null : selectGroupCombo
                                    .tryGetSelected().getCode();

                    final Boolean includeGroup = includeGroupCheckbox.getValue();
                    final Boolean includeInstance = includeInstanceCheckbox.getValue();

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
        refreshButton.setIconStyle("x-tbar-loading");
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
            sb.append("Neither group nor shared samples checkbox was selected.");
            error = true;
        }
        return error;
    }

    public void refresh()
    {
        display();
        selectSampleTypeCombo.refresh();
        selectGroupCombo.refresh();
    }

}