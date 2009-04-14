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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * Presents details of the project.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectViewer extends ContentPanel
{
    private static final String PREFIX = "project-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final String projectIdentifier;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public ProjectViewer(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String projectIdentifier)
    {
        this.viewContext = viewContext;
        setHeading(viewContext.getMessage(Dict.PROJECT) + " " + projectIdentifier);
        setId(createId(projectIdentifier));
        this.projectIdentifier = projectIdentifier;
        loadData();
    }

    public static String createId(String projectIdentifier)
    {
        return ID_PREFIX + projectIdentifier;
    }

    private static void addSection(final LayoutContainer lc, final Widget w)
    {
        lc.add(w, new RowData(-1, -1, new Margins(5)));
    }

    /**
     * Load the experiment information.
     */
    private void loadData()
    {
        viewContext.getService().getProjectInfo(projectIdentifier,
                new ProjectInfoCallback(viewContext, this));
    }

    public static final class ProjectInfoCallback extends AbstractAsyncCallback<Project>
    {
        private final ProjectViewer viewer;

        private ProjectInfoCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                final ProjectViewer viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        @Override
        // TODO 2009-04-01, Tomasz Pylak: add attachments auto-refresh
        protected final void process(final Project result)
        {
            viewer.removeAll();
            viewer.setScrollMode(Scroll.AUTO);
            addSection(viewer, new AttachmentsSection(result, viewContext));
            viewer.layout();
        }
    }
}