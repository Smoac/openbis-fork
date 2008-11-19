package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Comparator;

/**
 * An {@link Comparator} extension which bases its sorting on a specified class field.
 * 
 * @author Christian Ribeaud
 */
interface IFieldComparator<T> extends Comparator<T>
{
    /**
     * Set the field name which determines the sorting.
     * <p>
     * Must be specified before comparison occurs.
     * </p>
     */
    void setFieldName(final String fieldName);
}