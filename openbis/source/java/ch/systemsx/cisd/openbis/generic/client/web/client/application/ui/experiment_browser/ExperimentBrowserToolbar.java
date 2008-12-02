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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser;

import com.extjs.gxt.ui.client.data.ModelData;
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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
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

    public ExperimentBrowserToolbar(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ExperimentBrowserGrid experimentBrowserGrid)
    {
        this.experimentBrowserGrid = experimentBrowserGrid;
        selectExperimentTypeCombo = new ExperimentTypeSelectionWidget(viewContext, ID);
        selectProjectCombo = new ProjectSelectionWidget(viewContext);
        submitButton = createSubmitButton();
        submitButton.setEnabled(false);
        exportButton = createExportButton();
        exportButton.setEnabled(false);
        addSelectSampleTypeListeners();
        addSelectGroupListeners();
    }

    private void refreshButtons()
    {
        final boolean experiementTypeSelected = selectExperimentTypeCombo.tryGetSelected() != null;
        final boolean projectChosen = selectProjectCombo.tryGetSelected() != null;
        final boolean enable = experiementTypeSelected && projectChosen;
        submitButton.setEnabled(enable);
        exportButton.setEnabled(enable);
        if (enable)
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
        selectProjectCombo.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public final void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    refreshButtons();
                }
            });

    }

    private void addSelectSampleTypeListeners()
    {
        selectExperimentTypeCombo
                .addSelectionChangedListener(new SelectionChangedListener<ModelData>()
                    {
                        @Override
                        public final void selectionChanged(final SelectionChangedEvent<ModelData> se)
                        {
                            refreshButtons();
                        }
                    });
    }

    private void display()
    {
        setBorders(true);
        removeAll();
        add(new LabelToolItem("Experiment type:"));
        add(new AdapterToolItem(selectExperimentTypeCombo));

        add(new SeparatorToolItem());

        add(new LabelToolItem("Project:"));
        add(new AdapterToolItem(selectProjectCombo));

        add(new SeparatorToolItem());

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
                public final void componentSelected(final ComponentEvent ce)
                {
                    final ExperimentType selectedType = selectExperimentTypeCombo.tryGetSelected();
                    final Project selectedProject =
                            selectProjectCombo.tryGetSelected() == null ? null : selectProjectCombo
                                    .tryGetSelected();

                    experimentBrowserGrid.refresh(selectedType, selectedProject);
                }
            });
        refreshButton.setId(REFRESH_BUTTON_ID);
        return refreshButton;
    }

    private Button createExportButton()
    {
        final Button button = new Button("Export data", new SelectionListener<ComponentEvent>()
            {
                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    MessageBox.alert("Warning", "Not yet implemented!", null);
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