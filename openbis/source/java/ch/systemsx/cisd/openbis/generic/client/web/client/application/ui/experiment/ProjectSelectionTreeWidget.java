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
import java.util.Set;
import java.util.TreeSet;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ProjectViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.NonHierarchicalBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link TreePanel} containing projects loaded from the server. Main items of the tree are project
 * groups and projects are their children.
 * 
 * @author Piotr Buczek
 */
public final class ProjectSelectionTreeWidget extends TreePanel<ModelData> implements
        IDatabaseModificationObserver
{

    public static final String ID = GenericConstants.ID_PREFIX + "select-project";

    private final IViewContext<?> viewContext;

    private Project selectedProjectOrNull;

    private SelectionChangedListener<?> selectionChangedListener;

    public ProjectSelectionTreeWidget(final IViewContext<?> viewContext)
    {
        super(new TreeStore<ModelData>());
        this.viewContext = viewContext;
        setId(ID);
        setDisplayProperty(ModelDataPropertyNames.CODE);
        switchOffFolderIcons();
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        refreshTree();

        final Menu contextMenu = createContextMenu();
        setContextMenu(contextMenu);

        getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    ModelData selected = se.getSelectedItem();
                    if (selected != null && isLeaf(selected))
                    {
                        contextMenu.enable(); // context menu is enabled for projects
                        selectedProjectOrNull =
                                (Project) selected.get(ModelDataPropertyNames.OBJECT);
                        getSelectionChangedListener().handleEvent(null);
                    } else
                    {
                        contextMenu.disable(); // context menu is disabled for groups
                    }
                }
            });

    }

    private Menu createContextMenu()
    {
        final Menu contextMenu = new Menu();
        final MenuItem details = new MenuItem("details");
        details.addSelectionListener(new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    // show project detail view if project context menu was selected
                    if (selectedProjectOrNull != null)
                    {
                        showProjectDetailsView(selectedProjectOrNull);
                    }
                }
            });
        contextMenu.add(details);
        return contextMenu;
    }

    private void showProjectDetailsView(final Project project)
    {
        ITabItemFactory tabFactory;
        final TechId projectId = TechId.create(project);
        tabFactory = new ITabItemFactory()
            {
                public ITabItem create()
                {
                    final DatabaseModificationAwareComponent viewer =
                            ProjectViewer.create(viewContext.getCommonViewContext(), projectId);
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
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private void switchOffFolderIcons()
    {
        getStyle().setNodeCloseIcon(null);
        getStyle().setNodeOpenIcon(null);
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

    private void clearTree()
    {
        getStore().removeAll();
    }

    /**
     * Rebuilds the tree from a list of projects.
     */
    private void rebuildTree(List<Project> projects)
    {
        clearTree();
        addToStore(projects);
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

    /** adds items for given <var>projects</var> to the tree */
    private void addToStore(List<Project> projects)
    {
        for (Group group : getSortedGroups(projects))
        {
            GroupItemModel groupModel = new GroupItemModel(group);
            getStore().add(groupModel, true);
            setLeaf(groupModel, false);
            for (Project project : projects)
            {
                if (project.getGroup().equals(group))
                {
                    ProjectItemModel projectModel = new ProjectItemModel(project);
                    getStore().add(groupModel, projectModel, false);
                    setLeaf(projectModel, true);
                }
            }
        }
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

    private static class BaseModelDataWithCode extends NonHierarchicalBaseModelData
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

    private static class ProjectItemModel extends BaseModelDataWithCode
    {

        private static final long serialVersionUID = 1L;

        private static String getProjectWithGroupCode(Project project)
        {
            return project.getCode() + " (" + project.getGroup().getCode() + ")";
        }

        public ProjectItemModel(Project project)
        {
            super(project.getCode());
            set(ModelDataPropertyNames.PROJECT_IDENTIFIER, project.getIdentifier());
            set(ModelDataPropertyNames.OBJECT, project);
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
