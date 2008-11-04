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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.XDOM;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;

/**
 * A {@link LayoutContainer} extension which displays the matching entities.
 * 
 * @author Christian Ribeaud
 */
public class MatchingEntitiesPanel extends LayoutContainer
{
    private static final String PREFIX = "global-search_";

    static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    public MatchingEntitiesPanel(final IViewContext<?> viewContext,
            final List<MatchingEntity> matchingEntities)
    {
        this.viewContext = viewContext;
        setId(ID + XDOM.getUniqueId());
        setLayout(new FitLayout());
        add(createGrid(matchingEntities));
    }

    private final Grid<MatchingEntityModel> createGrid(final List<MatchingEntity> matchingEntities)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig columnConfig =
                ColumnConfigFactory.createDefaultConfig(viewContext.getMessageProvider(),
                        ModelDataPropertyNames.IDENTIFIER);
        columnConfig.setWidth(120);

        configs.add(columnConfig);

        final ColumnModel columnModel = new ColumnModel(configs);

        final ListStore<MatchingEntityModel> store = new ListStore<MatchingEntityModel>();
        store.add(MatchingEntityModel.convert(matchingEntities));
        final Grid<MatchingEntityModel> grid = new Grid<MatchingEntityModel>(store, columnModel);
        return grid;
    }
}
