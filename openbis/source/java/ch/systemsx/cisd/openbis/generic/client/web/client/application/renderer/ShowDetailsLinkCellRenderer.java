package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

public class ShowDetailsLinkCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{
    private final String text;

    public ShowDetailsLinkCellRenderer(String text)
    {
        this.text = text;
    }

    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        String originalValue = String.valueOf(model.get(property));
        return LinkRenderer.renderAsLinkWithAnchor(text, originalValue, true);
    }
}
