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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type_browser;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PropertyTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;

/**
 * Encapsulates property types listing functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyTypeBrowser extends ContentPanel
{

    public static final String ID = GenericConstants.ID_PREFIX + "property-types-browser";

    public static final String GRID_ID = ID + "_grid";

    private final CommonViewContext viewContext;

    public PropertyTypeBrowser(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setHeading("Property types");
        setId(ID);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void display(final List<PropertyType> propertyTypes)
    {
        removeAll();
        final ColumnModel cm = createColumns();
        final ListStore<PropertyTypeModel> store = new ListStore<PropertyTypeModel>();
        store.add(getPropertyTypeModels(propertyTypes));
        final Grid<PropertyTypeModel> grid = new Grid<PropertyTypeModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setId(GRID_ID);
        add(grid);
        layout();
    }

    private ColumnModel createColumns()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext.getMessageProvider()
                .getMessage("label"), ModelDataPropertyNames.LABEL));
        configs.add(ColumnConfigFactory.createCodeColumnConfig(viewContext.getMessageProvider()));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext.getMessageProvider()
                .getMessage("data_type"), ModelDataPropertyNames.DATA_TYPE));
        configs
                .add(ColumnConfigFactory.createDefaultColumnConfig(viewContext.getMessageProvider()
                        .getMessage("controlled_vocabulary"),
                        ModelDataPropertyNames.CONTROLLED_VOCABULARY));
        configs.add(ColumnConfigFactory.createDefaultColumnConfig(viewContext.getMessageProvider()
                .getMessage("description"), ModelDataPropertyNames.DESCRIPTION));
        return new ColumnModel(configs);
    }

    List<PropertyTypeModel> getPropertyTypeModels(final List<PropertyType> propertyTypes)
    {
        final List<PropertyTypeModel> list = new ArrayList<PropertyTypeModel>();
        for (final PropertyType pt : propertyTypes)
        {
            list.add(new PropertyTypeModel(pt));
        }
        return list;
    }

    public void refresh()
    {
        viewContext.getService().listPropertyTypes(new ListPropertyTypesCallback(viewContext));
    }

    public final class ListPropertyTypesCallback extends AbstractAsyncCallback<List<PropertyType>>
    {
        private ListPropertyTypesCallback(final CommonViewContext viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final List<PropertyType> propertyTypes)
        {
            display(propertyTypes);
        }
    }
}
