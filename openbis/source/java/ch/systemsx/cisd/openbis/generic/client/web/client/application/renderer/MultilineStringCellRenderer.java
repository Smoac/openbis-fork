package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;

public class MultilineStringCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{
    public String render(BaseEntityModel<?> model, String property,
            com.extjs.gxt.ui.client.widget.grid.ColumnData config, int rowIndex, int colIndex,
            ListStore<BaseEntityModel<?>> store)
    {
        String originalValue = String.valueOf(model.get(property));
        return new MultilineHTML(originalValue).toString();
    }
}
