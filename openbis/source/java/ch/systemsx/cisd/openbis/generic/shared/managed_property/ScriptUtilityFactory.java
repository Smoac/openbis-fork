package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.ElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.XmlStructuredPropertyConverter;

/**
 * This utility class with function to be used by jython scripts for managed properties.
 * <p>
 * All public methods of this class are part of the Managed Properties API.
 */
public class ScriptUtilityFactory
{
    /**
     * Creates a table builder.
     */
    public static ISimpleTableModelBuilderAdaptor createTableBuilder()
    {
        return SimpleTableModelBuilderAdaptor.create();
    }
    
    /**
     * Creates a {@link ValidationException} with specified message. 
     */
    // Violates Java naming conventions for method because it should look like a constructor
    // for invocations in jython. 
    public static ValidationException ValidationException(String message)
    {
        return new ValidationException(message);
    }

    public static IElementFactory createElementFactory()
    {
        return new ElementFactory();
    }

    public static IStructuredPropertyConverter createPropertyConverter()
    {
        return new XmlStructuredPropertyConverter(createElementFactory());
    }
}