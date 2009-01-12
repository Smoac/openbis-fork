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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ETPTModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * Encapsulates property type assignments listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeAssignmentBrowser extends ContentPanel
{
    public static final String ID =
            GenericConstants.ID_PREFIX + "property-type-assignments-browser";

    private ToolBar toolbar;

    private PropertyTypeAssignmentGrid grid;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public PropertyTypeAssignmentBrowser(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeading("Property Type Assignments");
        setId(ID);
        grid = new PropertyTypeAssignmentGrid(viewContext, ID);
        grid.setGroupRenderer(new GridGroupRenderer()
            {

                public String render(GroupColumnData data)
                {
                    return viewContext.getMessage(Dict.ENTITY_TYPE_ASSIGNMENTS, StringUtils
                            .capitalize(data.group), data.models.size() > 1 ? "s" : "");
                }
            });
        add(grid);
        setBottomComponent(getToolbar());

    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        layout();
        grid.load();
    }

    private ToolBar getToolbar()
    {
        if (toolbar == null)
        {
            toolbar = new ToolBar();
            toolbar.add(new LabelToolItem("Filter:"));
            Store<ETPTModel> store = grid.getStore();
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.PROPERTY_TYPE_CODE, viewContext
                            .getMessage(Dict.PROPERTY_TYPE_CODE))));
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.ENTITY_TYPE_CODE, viewContext
                            .getMessage(Dict.ASSIGNED_TO))));
            toolbar.add(new AdapterToolItem(new ColumnFilter<ETPTModel>(store,
                    ModelDataPropertyNames.ENTITY_KIND, viewContext.getMessage(Dict.TYPE_OF))));
        }
        return toolbar;
    }

}
