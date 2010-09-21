package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableGridColumnDefinition<T extends IsSerializable> implements
        IColumnDefinition<TableModelRowWithObject<T>>
{
    protected TableModelColumnHeader header;

    private String title;

    public TypedTableGridColumnDefinition(TableModelColumnHeader header, String title)
    {
        this.header = header;
        this.title = title;
    }
    

    // GWT only
    @SuppressWarnings("unused")
    private TypedTableGridColumnDefinition()
    {
    }

    public String getHeader()
    {
        return title;
    }

    public String getIdentifier()
    {
        return header.getId();
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

    public String getValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        return rowModel.getOriginalObject().getValues().get(header.getIndex()).toString();
    }

    public Comparable<?> tryGetComparableValue(GridRowModel<TableModelRowWithObject<T>> rowModel)
    {
        return rowModel.getOriginalObject().getValues().get(header.getIndex());
    }
}