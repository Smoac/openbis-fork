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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Presents details of the project.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectViewer extends AbstractViewer<IEntityInformationHolder>
{
    private static final String PREFIX = "project-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TechId projectId;

    // cannot use 'originalData' because Project does not implement IEntityInformationHolder
    private Project originalProject;

    private final CompositeDatabaseModificationObserver modificationObserver;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final TechId projectId)
    {
        ProjectViewer viewer = new ProjectViewer(viewContext, projectId);
        return new DatabaseModificationAwareComponent(viewer, viewer.modificationObserver);
    }

    private ProjectViewer(final IViewContext<ICommonClientServiceAsync> viewContext,
            final TechId projectId)
    {
        super(viewContext, createId(projectId));
        setLayout(new BorderLayout());
        this.projectId = projectId;
        this.modificationObserver = new CompositeDatabaseModificationObserver();
        this.viewContext = viewContext;
        reloadAllData();
    }

    private void reloadAllData()
    {
        reloadData(new ProjectInfoCallback(viewContext, this, modificationObserver));
    }

    public static String createId(final TechId projectId)
    {
        return ID_PREFIX + projectId;
    }

    /**
     * Load the project information.
     */
    protected void reloadData(AbstractAsyncCallback<Project> callback)
    {
        viewContext.getService().getProjectInfo(projectId, callback);
    }

    private AttachmentVersionsSection<Project> createAttachmentsSection(final Project project)
    {
        return new AttachmentVersionsSection<Project>(viewContext.getCommonViewContext(), project);
    }

    private static final class ProjectInfoCallback extends AbstractAsyncCallback<Project>
    {
        private final ProjectViewer viewer;

        private final CompositeDatabaseModificationObserver modificationObserver;

        private ProjectInfoCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final ProjectViewer viewer,
                final CompositeDatabaseModificationObserver modificationObserver)
        {
            super(viewContext);
            this.viewer = viewer;
            this.modificationObserver = modificationObserver;
        }

        @Override
        protected final void process(final Project result)
        {
            viewer.updateOriginalProject(result);
            viewer.removeAll();
            viewer.setScrollMode(Scroll.AUTO);
            AttachmentVersionsSection<Project> attachmentsSection =
                    viewer.createAttachmentsSection(result);
            viewer.add(attachmentsSection, createBorderLayoutData(LayoutRegion.NORTH));
            modificationObserver.addObserver(attachmentsSection.getDatabaseModificationObserver());
            viewer.layout();
        }
    }

    private void updateOriginalProject(Project result)
    {
        this.originalProject = result;
        editButton.enable();
        updateTitle();
    }

    private void updateTitle()
    {
        updateTitle(viewContext.getMessage(Dict.PROJECT) + " " + originalProject.getIdentifier());
    }

    @Override
    protected void showEntityEditor()
    {
        assert originalProject != null;
        ProjectGrid.showEntityViewer(originalProject, true, viewContext);
    }
}
