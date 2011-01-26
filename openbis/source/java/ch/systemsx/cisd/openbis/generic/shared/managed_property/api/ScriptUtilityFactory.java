package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.SimpleTableModelBuilderAdaptor;

/**
 * This utility class with function to be used by jython scripts for managed properties.
 * <p>
 * All methods of this class are part of the Managed Properties API.
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
}