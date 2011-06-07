package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;

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
    
    /**
     * Returns <code>true</code> if the column cell is editable.
     */
    boolean isEditable();

    /**
     * Creates a field to be used for {@link CellEditor}. This method will only be invoked if
     * {@link #isEditable()} returns <code>true</code>.
     */
    Field<? extends Object> createEditorField();

    String tryGetLink(T entity);
}
