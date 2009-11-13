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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.TreeBuilder;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.CISDBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.TreeItemWithModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link Tree} containing projects loaded from the server. Main items of the tree are project
 * groups and projects are their children.
 * 
 * @author Piotr Buczek
 */
public final class ProjectSelectionTreeWidget extends Tree implements IDatabaseModificationObserver
{

    public static final String ID = GenericConstants.ID_PREFIX + "select-project";

    private final IViewContext<?> viewContext;

    private Project selectedProjectOrNull;

    private SelectionChangedListener<?> selectionChangedListener;

    public ProjectSelectionTreeWidget(final IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        refreshTree();
    }

    /**
     * Returns the {@link Project} currently selected.
     * 
     * @return <code>null</code> if no project is selected.
     */
    public final Project tryGetSelectedProject()
    {
        return selectedProjectOrNull;
    }

    private SelectionChangedListener<?> getSelectionChangedListener()
    {
        return selectionChangedListener;
    }

    public void setSelectionChangedListener(SelectionChangedListener<?> listener)
    {
        selectionChangedListener = listener;
    }

    // Tree building

    private Map<Group, TreeItem> groupItems = new HashMap<Group, TreeItem>();

    /** @return a new {@link TreeItem} for given group */
    private TreeItem createGroupTreeItem(Group group)
    {
        TreeItem result =
                new TreeItemWithModel(new GroupItemModel(group), createSelectItemAction(null));
        groupItems.put(group, result);
        root.add(result);
        return result;
    }

    /** @return a {@link TreeItem} for given group */
    private TreeItem getGroupTreeItem(Group group)
    {
        return groupItems.get(group);
    }

    private void clearTree()
    {
        root.removeAll();
    }

    /**
     * Rebuilds the tree from a list of projects. {@link TreeBuilder} instead.
     */
    private void rebuildTree(List<Project> projects)
    {
        clearTree();
        addGroupItems(projects);
        addProjectItems(projects);
        expandAll();
    }

    /** @return need a sorted set of groups of given <var>projects</var> */
    private Set<Group> getSortedGroups(List<Project> projects)
    {
        Set<Group> groups = new TreeSet<Group>();
        for (final Project project : projects)
        {
            groups.add(project.getGroup());
        }
        return groups;
    }

    /** adds group items for given <var>projects</var> to the tree */
    private void addGroupItems(List<Project> projects)
    {
        for (Group group : getSortedGroups(projects))
        {
            createGroupTreeItem(group);
        }
    }

    /** adds project items for given <var>projects</var> to the tree */
    private void addProjectItems(List<Project> projects)
    {
        for (final Project project : projects)
        {
            TreeItem item =
                    new TreeItemWithModel(new ProjectItemModel(project),
                            createSelectItemAction(project));
            getGroupTreeItem(project.getGroup()).add(item);
        }
    }

    /**
     * @return an {@link IDelegatedAction} that will be executed when given project is selected.
     */
    private IDelegatedAction createSelectItemAction(final Project projectOrNull)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    selectedProjectOrNull = projectOrNull;
                    getSelectionChangedListener().handleEvent(null);
                }
            };
    }

    /**
     * Refreshes the whole tree. If the previously selected project is no longer present in the db,
     * nothing will be selected. Otherwise the previous selection will be preserved.
     */
    public void refreshTree()
    {
        loadData();
    }

    private void loadData()
    {
        DefaultResultSetConfig<String, Project> config = DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listProjects(config, new ListProjectsCallback(viewContext));
    }

    private void selectByIdentifierIfPossible(String projectIdentifier)
    {
        GWTUtils
                .setSelectedItem(this, ModelDataPropertyNames.PROJECT_IDENTIFIER, projectIdentifier);
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        refreshTree();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROJECT);
    }

    // 
    // Helper classes
    //
    private final class ListProjectsCallback extends AbstractAsyncCallback<ResultSet<Project>>
    {
        ListProjectsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ResultSet<Project> result)
        {
            List<Project> projects = result.getList().extractOriginalObjects();
            rebuildTree(projects);

            if (selectedProjectOrNull != null)
            {
                selectByIdentifierIfPossible(selectedProjectOrNull.getIdentifier());
            }
        }
    }

    private static class BaseModelDataWithCode extends CISDBaseModelData
    {
        private static final long serialVersionUID = 1L;

        public BaseModelDataWithCode(String code)
        {
            set(ModelDataPropertyNames.CODE, code);
        }

        @Override
        public String toString()
        {
            return get(ModelDataPropertyNames.CODE);
        }
    }

    public static final String PROJECT_WITH_GROUP_CODE = "projectWithGroupCode";

    private static String getProjectWithGroupCode(Project project)
    {
        return project.getCode() + " (" + project.getGroup().getCode() + ")";
    }

    private static class ProjectItemModel extends BaseModelDataWithCode
    {
        private static final long serialVersionUID = 1L;

        public ProjectItemModel(Project project)
        {
            super(project.getCode());
            set(ModelDataPropertyNames.PROJECT_IDENTIFIER, project.getIdentifier());
            set(PROJECT_WITH_GROUP_CODE, getProjectWithGroupCode(project));
        }
    }

    private static class GroupItemModel extends BaseModelDataWithCode
    {
        private static final long serialVersionUID = 1L;

        public GroupItemModel(Group group)
        {
            super(group.getCode());
        }
    }

}
