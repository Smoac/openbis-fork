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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ProjectModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * The toolbar of experiment browser.
 * 
 * @author Izabela Adamczyk
 * @author Christian Ribeaud
 */
class ExperimentBrowserToolbar extends ToolBar
{
    public static final String ID = "experiment-browser-toolbar";

    private static final String PREFIX = ID + "_";

    static final String REFRESH_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "refresh-button";

    private final ExperimentBrowserGrid experimentBrowserGrid;

    private final ExperimentTypeSelectionWidget selectExperimentTypeCombo;

    private final ProjectSelectionWidget selectProjectCombo;

    private final Button submitButton;

    private final Button exportButton;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ExperimentBrowserGrid experimentBrowserGrid)
    {
        this.experimentBrowserGrid = experimentBrowserGrid;
        this.viewContext = viewContext;
        selectExperimentTypeCombo = new ExperimentTypeSelectionWidget(viewContext, ID);
        selectProjectCombo = new ProjectSelectionWidget(viewContext, ID);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        exportButton = createExportButton();
        exportButton.setEnabled(false);
        addSelectSampleTypeListeners();
        addSelectGroupListeners();
    }

    private final void refreshButtons(final Project projectOrNull,
            final ExperimentType experimentTypeOrNull)
    {
        final boolean experimentTypeSelected = experimentTypeOrNull != null;
        final boolean projectChosen = projectOrNull != null;
        final boolean enabled = experimentTypeSelected && projectChosen;
        submitButton.setEnabled(enabled);
        exportButton.setEnabled(enabled);
        if (enabled)
        {
            submitButton.setTitle("Load or update experiment table");
            exportButton.setTitle("Export experiment table to excel file");
        } else
        {
            final String msg = "HINT: To activate choose experiment type and project.";
            submitButton.setTitle(msg);
            exportButton.setTitle(msg);
        }
    }

    private void addSelectGroupListeners()
    {
        selectProjectCombo.addSelectionChangedListener(new SelectionChangedListener<ProjectModel>()
            {
                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<ProjectModel> se)
                {
                    final ProjectModel selectedItem = se.getSelectedItem();
                    refreshButtons(selectedItem != null ? (Project) selectedItem
                            .get(ModelDataPropertyNames.OBJECT) : null, selectExperimentTypeCombo
                            .tryGetSelectedExperimentType());
                }
            });

    }

    private void addSelectSampleTypeListeners()
    {
        selectExperimentTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<ExperimentTypeModel>()
                    {

                        //
                        // SelectionChangedListener
                        //

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<ExperimentTypeModel> se)
                        {
                            final ExperimentTypeModel selectedItem = se.getSelectedItem();
                            refreshButtons(selectProjectCombo.tryGetSelectedProject(),
                                    selectedItem != null ? (ExperimentType) selectedItem
                                            .get(ModelDataPropertyNames.OBJECT) : null);
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        removeAll();
        add(new LabelToolItem(viewContext.getMessage(Dict.EXPERIMENT_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectExperimentTypeCombo));
        add(new SeparatorToolItem());
        add(new LabelToolItem(viewContext.getMessage(Dict.PROJECT)
                + GenericConstants.LABEL_SEPARATOR));
        add(new AdapterToolItem(selectProjectCombo));
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
                                    final ExperimentType selectedType =
                                            selectExperimentTypeCombo
                                                    .tryGetSelectedExperimentType();
                                    assert selectedType != null : "No experiment type selected.";
                                    final Project selectedProject =
                                            selectProjectCombo.tryGetSelectedProject();
                                    assert selectedProject != null : "No project selected.";
                                    experimentBrowserGrid.refresh(selectedType, selectedProject);
                                }
                            });
        refreshButton.setId(REFRESH_BUTTON_ID);
        return refreshButton;
    }

    private final Button createExportButton()
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_EXPORT_DATA),
                        new SelectionListener<ComponentEvent>()
                            {
                                //
                                // SelectionListener
                                //

                                @Override
                                public final void componentSelected(final ComponentEvent ce)
                                {
                                    MessageBox.alert(viewContext
                                            .getMessage(Dict.MESSAGEBOX_WARNING),
                                            "Not yet implemented!", null);
                                }
                            });
        return button;
    }

    @Override
    protected final void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        display();
    }

}