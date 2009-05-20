package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;

public class ShowDetailsLinkCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{
    private String text;

    public ShowDetailsLinkCellRenderer(String text)
    {
        this.text = text;
    }

    public String render(BaseEntityModel<?> model, String property,
            com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
            ListStore<BaseEntityModel<?>> store)
    {
        String originalValue = String.valueOf(model.get(property));
        return LinkRenderer.renderAsLinkWithAnchor(text, originalValue, true);
    }
}