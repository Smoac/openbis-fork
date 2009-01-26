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

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ProjectModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * {@link ComboBox} containing list of projects loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectSelectionWidget extends ComboBox<ProjectModel>
{
    private static final String PREFIX = "project-select";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        this.viewContext = viewContext;
        setId(ID + idSuffix);
        setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "projects"));
        setDisplayField(ModelDataPropertyNames.PROJECT_WITH_GROUP);
        setEditable(false);
        setWidth(200);
        setFieldLabel(viewContext.getMessage(Dict.PROJECT));
        setStore(new ListStore<ProjectModel>());
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
        viewContext.getCommonService().listProjects(new ListProjectsCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListProjectsCallback extends AbstractAsyncCallback<List<Project>>
    {
        ListProjectsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<Project> result)
        {
            final ListStore<ProjectModel> projectStore = getStore();
            projectStore.removeAll();
            projectStore.add(ProjectModel.convert(result));
            if (projectStore.getCount() > 0)
            {
                setValue(projectStore.getAt(0));
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "project"));
            }
            fireEvent(AppEvents.CALLBACK_FINISHED);
        }
    }

}