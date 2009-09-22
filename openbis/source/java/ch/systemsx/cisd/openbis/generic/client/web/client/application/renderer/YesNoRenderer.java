package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;

/**
 * Renderer of {@link Boolean} value. Render <code>true</code> to <code>yes</code> and
 * <code>false</code> to <code>no</code>.
 * 
 * @author    Franz-Josef Elmer
 */
public final class YesNoRenderer implements GridCellRenderer<ModelData>
{
    public String render(ModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<ModelData> store)
    {
        Object value = model.get(property);
        if (value == null)
        {
            return "";
        }
        if (value instanceof Boolean == false)
        {
            return value.toString();
        }
        Boolean b = (Boolean) value;
        return SimpleYesNoRenderer.render(b.booleanValue());
    }
}
