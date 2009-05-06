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

    static final String CHECKED = "hidden";

    static final String HEADER = "header";

    public ColumnDataModel(String header, boolean isChecked, String columnID)
    {
        setHeader(header);
        setChecked(isChecked);
        setColumnID(columnID);
    }

    public void setColumnID(String columnID)
    {
        set(COLUMN_ID, columnID);
    }

    public void setChecked(boolean isChecked)
    {
        set(CHECKED, isChecked);
    }

    public void setHeader(String header)
    {
        set(HEADER, header);
    }

    public String getColumnID()
    {
        return get(COLUMN_ID);
    }

    public String getHeader()
    {
        return get(HEADER);
    }

    public boolean isChecked()
    {
        return get(CHECKED);
    }

}