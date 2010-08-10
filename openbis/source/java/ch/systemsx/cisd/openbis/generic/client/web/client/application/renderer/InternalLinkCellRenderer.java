package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;

public class InternalLinkCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{
    private final boolean renderOriginalValueForEmptyToken;

    public InternalLinkCellRenderer(boolean renderOriginalValueForEmptyToken)
    {
        this.renderOriginalValueForEmptyToken = renderOriginalValueForEmptyToken;
    }

    public InternalLinkCellRenderer()
    {
        this(false);
    }

    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        if (model.get(property) == null)
        {
            return "";
        } else
        { // TODO 2010-05-18, IA: almost the same as LinkRenderer#createLinkRenderer()
            String originalValue = String.valueOf(model.get(property));
            String tokenOrNull = model.tryGetLink(property);
            if (tokenOrNull == null && renderOriginalValueForEmptyToken)
            {
                return new MultilineHTML(originalValue).toString();
            } else
            {
                String href = "#" + (tokenOrNull != null ? tokenOrNull : "");
                return LinkRenderer.renderAsLinkWithAnchor(originalValue, href, false);
            }
        }
    }
}
