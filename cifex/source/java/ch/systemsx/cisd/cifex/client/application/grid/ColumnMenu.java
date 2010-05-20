package ch.systemsx.cisd.cifex.client.application.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;

/**
 * Button with menu allowing to switch on/off columns.
 * 
 * @author Izabela Adamczyk
 */
class ColumnMenu extends SplitButton
{
    /**
     * Creates column menu.
     */
    public <M extends ModelData> ColumnMenu(String label, final ColumnModel columnModel)
    {
        super(label);
        final Menu columnMenu = new Menu();
        for (int colIndex = 0; colIndex < columnModel.getColumnCount(); colIndex++)
        {
            if (columnModel.isFixed(colIndex) == false)
            {
                final CheckMenuItem menuItem =
                        new CheckMenuItem(columnModel.getColumnHeader(colIndex));
                menuItem.setHideOnClick(false);
                menuItem.setChecked(columnModel.isHidden(colIndex) == false);
                bindMenuItemWithColumn(columnModel, menuItem, colIndex, columnMenu);
                columnMenu.add(menuItem);
            }
        }
        setMenu(columnMenu);
    }

    private static void bindMenuItemWithColumn(final ColumnModel columnModel,
            final CheckMenuItem menuItem, final int colIndex, final Menu columnMenu)
    {
        columnModel.addListener(Events.HiddenChange, new Listener<ColumnModelEvent>()
            {
                public void handleEvent(ColumnModelEvent be)
                {
                    if (be.getColIndex() == colIndex)
                    {
                        menuItem.setChecked(columnModel.isHidden(colIndex) == false, false);
                    }
                }
            });
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>()
            {
                @Override
                public void componentSelected(MenuEvent ce)
                {
                    columnModel.setHidden(colIndex, menuItem.isChecked() == false);
                    restrictMenu(columnMenu, columnModel);
                }
            });
    }

    private static void restrictMenu(Menu columns, ColumnModel columnModel)
    {
        int count = 0;
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            if (columnModel.isHidden(i) == false && columnModel.isFixed(i) == false)
            {
                count++;
            }
        }
        if (count == 1)
        {
            for (Component item : columns.getItems())
            {
                CheckMenuItem checkItem = (CheckMenuItem) item;
                if (checkItem.isChecked())
                {
                    checkItem.disable();
                }
            }
        } else
        {
            for (Component item : columns.getItems())
            {
                item.enable();
            }
        }
    }

}