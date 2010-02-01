package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Allows to change visibility and order of the grid columns.
 * 
 * @author Izabela Adamczyk
 */
class ColumnSettingsChooser
{

    private final Grid<ColumnDataModel> grid;

    private final AbstractColumnSettingsDataModelProvider columnDataModelProvider;

    public ColumnSettingsChooser(AbstractColumnSettingsDataModelProvider columnDataModelProvider,
            IMessageProvider messageProvider)
    {
        this.columnDataModelProvider = columnDataModelProvider;

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

        grid = new Grid<ColumnDataModel>(createStore(), new ColumnModel(configs));
        grid.setHideHeaders(false);
        grid.addPlugin(isVisibleColumn);
        grid.addPlugin(hasFilterColumn);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void enableButtonOnGridSelectedItem(final Button button, final String enabledTitle)
    {
        final String disabledTitle = "Select a table row first.";
        button.disable();
        GWTUtils.setToolTip(button, disabledTitle);

        grid.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<ModelData>>()
                    {
                        public void handleEvent(SelectionChangedEvent<ModelData> se)
                        {
                            if (grid.getSelectionModel().getSelectedItems().size() == 1)
                            {
                                button.enable();
                                GWTUtils.setToolTip(button, enabledTitle);
                            } else
                            {
                                button.disable();
                                GWTUtils.setToolTip(button, disabledTitle);
                            }
                        }
                    });
    }

    private ListStore<ColumnDataModel> createStore()
    {
        List<ColumnDataModel> columnDataModels = columnDataModelProvider.getColumnDataModels();

        // we want to create a store with new items but the order of old items should be kept
        List<ColumnDataModel> newOrderedItems;
        if (grid == null || grid.getStore() == null)
        {
            newOrderedItems = columnDataModels;
        } else
        {
            newOrderedItems = keepPreviousOrder(columnDataModels, grid.getStore().getModels());
        }
        ListStore<ColumnDataModel> store = new ListStore<ColumnDataModel>();
        store.add(newOrderedItems);
        return store;
    }

    private static List<ColumnDataModel> keepPreviousOrder(
            List<ColumnDataModel> newColumnDataModels, List<ColumnDataModel> prevOrderedItems)
    {
        List<ColumnDataModel> temporaryModels = new ArrayList<ColumnDataModel>(newColumnDataModels);

        List<ColumnDataModel> newOrderedItems = new ArrayList<ColumnDataModel>();
        for (ColumnDataModel prevModel : prevOrderedItems)
        {
            int ix = findIx(temporaryModels, prevModel.getColumnID());
            if (ix != -1)
            {
                newOrderedItems.add(temporaryModels.get(ix));
                temporaryModels.remove(ix);
            }
        }
        for (ColumnDataModel newItems : temporaryModels)
        {
            newOrderedItems.add(newItems);
        }
        return newOrderedItems;
    }

    private static int findIx(List<ColumnDataModel> columnDataModels, String columnID)
    {
        for (int i = 0; i < columnDataModels.size(); i++)
        {
            if (columnDataModels.get(i).getColumnID().equals(columnID))
            {
                return i;
            }
        }
        return -1;
    }

    public Component getComponent()
    {
        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBottomComponent(new BottomToolbar());
        grid.setAutoWidth(true);
        grid.setAutoHeight(true);
        cp.add(grid);
        cp.setScrollMode(Scroll.AUTOY);
        return cp;
    }

    class BottomToolbar extends ToolBar
    {

        public BottomToolbar()
        {
            add(new LabelToolItem("Select:"));
            add(new WidgetComponent(createLink(Selectable.VISIBLE, true)));
            add(new SeparatorToolItem());
            add(new WidgetComponent(createLink(Selectable.VISIBLE, false)));
            add(new SeparatorToolItem());
            // WORKAROUND to fix problems with layout when all many filters are shown
            // add(new WidgetComponent(createLink(Selectable.FILTER, true)));
            // add(new SeparatorToolItem());
            add(new WidgetComponent(createLink(Selectable.FILTER, false)));
            add(new FillToolItem());
            Button up = new Button("Move Up");
            up.addSelectionListener(moveSelectedItem(-1));
            enableButtonOnGridSelectedItem(up,
                    "Move selected column to the left in modified table.");
            add(up);
            Button down = new Button("Move Down");
            down.addSelectionListener(moveSelectedItem(+1));
            enableButtonOnGridSelectedItem(down,
                    "Move selected column to the right in modified table.");
            add(down);
        }

        private Widget createLink(final Selectable selectable, final boolean select)
        {
            String prefix = select ? "All" : "No";
            String suffix = selectable.title + "s";
            String title = prefix + " " + suffix;
            return LinkRenderer.getLinkWidget(title, new ClickHandler()
                {

                    public void onClick(ClickEvent event)
                    {
                        for (ColumnDataModel m : grid.getStore().getModels())
                        {
                            Record r = grid.getStore().getRecord(m);
                            r.set(selectable.columnName, select);
                        }
                    }
                });
        }

    }

    private enum Selectable
    {
        VISIBLE(ColumnDataModel.IS_VISIBLE, "Column"), FILTER(ColumnDataModel.HAS_FILTER, "Filter");

        private final String columnName;

        private final String title;

        private Selectable(String columnName, String title)
        {
            this.columnName = columnName;
            this.title = title;
        }
    }

    private SelectionListener<ButtonEvent> moveSelectedItem(final int delta)
    {
        return new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
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
                        grid.getSelectionModel().select(m, false);
                    }
                }
            };
    }

    /** rebuilds columns which can be chosen */
    public void refresh()
    {
        grid.reconfigure(createStore(), grid.getColumnModel());
    }

    public List<ColumnDataModel> getModels()
    {
        return grid.getStore().getModels();
    }
}
