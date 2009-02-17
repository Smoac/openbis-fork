package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;

/**
 * Describes column's metadata and UI.
 * 
 * @author Tomasz Pylak
 */
public interface IColumnDefinitionUI<T> extends IColumnDefinition<T>
{
    /** width of the column */
    int getWidth();

    /** Returns <code>true</code> if this column should initially be hidden. */
    boolean isHidden();
}