/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ProjectViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.ProjectColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * Grid displaying projects.
 * 
 * @author Tomasz Pylak
 */
public class ProjectGrid extends AbstractSimpleBrowserGrid<Project>
{
    // browser consists of the grid and the paging toolbar
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "project-browser";

    public static final String GRID_ID = BROWSER_ID + "_grid";

    public static final String SHOW_DETAILS_BUTTON_ID = BROWSER_ID + "-show-details";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectGrid grid = new ProjectGrid(viewContext);
        return grid.asDisposableWithToolbar(grid.createToolbar());
    }

    private ProjectGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
    }

    private final Component createToolbar()
    {
        ToolBar toolbar = new ToolBar();
        toolbar.add(new FillToolItem());
        Button showDetailsButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                        new ISelectedEntityInvoker<BaseEntityModel<Project>>()
                            {
                                public void invoke(BaseEntityModel<Project> selectedItem)
                                {
                                    showEntityViewer(selectedItem, false);
                                }
                            });
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        toolbar.add(new AdapterToolItem(showDetailsButton));
        return toolbar;
    }

    @Override
    protected IColumnDefinitionKind<Project>[] getStaticColumnsDefinition()
    {
        return ProjectColDefKind.values();
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Project> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Project>> callback)
    {
        viewContext.getService().listProjects(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Project> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportProjects(exportCriteria, callback);
    }

    @Override
    protected List<IColumnDefinition<Project>> getAvailableFilters()
    {
        return asColumnFilters(new ProjectColDefKind[]
            { ProjectColDefKind.CODE, ProjectColDefKind.GROUP });
    }

    @Override
    protected void showEntityViewer(BaseEntityModel<Project> modelData, boolean editMode)
    {
        final Project project = modelData.getBaseObject();
        final ITabItemFactory tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    return DefaultTabItem.createUnaware(new ProjectViewer(viewContext, project
                            .getIdentifier()), false);
                }

                public String getId()
                {
                    return ProjectViewer.createId(project.getIdentifier());
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.PROJECT) };
    }
}