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

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ProjectViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.ProjectColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
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

    public static final String EDIT_BUTTON_ID = BROWSER_ID + "-edit";

    public static IDisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ProjectGrid grid = new ProjectGrid(viewContext);
        grid.extendBottomToolbar();
        return grid.asDisposableWithoutToolbar();
    }

    private ProjectGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
        setDisplayTypeIDGenerator(DisplayTypeIDGenerator.PROJECT_BROWSER_GRID);
    }

    private void extendBottomToolbar()
    {
        addEntityOperationsLabel();

        Button showDetailsButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_SHOW_DETAILS),
                        new ISelectedEntityInvoker<BaseEntityModel<Project>>()
                            {
                                public void invoke(BaseEntityModel<Project> selectedItem)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), false);
                                }
                            });
        showDetailsButton.setId(SHOW_DETAILS_BUTTON_ID);
        pagingToolbar.add(new AdapterToolItem(showDetailsButton));

        Button editButton =
                createSelectedItemButton(viewContext.getMessage(Dict.BUTTON_EDIT),
                        new ISelectedEntityInvoker<BaseEntityModel<Project>>()
                            {
                                public void invoke(BaseEntityModel<Project> selectedItem)
                                {
                                    showEntityViewer(selectedItem.getBaseObject(), true);
                                }
                            });
        editButton.setId(EDIT_BUTTON_ID);
        pagingToolbar.add(new AdapterToolItem(editButton));

        addEntityOperationsSeparator();
    }

    @Override
    protected IColumnDefinitionKind<Project>[] getStaticColumnsDefinition()
    {
        return ProjectColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<Project> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Project> schema = super.createColumnsDefinition();
        schema.setGridCellRendererFor(ProjectColDefKind.CODE.id(), LinkRenderer
                .createGridCellRenderer());
        return schema;
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
    protected List<IColumnDefinition<Project>> getInitialFilters()
    {
        return asColumnFilters(new ProjectColDefKind[]
            { ProjectColDefKind.CODE, ProjectColDefKind.GROUP });
    }

    @Override
    protected void showEntityViewer(final Project project, boolean editMode)
    {
        showEntityViewer(project, editMode, viewContext);
    }

    public static void showEntityViewer(final Project project, boolean editMode,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ITabItemFactory tabFactory;
        final TechId projectId = TechId.create(project);
        if (editMode == false)
        {
            tabFactory = new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                ProjectViewer.create(viewContext, projectId);
                        return DefaultTabItem.create(getViewerTitle(), viewer, viewContext, false);
                    }

                    public String getId()
                    {
                        return ProjectViewer.createId(projectId);
                    }

                    private String getViewerTitle()
                    {
                        return AbstractViewer.getTitle(viewContext, Dict.PROJECT, project);
                    }
                };
        } else
        {
            tabFactory = new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                ProjectEditForm.create(viewContext, projectId);
                        return DefaultTabItem.create(getEditTitle(), component, viewContext, true);
                    }

                    public String getId()
                    {
                        return ProjectEditForm.createId(projectId);
                    }

                    private String getEditTitle()
                    {
                        return AbstractRegistrationForm.getEditTitle(viewContext, Dict.PROJECT,
                                project);
                    }
                };
        }
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.PROJECT),
                    DatabaseModificationKind.edit(ObjectKind.PROJECT) };
    }
}