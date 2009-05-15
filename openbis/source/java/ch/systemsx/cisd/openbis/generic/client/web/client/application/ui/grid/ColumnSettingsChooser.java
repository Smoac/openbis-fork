package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Allows to change visibility and order of the grid columns.
 * 
 * @author Izabela Adamczyk
 */
class ColumnSettingsChooser
{

    private final Grid<ColumnDataModel> grid;

    public ColumnSettingsChooser(List<ColumnDataModel> list, IMessageProvider messageProvider)
    {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        CheckColumnConfig isVisibleColumn =
                new CheckColumnConfig(ColumnDataModel.IS_VISIBLE, messageProvider
                        .getMessage(Dict.GRID_IS_COLUMN_VISIBLE_HEADER), 55);
        CheckColumnConfig hasFilterColumn =
                new CheckColumnConfig(ColumnDataModel.HAS_FILTER, messageProvider
                        .getMessage(Dict.GRID_COLUMN_HAS_FILTER_HEADER), 75);
        ColumnConfig nameColumn =
                new ColumnConfig(ColumnDataModel.HEADER, messageProvider
                        .getMessage(Dict.GRID_COLUMN_NAME_HEADER), 300);
        configs.add(isVisibleColumn);
        configs.add(hasFilterColumn);
        configs.add(nameColumn);
        for (ColumnConfig column : configs)
        {
            column.setSortable(false);
            column.setMenuDisabled(true);
        }

        grid = new Grid<ColumnDataModel>(createStore(list), new ColumnModel(configs));
        grid.setHideHeaders(false);
        grid.addPlugin(isVisibleColumn);
        grid.addPlugin(hasFilterColumn);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // grid.setAutoExpandColumn(ColumnDataModel.HEADER);

        //
        // TODO 2009-05-06 Izabela Adamczyk: Code below can be used to allow DND after we migrate to
        // GXT 1.2.3, which fixes bug wit disappearing column
        //
        // new GridDragSource(grid);
        // GridDropTarget target = new GridDropTarget(grid);
        // target.setAllowSelfAsSource(true);
        // target.setOperation(Operation.MOVE);
        // target.setFeedback(Feedback.INSERT);
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
        cp.getButtonBar().setButtonWidth(110);
        cp.setHeaderVisible(false);
        Button up = new Button("Move Up");
        up.setTitle("Move selected column to the left");
        up.addSelectionListener(moveSelectedItem(-1));
        cp.addButton(up);
        Button down = new Button("Move Down");
        down.setTitle("Move selected column to the right");
        down.addSelectionListener(moveSelectedItem(+1));
        cp.addButton(down);
        grid.setAutoWidth(true);
        grid.setAutoHeight(true);
        cp.add(grid);
        cp.setScrollMode(Scroll.AUTOY);
        return cp;
    }

    private SelectionListener<ComponentEvent> moveSelectedItem(final int delta)
    {
        return new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    ColumnDataModel m = grid.getSelectionModel().getSelectedItem();
                    if (m == null)
                    {
                        return;
                    }
                    int oldIndex = grid.getStore().indexOf(m);
                    int newIndex = oldIndex + delta;
                    if (newIndex >= 0 && newIndex < grid.getStore().getCount())
                    {
                        grid.getStore().remove(m);
                        grid.getStore().insert(m, newIndex);
                        grid.getSelectionModel().select(m);
                    }
                }
            };
    }

    public List<ColumnDataModel> getModels()
    {
        return grid.getStore().getModels();
    }

}