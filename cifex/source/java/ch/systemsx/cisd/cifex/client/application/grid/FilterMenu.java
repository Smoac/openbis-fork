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
 * Button with menu allowing to switch on/off filters. Menu items and filters availability depends
 * on visibility of corresponding columns.
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
        for (final AbstractFilterField<M> ff : filterFields)
        {
            final CheckMenuItem menuItem = createMenuItem(ff);
            updateFilterAndMenu(ff, menuItem, columnModel.isHidden(columnModel.getIndexById(ff
                    .getProperty())) == false);
            bindColumnsAndFilters(columnModel, ff, menuItem);
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
        menuItem.setEnabled(columnVisible);
    }

    private static <M extends ModelData> void bindColumnsAndFilters(ColumnModel columnModel,
            final AbstractFilterField<M> ff, final CheckMenuItem menuItem)
    {
        columnModel.addListener(Events.HiddenChange, new Listener<ColumnModelEvent>()
            {
                public void handleEvent(ColumnModelEvent be)
                {
                    ColumnConfig column = be.getColumnModel().getColumn(be.getColIndex());
                    if (ff.getProperty().equals(column.getId()))
                    {
                        updateFilterAndMenu(ff, menuItem, column.isHidden() == false);
                    }
                }

            });
    }

    private static <M extends ModelData> CheckMenuItem createMenuItem(
            final AbstractFilterField<M> ff)
    {
        final CheckMenuItem menuItem = new CheckMenuItem(ff.getEmptyText());
        menuItem.setChecked(ff.isEnabled(), false);
        menuItem.setHideOnClick(false);
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    showFilter(ff, menuItem.isChecked());
                }
            });
        return menuItem;
    }

    private static <M extends ModelData> void showFilter(final AbstractFilterField<M> ff,
            boolean show)
    {
        if (show == false)
        {
            ff.clear();
        }
        ff.setEnabled(show);
        ff.setVisible(show);
    }
}