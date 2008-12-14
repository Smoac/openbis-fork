package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypePropertyType;

/**
 * Renderer of {@link EntityTypePropertyType} value.
 * 
 * @author Izabela Adamczyk 
 */
public final class ETPTRenderer implements GridCellRenderer<ModelData>
{
    public String render(ModelData model, String property, ColumnData config, int rowIndex,
            int colIndex, ListStore<ModelData> store)
    {
        Object value = model.get(property);
        if (value == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<EntityTypePropertyType<?>> list = cast(value);
        for (EntityTypePropertyType<?> etpt : list)
        {
            sb.append(render(etpt));
        }
        return sb.toString();
    }

    private String render(EntityTypePropertyType<?> etpt)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(etpt.getEntityType().getCode());
        if (etpt.isMandatory())
        {
            sb.append(" *");
        }

        final Element div = DOM.createDiv();
        div.setInnerText(sb.toString());
        return div.getString();
    }

    @SuppressWarnings("unchecked")
    private List<EntityTypePropertyType<?>> cast(Object value)
    {
        return (List<EntityTypePropertyType<?>>) value;
    }
}