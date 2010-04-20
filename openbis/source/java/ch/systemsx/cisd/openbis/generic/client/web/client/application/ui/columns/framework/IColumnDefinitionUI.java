package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

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

    /**
     * Returns <code>true</code> if the values of the column are numerically.
     */
    boolean isNumeric();

    /**
     * Returns <code>true</code> if the column cell should be rendered as a link.
     */
    boolean isLink();

}
