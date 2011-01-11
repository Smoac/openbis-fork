package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A utility class that can be used to simplify assertions in {@link TypedTableGrid}-related
 * headless system tests.
 * 
 * @author Kaloyan Enimanev
 */
class TypedTableColumnAssertions<T extends ISerializable> extends AssertJUnit
{
    /**
     * a convenience method, that can be used as a static import to achieve a fluent-API like
     * programming style.
     */
    static <S extends ISerializable> TypedTableColumnAssertions<S> assertColumn(
            TypedTableResultSet<S> tableResultSet,
            String columnName)
    {
        return new TypedTableColumnAssertions<S>(tableResultSet, columnName);
    }

    private TypedTableResultSet<T> tableResultSet;

    private TableModelColumnHeader header = null;

    private TypedTableColumnAssertions(TypedTableResultSet<T> tableResultSet, String columnId)
    {
        this.tableResultSet = tableResultSet;
        initTableHeader(columnId);
    }

    /**
     * prepares the column header for subsequent assertions.
     */
    private void initTableHeader(String columnId)
    {
        GridRowModels<TableModelRowWithObject<T>> list =
                tableResultSet.getResultSet().getList();
        List<String> headerIds = new ArrayList<String>();

        for (TableModelColumnHeader tmch : list.getColumnHeaders())
        {
            String headerId = tmch.getId();
            headerIds.add(headerId);
            if (headerId.equalsIgnoreCase(columnId))
            {
                header = tmch;
                return;
            }
        }

        String errMessage =
                String.format(
                        "Table header with id '%s' was not discovered in the headers list: %s",
                        columnId, headerIds.toString());
        assertNotNull(errMessage, header);
    }

    /**
     * just a convenience method redirecting to the {@link #containsValue(String)} with a single
     * parameter.
     */
    public void containsValues(String... values)
    {
        for (String value : values)
        {
            containsValue(value);
        }
    }

    /**
     * asserts that the column contains a specified value.
     * 
     * @param value a value to be searched for
     */
    public void containsValue(String value)
    {
        GridRowModels<TableModelRowWithObject<T>> list =
            tableResultSet.getResultSet().getList();
        List<String> presentValues = new ArrayList<String>();
        for (int i = 0; i < tableResultSet.getResultSet().getTotalLength(); i++)
        {
            List<ISerializableComparable> rowValues =
                    list.get(i).getOriginalObject().getValues();
            String cellValue = rowValues.get(header.getIndex()).toString();
            presentValues.add(cellValue);
            if (value.equals(cellValue))
            {
                // the expected value has been found
                return;
            }
        }
        String errMessage =
                String.format(
                        "The value '%s' was not discovered in column %s. Its contents were : %s",
                        value, header.getId(), presentValues.toString());
        fail(errMessage);

    }
}