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

/**
 * Allows to change visibility and order of the columns.
 * 
 * @author Izabela Adamczyk
 */
class ColumnChooser
{

    final Grid<ColumnDataModel> grid;

    public ColumnChooser(List<ColumnDataModel> list)
    {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        CheckColumnConfig checkColumn =
                new CheckColumnConfig(ColumnDataModel.CHECKED, "Shown?", 45);
        configs.add(checkColumn);
        configs.add(new ColumnConfig(ColumnDataModel.HEADER, "Column", 200));

        grid = new Grid<ColumnDataModel>(createStore(list), new ColumnModel(configs));
        grid.setHideHeaders(true);
        grid.addPlugin(checkColumn);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setAutoExpandColumn(ColumnDataModel.HEADER);

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