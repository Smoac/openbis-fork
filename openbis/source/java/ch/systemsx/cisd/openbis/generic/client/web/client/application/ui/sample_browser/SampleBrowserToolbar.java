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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
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
    public static final String ID = "sample-browser-toolbar";

    private static final String PREFIX = ID + "_";

    static final String REFRESH_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "refresh-button";

    public static final String INCLUDE_GROUP_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-group-checkbox";

    public static final String INCLUDE_SHARED_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-shared-checkbox";

    private final SampleBrowserGrid sampleBrowserGrid;

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final GroupSelectionWidget selectGroupCombo;

    private final ToolbarController controller;

    private final CheckBox includeInstanceCheckbox;

    private final CheckBox includeGroupCheckbox;

    private final Button submitButton;

    private final Button exportButton;

    public SampleBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            final SampleBrowserGrid sampleBrowserGrid, final CommonColumns commonColumns,
            final ParentColumns parentColumns, final PropertyColumns propertyColumns)
    {
        this.sampleBrowserGrid = sampleBrowserGrid;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext, ID);
        selectGroupCombo = new GroupSelectionWidget(viewContext);
        includeInstanceCheckbox = new CheckBox();
        includeInstanceCheckbox.setId(INCLUDE_SHARED_CHECKBOX_ID);
        includeInstanceCheckbox.setValue(true);
        includeGroupCheckbox = new CheckBox();
        includeGroupCheckbox.setId(INCLUDE_GROUP_CHECKBOX_ID);
        includeGroupCheckbox.setStyleAttribute("margin", "4px");
        includeGroupCheckbox.setValue(true);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        exportButton = createExportButton();
        exportButton.setEnabled(false);
        controller =
                new ToolbarController(sampleBrowserGrid, selectSampleTypeCombo, selectGroupCombo,
                        includeInstanceCheckbox, includeGroupCheckbox, submitButton, exportButton,
                        parentColumns, propertyColumns);
        addSelectSampleTypeListeners();
        addSelectGroupListeners();
        addIncludeInstanceListeners();
        addIncludeGroupListeners();
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
                }
            });
    }

    private void addSelectGroupListeners()
    {
        selectGroupCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {

                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    controller.refreshButtons();
                }
            });

        selectGroupCombo.addListener(AppEvents.CALLBACK_FINISHED, new Listener<BaseEvent>()
            {

                //
                // Listener
                //

                public final void handleEvent(final BaseEvent be)
                {
                    controller.refreshGroupCheckbox();
                }
            });
    }

    private void addSelectSampleTypeListeners()
    {
        selectSampleTypeCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
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
                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    final SampleType selectedType = selectSampleTypeCombo.tryGetSelected();
                    final String selectedGroupCode =
                            selectGroupCombo.tryGetSelected() == null ? null : selectGroupCombo
                                    .tryGetSelected().getCode();

                    final Boolean includeGroup = includeGroupCheckbox.getValue();
                    final Boolean includeInstance = includeInstanceCheckbox.getValue();

                    controller.rebuildColumnChooser();
                    sampleBrowserGrid.refresh(selectedType, selectedGroupCode, includeGroup,
                            includeInstance);
                }
            });
        refreshButton.setId(REFRESH_BUTTON_ID);
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
        return button;
    }

    //
    // ToolBar
    //

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        display();
    }

}