package ch.systemsx.cisd.common.parser;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A simple <code>IPropertyModel</code> implementation that acts like a bean.
 * <p>
 * <code>column</code> and <code>name</code> are mandatory and are set in the constructor.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class MappedProperty implements IPropertyModel
{

    private final int column;

    private final String name;

    private String format;

    private boolean mandatory;

    MappedProperty(final int column, final String name)
    {
        this.column = column;
        this.name = name;
    }

    public final void setFormat(String format)
    {
        this.format = format;
    }

    /** Sets <code>mandatory</code>. */
    public final void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }
    
    //
    // IPropertyModel
    //

    public final String getFormat()
    {
        return format;
    }

    public final int getColumn()
    {
        return column;
    }

    public final String getName()
    {
        return name;
    }

    /** Returns <code>mandatory</code>. */
    public final boolean isMandatory()
    {
        return mandatory;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}