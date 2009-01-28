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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ProjectModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GridWithRPCProxy;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * {@link GridWithRPCProxy} displaying projects.
 * 
 * @author Izabela Adamczyk
 */
public class ProjectGrid extends GridWithRPCProxy<Project, ProjectModel>
{
    private final CommonViewContext viewContext;

    public ProjectGrid(final CommonViewContext viewContext, String idPrefix)
    {
        super(createColumnModel(viewContext), idPrefix);
        this.viewContext = viewContext;
    }

    private static ColumnModel createColumnModel(IViewContext<?> context)
    {
        final ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createCodeColumnConfig(context));

        configs.add(ColumnConfigFactory.createDefaultColumnConfig(context
                .getMessage(Dict.DESCRIPTION), ModelDataPropertyNames.DESCRIPTION));

        final ColumnConfig registratorColumnConfig =
                ColumnConfigFactory.createDefaultColumnConfig(context.getMessage(Dict.REGISTRATOR),
                        ModelDataPropertyNames.REGISTRATOR);
        configs.add(registratorColumnConfig);

        final ColumnConfig registrationDateColumnConfig =
                ColumnConfigFactory.createDefaultColumnConfig(context
                        .getMessage(Dict.REGISTRATION_DATE),
                        ModelDataPropertyNames.REGISTRATION_DATE);
        registrationDateColumnConfig.setDateTimeFormat(DateRenderer.DEFAULT_DATE_TIME_FORMAT);
        configs.add(registrationDateColumnConfig);

        return new ColumnModel(configs);
    }

    @Override
    protected void loadDataFromService(AsyncCallback<BaseListLoadResult<ProjectModel>> callback)
    {
        viewContext.getService().listProjects(new ListProjectsCallback(viewContext, callback));
    }

    class ListProjectsCallback extends DelegatingAsyncCallback
    {
        public ListProjectsCallback(IViewContext<?> context,
                AsyncCallback<BaseListLoadResult<ProjectModel>> callback)
        {
            super(context, callback);
        }

        @Override
        protected List<ProjectModel> convert(List<Project> result)
        {
            return ProjectModel.convert(result);
        }
    }
}