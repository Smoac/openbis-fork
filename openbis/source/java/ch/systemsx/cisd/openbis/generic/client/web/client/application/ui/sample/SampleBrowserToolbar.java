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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * The toolbar of sample browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
final class SampleBrowserToolbar extends ToolBar
{
    public static final String ID = "sample-browser-toolbar";

    private static final String PREFIX = ID + "_";

    static final String REFRESH_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "refresh-button";

    public static final String INCLUDE_GROUP_CHECKBOX_ID =
            GenericConstants.ID_PREFIX + PREFIX + "include-group-checkbox";

    private final SampleBrowserGrid sampleBrowserGrid;

    private final SampleTypeSelectionWidget selectSampleTypeCombo;

    private final GroupSelectionWidget selectGroupCombo;

    private final ToolbarController controller;

    private final Button submitButton;

    private final Button exportButton;

    private final IViewContext<?> viewContext;

    public SampleBrowserToolbar(final IViewContext<?> viewContext,
            final SampleBrowserGrid sampleBrowserGrid)
    {
        this.sampleBrowserGrid = sampleBrowserGrid;
        this.viewContext = viewContext;
        selectSampleTypeCombo = new SampleTypeSelectionWidget(viewContext, ID, true);
        selectGroupCombo = new GroupSelectionWidget(viewContext, ID);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        exportButton = createExportButton();
        exportButton.setEnabled(false);
        controller = new ToolbarController(submitButton, exportButton);
        controller.disableExportButton();
        addSelectSampleTypeListeners();
        addSelectGroupListeners();
    }

    private void addSelectGroupListeners()
    {
        selectGroupCombo.addSelectionChangedListener(new SelectionChangedListener<GroupModel>()
            {

                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<GroupModel> se)
                {
                    final GroupModel selectedItem = se.getSelectedItem();
                    Group group =
                            selectedItem != null ? (Group) selectedItem
                                    .get(ModelDataPropertyNames.OBJECT) : null;
                    controller.refreshSubmitButtons(selectSampleTypeCombo
                            .tryGetSelectedSampleType(), group);
                }
            });
    }

    private void addSelectSampleTypeListeners()
    {
        selectSampleTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                    {
                        //
                        // SelectionChangedListener
                        //

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<SampleTypeModel> se)
                        {
                            final SampleTypeModel selectedItem = se.getSelectedItem();
                            SampleType sampleType =
                                    selectedItem != null ? (SampleType) selectedItem
                                            .get(ModelDataPropertyNames.OBJECT) : null;
                            controller.refreshSubmitButtons(sampleType, selectGroupCombo
                                    .tryGetSelectedGroup());
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        removeAll();
        add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectSampleTypeCombo));
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.GROUP) + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectGroupCombo));
        add(new FillToolItem());
        add(new AdapterToolItem(submitButton));
        add(new SeparatorToolItem());
        add(new AdapterToolItem(exportButton));
        layout();
    }

    private Button createSubmitButton()
    {
        final Button refreshButton =
                new Button(viewContext.getMessage(Dict.BUTTON_REFRESH),
                        new SelectionListener<ButtonEvent>()
                            {
                                //
                                // SelectionListener
                                //

                                @Override
                                public final void componentSelected(final ButtonEvent ce)
                                {
                                    final SampleType selectedType =
                                            selectSampleTypeCombo.tryGetSelectedSampleType();
                                    assert selectedType != null : "No sample type is selected.";
                                    final Group selectedGroup =
                                            selectGroupCombo.tryGetSelectedGroup();
                                    assert selectedGroup != null : "No group is selected.";
                                    final boolean includeInstance =
                                            GroupSelectionWidget.isSharedGroup(selectedGroup);
                                    final boolean includeGroup = (includeInstance == false);

                                    sampleBrowserGrid.refresh(selectedType,
                                            selectedGroup.getCode(), includeGroup, includeInstance,
                                            createPostRefreshCallback());
                                }

                                private IDataRefreshCallback createPostRefreshCallback()
                                {
                                    return new IDataRefreshCallback()
                                        {
                                            public void postRefresh()
                                            {
                                                controller.enableExportButton();
                                            }
                                        };
                                }
                            });
        refreshButton.setId(REFRESH_BUTTON_ID);
        return refreshButton;
    }

    private Button createExportButton()
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_EXPORT_DATA),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    sampleBrowserGrid.export();
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