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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of projects loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectSelectionWidget extends
        DropDownList<ProjectSelectionWidget.ProjectComboModel, Project>
{
    private static final String EMPTY_RESULT_SUFFIX = "projects";

    private static final String CHOOSE_SUFFIX = "project";

    // @Private
    static final String DISPLAY_COLUMN_ID = "id";

    static class ProjectComboModel extends BaseModelData
    {
        private static final long serialVersionUID = 1L;

        public ProjectComboModel(Project project)
        {
            set(DISPLAY_COLUMN_ID, renderProjectWithGroup(project));
            set(ModelDataPropertyNames.PROJECT_IDENTIFIER, project.getIdentifier());
            set(ModelDataPropertyNames.OBJECT, project);
        }

        private String renderProjectWithGroup(final Project p)
        {
            return p.getCode() + " (" + p.getGroup().getCode() + ")";
        }
    }

    public static final String SUFFIX = CHOOSE_SUFFIX;

    private final IViewContext<?> viewContext;

    private final Group groupOrNull;

    private String initialProjectIdentifierOrNull;

    private boolean dataLoaded = false;

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        this(viewContext, idSuffix, null, null);
    }

    /** @param groupOrNull if specified, only projects from that group will be presented */
    public ProjectSelectionWidget(final IViewContext<?> viewContext, Group groupOrNull,
            final String idSuffix)
    {
        this(viewContext, idSuffix, groupOrNull, null);
    }

    public ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            String initialProjectIdentifier)
    {
        this(viewContext, idSuffix, null, initialProjectIdentifier);
    }

    private ProjectSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            Group groupOrNull, String initialProjectIdentifier)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PROJECT, DISPLAY_COLUMN_ID, CHOOSE_SUFFIX,
                EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        this.groupOrNull = groupOrNull;
        this.initialProjectIdentifierOrNull = initialProjectIdentifier;
    }

    /**
     * Returns the {@link Project} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Project tryGetSelectedProject()
    {
        return super.tryGetSelected();
    }

    private final class ListProjectsCallback extends AbstractAsyncCallback<ResultSet<Project>>
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
            projectStore.add(convertItems(result.getList().extractOriginalObjects()));
            if (projectStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            applyEmptyText();
            dataLoaded = true;
            selectProjectAndUpdateOriginal(initialProjectIdentifierOrNull);
        }

    }

    public void selectProjectAndUpdateOriginal(String newProjectIdentifier)
    {
        this.initialProjectIdentifierOrNull = newProjectIdentifier;
        if (dataLoaded && initialProjectIdentifierOrNull != null)
        {
            trySelectByIdentifier(initialProjectIdentifierOrNull);
            updateOriginalValue();
        }
    }

    public void updateOriginalValue()
    {
        setOriginalValue(getValue());
    }

    @Override
    protected List<ProjectComboModel> convertItems(List<Project> projects)
    {
        final List<ProjectComboModel> result = new ArrayList<ProjectComboModel>();
        for (final Project p : projects)
        {
            if (matchesTheGroup(p))
            {
                result.add(new ProjectComboModel(p));
            }
        }
        return result;
    }

    private boolean matchesTheGroup(Project project)
    {
        if (groupOrNull == null)
        {
            return true;
        }
        Group projectGroup = project.getGroup();
        return projectGroup.getCode().equals(groupOrNull.getCode());
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Project>> callback)
    {
        DefaultResultSetConfig<String, Project> config = DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listProjects(config, new ListProjectsCallback(viewContext));
        callback.ignore();
    }

    public void trySelectByIdentifier(String projectIdentifier)
    {
        GWTUtils
                .setSelectedItem(this, ModelDataPropertyNames.PROJECT_IDENTIFIER, projectIdentifier);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROJECT);
    }

}
