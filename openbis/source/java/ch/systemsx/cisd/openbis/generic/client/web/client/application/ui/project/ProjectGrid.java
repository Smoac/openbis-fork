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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.columns.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

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

    public static DisposableComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        return new ProjectGrid(viewContext).asDisposableWithoutToolbar();
    }

    private ProjectGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, BROWSER_ID, GRID_ID);
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
}