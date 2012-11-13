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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.browser;

import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.MetaprojectBrowserGrids;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComposite;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class MetaprojectBrowser extends ContentPanel implements IDisposableComponent
{

    public static final String ID = GenericConstants.ID_PREFIX + "metaproject-browser";

    private MetaprojectBrowserTree tree;

    private MetaprojectBrowserGrids grids;

    private DisposableComposite composite;

    public MetaprojectBrowser(final IViewContext<?> viewContext)
    {
        setId(ID);
        setLayout(new BorderLayout());

        tree = new MetaprojectBrowserTree(viewContext);
        // GWTUtils.setToolTip(tree, viewContext.getMessage(Dict.METAPROJECT_BROWSER_TREE_TOOLTIP));

        grids = new MetaprojectBrowserGrids(viewContext);
        composite = new DisposableComposite(this, tree, grids);

        BorderLayoutData treeLayout = new BorderLayoutData(LayoutRegion.WEST, 400, 20, 2000);
        treeLayout.setSplit(true);
        treeLayout.setMargins(new Margins(2));
        treeLayout.setCollapsible(true);
        treeLayout.setFloatable(false);

        BorderLayoutData gridsLayout = new BorderLayoutData(LayoutRegion.CENTER, 200, 20, 2000);
        gridsLayout.setSplit(true);
        gridsLayout.setMargins(new Margins(2));
        gridsLayout.setCollapsible(true);
        gridsLayout.setFloatable(false);

        add(tree, treeLayout);
        add(grids, gridsLayout);

        layout();
    }

    @Override
    public Component getComponent()
    {
        return composite.getComponent();
    }

    @Override
    public void dispose()
    {
        composite.dispose();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return composite.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        composite.update(observedModifications);
    }

}
