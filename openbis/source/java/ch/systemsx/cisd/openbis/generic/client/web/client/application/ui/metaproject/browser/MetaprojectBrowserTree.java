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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.MetaprojectTree;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public final class MetaprojectBrowserTree extends ContentPanel implements IDisposableComponent
{

    public static final String ID = GenericConstants.ID_PREFIX + "metaproject-browser-tree-panel";

    private final MetaprojectTree tree;

    public MetaprojectBrowserTree(final IViewContext<?> viewContext)
    {
        setLayout(new FitLayout());
        setBodyBorder(false);
        setHeading(viewContext.getMessage(Dict.METAPROJECT_BROWSER_TREE_TITLE));

        tree = new MetaprojectTree(viewContext);
        add(tree);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return null;
    }

    @Override
    public Component getComponent()
    {
        return null;
    }

    @Override
    public void dispose()
    {

    }

}