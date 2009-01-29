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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ProjectModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;

/**
 * A {@link ContentPanel} extension for browsing the projects.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectBrowser extends ContentPanel
{
    private static final String PREFIX = "project-browser";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private ProjectGrid grid;

    private ToolBar toolbar;

    public ProjectBrowser(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeading(viewContext.getMessage(Dict.PROJECT_BROWSER));
        setId(ID);
        grid = new ProjectGrid(viewContext, ID);
        add(grid);
        setBottomComponent(getToolbar());
    }

    private ToolBar getToolbar()
    {
        if (toolbar == null)
        {
            toolbar = new ToolBar();
            toolbar.add(new LabelToolItem("Filter:"));
            Store<ProjectModel> store = grid.getStore();
            toolbar.add(new AdapterToolItem(new ColumnFilter<ProjectModel>(store,
                    ModelDataPropertyNames.CODE, viewContext.getMessage(Dict.CODE))));
        }
        return toolbar;
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        layout();
        grid.load();
    }

}
