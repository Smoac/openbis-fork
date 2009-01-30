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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;

/**
 * {@link ComboBox} containing list of projects loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectSelectionWidget extends
        ComboBox<ProjectSelectionWidget.ProjectComboModel>
{
    // @Private
    static final String DISPLAY_COLUMN_ID = "id";

    private static class ProjectComboModel extends BaseModelData
    {
        private static final long serialVersionUID = 1L;

        public ProjectComboModel(Project project)
        {
            set(DISPLAY_COLUMN_ID, renderProjectWithGroup(project));
            set(ModelDataPropertyNames.OBJECT, project);
        }

        private String renderProjectWithGroup(final Project p)
        {
            return p.getCode() + " (" + p.getGroup().getCode() + ")";
        }
    }

    private static final String PREFIX = "project-select";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        this.viewContext = viewContext;
        setId(ID + idSuffix);
        setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "projects"));
        setDisplayField(DISPLAY_COLUMN_ID);
        setEditable(false);
        setWidth(200);
        setFieldLabel(viewContext.getMessage(Dict.PROJECT));
        setStore(new ListStore<ProjectComboModel>());
    }

    /**
     * Returns the {@link Project} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Project tryGetSelectedProject()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        DefaultResultSetConfig<String, Project> config = DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listProjects(config, new ListProjectsCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListProjectsCallback extends AbstractAsyncCallback<ResultSet<Project>>
    {
        ListProjectsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ResultSet<Project> result)
        {
            final ListStore<ProjectComboModel> projectStore = getStore();
            projectStore.removeAll();
            projectStore.add(convert(result.getList()));
            if (projectStore.getCount() > 0)
            {
                setValue(projectStore.getAt(0));
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "project"));
            }
            fireEvent(AppEvents.CALLBACK_FINISHED);
        }
    }

    private static List<ProjectComboModel> convert(List<Project> projects)
    {
        final List<ProjectComboModel> result = new ArrayList<ProjectComboModel>();
        for (final Project p : projects)
        {
            result.add(new ProjectComboModel(p));
        }
        return result;
    }
}