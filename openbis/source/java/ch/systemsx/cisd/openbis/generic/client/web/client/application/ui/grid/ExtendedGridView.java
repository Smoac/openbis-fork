package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.ColumnHeader;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;

/**
 * {@link GridView} with {@link ColumnHeader} allowing to define additional behavior for header
 * click when SHIFT button is pressed at the same time. By default the width of the column is
 * adjusted.
 * 
 * @author Izabela Adamczyk
 */
class ExtendedGridView extends GridView
{
    @Override
    protected ColumnHeader newColumnHeader()
    {
        header = new ColumnHeader(grid, cm)
            {
                @Override
                protected ComponentEvent createColumnEvent(ColumnHeader pHeader, int column,
                        Menu menu)
                {
                    GridEvent<ModelData> event = new GridEvent<ModelData>(grid);
                    event.setColIndex(column);
                    event.setMenu(menu);
                    return event;
                }

                @Override
                protected Menu getContextMenu(int column)
                {
                    return createContextMenu(column);
                }

                @Override
                protected void onColumnSplitterMoved(int colIndex, int pWidth)
                {
                    super.onColumnSplitterMoved(colIndex, pWidth);
                    ExtendedGridView.this.onColumnSplitterMoved(colIndex, pWidth);
                }

                @Override
                protected void onHeaderClick(ComponentEvent ce, int column)
                {
                    super.onHeaderClick(ce, column);
                    if (ce.isShiftKey())
                    {
                        ExtendedGridView.this.onHeaderClickWithShift(grid, column);
                    } else
                    {
                        ExtendedGridView.this.onHeaderClick(grid, column);
                    }
                }

            };
        header.setSplitterWidth(splitterWidth);
        header.setMinColumnWidth(grid.getMinColumnWidth());

        return header;
    }

    protected void onHeaderClickWithShift(Grid<ModelData> pGrid, int column)
    {
        int margin = 10;
        pGrid.getColumnModel().setColumnWidth(column,
                calculateWidthWithScroll(pGrid, column) + margin);
    }

    private int calculateWidthWithScroll(Grid<ModelData> pGrid, int column)
    {
        GridView view = pGrid.getView();
        Element headerCell = (Element) view.getHeaderCell(calculateHeaderCellIndex(pGrid, column));
        headerCell = (Element) headerCell.getFirstChildElement();
        headerCell.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
        int max = headerCell.getScrollWidth();
        headerCell.getStyle().setProperty("width", "auto");
        for (int i = 0; i < pGrid.getStore().getCount(); i++)
        {
            Element cell = (Element) view.getCell(i, column).getFirstChildElement();
            cell.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
            int width = cell.getScrollWidth();
            cell.getStyle().setProperty("width", "auto");
            if (width > max)
            {
                max = width;
            }
        }
        return max;
    }

    private int calculateHeaderCellIndex(Grid<ModelData> pGrid, int column)
    {
        // WORKAROUND: getHeaderCell takes into account only visible columns
        int headerCellIndex = column;
        for (int i = 0; i < column; i++)
        {
            if (pGrid.getColumnModel().getColumn(i).isHidden())
            {
                headerCellIndex--;
            }
        }
        return headerCellIndex;
    }

}