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
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComposite;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity.MetaprojectEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.MetaprojectTree;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeEntityKindItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model.MetaprojectTreeMetaprojectItemData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class MetaprojectBrowser extends ContentPanel implements IDisposableComponent
{

    // TODO add loading to tree and entities (Loading class)

    // TODO add remove assignments button to entity grids

    // TODO check ids are correct (maybe pass idPrefix)

    public static final String ID = GenericConstants.ID_PREFIX + "metaproject-browser";

    private MetaprojectTree tree;

    private MetaprojectEntities entities;

    private DisposableComposite composite;

    public MetaprojectBrowser(final IViewContext<?> viewContext)
    {
        setId(ID);
        setLayout(new BorderLayout());

        tree = new MetaprojectTree(viewContext, getId());
        tree.getSelectionModel().addSelectionChangedListener(
                new SelectionChangedListener<MetaprojectTreeItemData>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<MetaprojectTreeItemData> se)
                        {
                            MetaprojectTreeItemData item = se.getSelectedItem();

                            if (item instanceof MetaprojectTreeMetaprojectItemData)
                            {
                                MetaprojectTreeMetaprojectItemData metaprojectItem =
                                        (MetaprojectTreeMetaprojectItemData) item;
                                entities.showEntities(metaprojectItem.getMetaproject().getId());
                            } else if (item instanceof MetaprojectTreeEntityKindItemData)
                            {
                                MetaprojectTreeEntityKindItemData entityKindItem =
                                        (MetaprojectTreeEntityKindItemData) item;
                                entities.showEntities(entityKindItem.getMetaprojectId(),
                                        entityKindItem.getEntityKind());
                            } else if (item instanceof MetaprojectTreeEntityItemData)
                            {
                                MetaprojectTreeEntityItemData entityItem =
                                        (MetaprojectTreeEntityItemData) item;
                                entities.showEntities(entityItem.getMetaprojectId(), entityItem
                                        .getEntity().getEntityKind());
                            }
                        }
                    });

        GWTUtils.setToolTip(tree, viewContext.getMessage(Dict.METAPROJECT_BROWSER_TREE_TOOLTIP));
        entities = new MetaprojectEntities(viewContext, getId());
        composite = new DisposableComposite(this, tree, entities);

        BorderLayoutData treeLayout = new BorderLayoutData(LayoutRegion.WEST, 300, 20, 2000);
        treeLayout.setSplit(true);
        treeLayout.setMargins(new Margins(2));
        treeLayout.setCollapsible(true);
        treeLayout.setFloatable(false);

        BorderLayoutData gridsLayout = new BorderLayoutData(LayoutRegion.CENTER, 200, 20, 2000);
        gridsLayout.setSplit(true);
        gridsLayout.setMargins(new Margins(2));
        gridsLayout.setCollapsible(true);
        gridsLayout.setFloatable(false);

        add(new MetaprojectBrowserTreePanel(viewContext, tree), treeLayout);
        add(entities, gridsLayout);

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
