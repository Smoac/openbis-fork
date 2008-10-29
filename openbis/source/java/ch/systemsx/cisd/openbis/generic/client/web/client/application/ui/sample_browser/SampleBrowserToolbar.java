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
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.OpenbisEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CommonColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The toolbar of sample browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
class SampleBrowserToolbar extends ToolBar
{

    private static final String PREFIX = "sample-browser-toolbar_";

    static final String REFRESH_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "refresh-button";

    public static final String INCLUDE_GROUP_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-group-checkbox";

    public static final String INCLUDE_SHARED_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-shared-checkbox";

    private final SampleBrowserGrid grid;

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final GroupSelectionWidget selectGroupCombo;

    private final ToolbarController controller;

    private final CheckBox includeInstanceCheckbox;

    private final CheckBox includeGroupCheckbox;

    private final Button submitButton;

    private final Button exportButton;

    private final ColumnChooser columnChooser;

    public SampleBrowserToolbar(final GenericViewContext viewContext,
            final SampleBrowserGrid rightPanel, final CommonColumns commonColumns,
            final ParentColumns parentColumns, final PropertyColumns propertyColumns)
    {
        this.grid = rightPanel;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext);
        selectGroupCombo = new GroupSelectionWidget(viewContext);
        includeInstanceCheckbox = new CheckBox();
        includeInstanceCheckbox.setId(INCLUDE_SHARED_CHECKBOX_ID);
        includeInstanceCheckbox.setValue(true);
        includeGroupCheckbox = new CheckBox();
        includeGroupCheckbox.setId(INCLUDE_GROUP_CHECKBOX_ID);
        includeGroupCheckbox.setStyleAttribute("margin", "4px");
        includeGroupCheckbox.setValue(true);
        columnChooser = new ColumnChooser(commonColumns, parentColumns, propertyColumns);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        exportButton = createExportButton();
        exportButton.setEnabled(false);
        controller =
                new ToolbarController(selectSampleTypeCombo, selectGroupCombo,
                        includeInstanceCheckbox, includeGroupCheckbox, submitButton, exportButton,
                        columnChooser, parentColumns, propertyColumns);
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
                public void handleEvent(final BaseEvent be)
                {
                    controller.refreshButtons();
                    controller.showOrHideGroupList();
                }
            });
    }

    private void addIncludeInstanceListeners()
    {
        includeInstanceCheckbox.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(final BaseEvent be)
                {
                    controller.refreshButtons();
                    controller.includeInstanceHasChanged();
                }
            });
    }

    private void addSelectGroupListeners()
    {
        selectGroupCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    controller.resetPropertyCache(true);
                    controller.refreshButtons();
                }
            });

        selectGroupCombo.addListener(OpenbisEvents.CALLBACK_FINNISHED, new Listener<BaseEvent>()
            {

                public void handleEvent(final BaseEvent be)
                {
                    controller.refreshGroupCheckbox();
                }
            });
    }

    private void addSelectSampleTypeListeners()
    {
        selectSampleTypeCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    controller.rebuildColumnChooser();
                    controller.refreshButtons();
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

        add(new FillToolItem());

        add(new AdapterToolItem(submitButton));

        add(new SeparatorToolItem());

        add(new AdapterToolItem(exportButton));
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
        refreshButton.setId(REFRESH_BUTTON_ID);
        // refreshButton.setStyleAttribute("border-color", "#ff0000");
        refreshButton.setBorders(true);
        return refreshButton;
    }

    private Button createExportButton()
    {
        final Button button = new Button("Export data", new SelectionListener<ComponentEvent>()
            {
                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    MessageBox.alert("Warning", "Not yet implemented!", null);
                }
            });
        button.setBorders(true);
        return button;
    }

    private boolean checkForMissingInformation(final SampleType selectedType,
            final String selectedGroupCode, final Boolean includeGroup,
            final Boolean includeInstance, final StringBuilder sb)
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