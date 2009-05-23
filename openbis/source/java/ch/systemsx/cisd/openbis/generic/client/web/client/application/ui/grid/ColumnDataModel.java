package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Represents a column of the table.
 * 
 * @author Izabela Adamczyk
 */
class ColumnDataModel extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    private static final String COLUMN_ID = "column_id";

    static final String HAS_FILTER = "HAS_FILTER";

    static final String IS_VISIBLE = "IS_VISIBLE";

    static final String HEADER = "header";

    public ColumnDataModel(String header, boolean isVisible, boolean hasFilter, String columnID)
    {
        setHeader(header);
        setIsVisible(isVisible);
        setHasFilter(hasFilter);
        setColumnID(columnID);
    }

    private void setColumnID(String columnID)
    {
        set(COLUMN_ID, columnID);
    }

    private void setIsVisible(boolean isVisible)
    {
        set(IS_VISIBLE, isVisible);
    }

    private void setHasFilter(boolean hasFilter)
    {
        set(HAS_FILTER, hasFilter);
    }

    private void setHeader(String header)
    {
        set(HEADER, header);
    }

    public String getColumnID()
    {
        return get(COLUMN_ID);
    }

    public boolean isVisible()
    {
        return get(IS_VISIBLE);
    }

    public boolean hasFilter()
    {
        return get(HAS_FILTER);
    }
}