package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Allows to select columns which should be used in the filter.
 * 
 * @author Izabela Adamczyk
 */
class FilterColumnChooser
{

    private final Grid<ColumnDataModel> grid;

    public FilterColumnChooser(List<ColumnDataModel> list, IMessageProvider messageProvider)
    {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig addressColumn =
                new ColumnConfig(ColumnDataModel.HEADER, messageProvider
                        .getMessage(Dict.GRID_COLUMN_NAME_HEADER), 300);
        ColumnConfig idColumn =
                new ColumnConfig(ColumnDataModel.ADDRESS, messageProvider
                        .getMessage(Dict.HOW_TO_ADDRESS), 300);

        configs.add(addressColumn);
        configs.add(idColumn);
        for (ColumnConfig column : configs)
        {
            column.setSortable(false);
            column.setMenuDisabled(true);
        }

        grid = new Grid<ColumnDataModel>(createStore(list), new ColumnModel(configs));
        grid.setHideHeaders(false);
        grid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
    }

    private static ListStore<ColumnDataModel> createStore(List<ColumnDataModel> list)
    {
        ListStore<ColumnDataModel> store = new ListStore<ColumnDataModel>();
        store.add(list);
        return store;
    }

    public Component getComponent()
    {
        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        grid.setAutoWidth(true);
        grid.setAutoHeight(true);
        cp.add(grid);
        cp.setScrollMode(Scroll.AUTOY);
        return cp;
    }

    public List<ColumnDataModel> getModels()
    {
        return grid.getStore().getModels();
    }

}