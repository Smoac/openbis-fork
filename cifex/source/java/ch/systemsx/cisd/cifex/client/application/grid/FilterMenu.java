package ch.systemsx.cisd.cifex.client.application.grid;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;

/**
 * Button with menu allowing to switch on/off filters.Filters are switched off if column is hidden,
 * column is made visible if filter is switched on:
 * <ul>
 * <li>filter: on => column: on
 * <li>filter: off => column: no change
 * <li>column: on => filter: no change
 * <li>column: off => filter: off
 * </ul>
 * 
 * @author Izabela Adamczyk
 */
class FilterMenu extends SplitButton
{
    /**
     * Creates filter menu, updates filters visibility and connects menu with columns.
     */
    public <M extends ModelData> FilterMenu(List<AbstractFilterField<M>> filterFields,
            String label, ColumnModel columnModel)
    {
        super(label);
        Menu filterMenu = new Menu();
        for (final AbstractFilterField<M> filter : filterFields)
        {
            final CheckMenuItem menuItem = createMenuItem(filter);
            updateFilterAndMenu(filter, menuItem, columnModel.isHidden(columnModel
                    .getIndexById(filter.getProperty())) == false);
            bindColumnsAndFilters(columnModel, filter, menuItem);
            filterMenu.add(menuItem);
        }
        setMenu(filterMenu);
    }

    private static <M extends ModelData> void updateFilterAndMenu(
            final AbstractFilterField<M> filter, final CheckMenuItem menuItem, boolean columnVisible)
    {
        if (columnVisible == false) // hide filter and disable menu option if column not visible
        {
            menuItem.setChecked(columnVisible);
            showFilter(filter, columnVisible);
        }
    }

    private static <M extends ModelData> void bindColumnsAndFilters(final ColumnModel columnModel,
            final AbstractFilterField<M> filter, final CheckMenuItem menuItem)
    {
        columnModel.addListener(Events.HiddenChange, new Listener<ColumnModelEvent>()
            {
                public void handleEvent(ColumnModelEvent be)
                {
                    ColumnConfig column = be.getColumnModel().getColumn(be.getColIndex());
                    if (filter.getProperty().equals(column.getId()))
                    {
                        updateFilterAndMenu(filter, menuItem, column.isHidden() == false);
                    }
                }
            });
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    if (menuItem.isChecked())
                    {
                        int index = columnModel.getIndexById(filter.getProperty());
                        columnModel.setHidden(index, false);
                    }
                }
            });
    }

    private static <M extends ModelData> CheckMenuItem createMenuItem(
            final AbstractFilterField<M> filter)
    {
        final CheckMenuItem menuItem = new CheckMenuItem(filter.getEmptyText());
        menuItem.setChecked(filter.isEnabled(), false);
        menuItem.setHideOnClick(false);
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    showFilter(filter, menuItem.isChecked());
                }
            });
        return menuItem;
    }

    private static <M extends ModelData> void showFilter(final AbstractFilterField<M> filter,
            boolean show)
    {
        if (show == false)
        {
            filter.clear();
        }
        filter.setEnabled(show);
        filter.setVisible(show);
    }
}