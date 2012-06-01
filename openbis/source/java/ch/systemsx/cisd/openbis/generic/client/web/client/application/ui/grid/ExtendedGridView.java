package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.ColumnHeader;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;

/**
 * {@link GridView} with {@link ColumnHeader} allowing to define additional behavior for header
 * click when SHIFT button is pressed at the same time. By default the width of the column is
 * adjusted.
 * 
 * @author Izabela Adamczyk
 */
public class ExtendedGridView extends GridView
{
    @Override
    protected ColumnHeader newColumnHeader()
    {
        header = new ColumnHeader(grid, cm)
            {
                @Override
                protected ComponentEvent createColumnEvent(ColumnHeader pHeader, int column,
                        Menu componentMenu)
                {
                    GridEvent<ModelData> event = new GridEvent<ModelData>(grid);
                    event.setColIndex(column);
                    event.setMenu(componentMenu);
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
        int newWidth = calculateWidthWithScroll(pGrid, column) + margin;
        cm.setColumnWidth(column, newWidth);
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
            Element td = (Element) view.getCell(i, column);
            List<Node> nodes = extractNodes(td);
            for (Node n : nodes)
            {
                if (com.google.gwt.dom.client.Element.is(n))
                {
                    com.google.gwt.dom.client.Element e = com.google.gwt.dom.client.Element.as(n);
                    if (e.getTagName().equalsIgnoreCase("img") == false)
                    {
                        e.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
                    }
                }
            }
            Element element = (Element) td.getFirstChildElement();
            element.getStyle().setWidth(0, com.google.gwt.dom.client.Style.Unit.PX);
            int width = element.getScrollWidth();
            element.getStyle().setProperty("width", "auto");
            if (width > max)
            {
                max = width;
            }
        }
        return max;
    }

    private List<Node> extractNodes(Element element)
    {
        List<Node> visited = new ArrayList<Node>();
        List<Node> toVisit = new ArrayList<Node>();
        toVisit.add(element);
        while (toVisit.isEmpty() == false)
        {
            Node n = toVisit.get(0);
            toVisit.remove(n);
            if (visited.contains(n) == false)
            {
                visited.add(n);
                NodeList<Node> childNodes = n.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++)
                {
                    Node c = childNodes.getItem(j);
                    if (toVisit.contains(c) == false)
                    {
                        toVisit.add(c);
                    }
                }
            }
        }
        return visited;
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