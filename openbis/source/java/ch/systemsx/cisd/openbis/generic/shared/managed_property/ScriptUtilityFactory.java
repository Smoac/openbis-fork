package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElement;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IStructuredPropertyConverter;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.ElementFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.XmlStructuredPropertyConverter;

/**
 * This is a utility class with functions to be used by Jython scripts for managed properties.
 * <p>
 * All public methods of this class are part of the Managed Properties API.
 */
@Component(value = ResourceNames.MANAGED_PROPERTY_SCRIPT_UTILITY_FACTORY)
public class ScriptUtilityFactory
{
    private static final IElementFactory ELEMENT_FACTORY_INSTANCE = new ElementFactory();

    private static final IStructuredPropertyConverter STRUCTURED_PROPERTY_CONVERTER_INSTANCE =
            new XmlStructuredPropertyConverter(ELEMENT_FACTORY_INSTANCE);

    // initialized by spring
    private static IEntityInformationProvider entityInformationProvider;

    public IEntityInformationProvider getEntityInformationProvider()
    {
        return entityInformationProvider;
    }

    // @Autowired
    @Resource(name = ResourceNames.ENTITY_INFORMATION_PROVIDER)
    public void setEntityInformationProvider(IEntityInformationProvider entityInformationProvider)
    {
        ScriptUtilityFactory.entityInformationProvider = entityInformationProvider;
    }

    private ScriptUtilityFactory()
    {

    }

    /**
     * Creates a table builder.
     */
    public static ISimpleTableModelBuilderAdaptor createTableBuilder()
    {
        return SimpleTableModelBuilderAdaptor.create(entityInformationProvider);
    }

    /**
     * Creates a {@link ValidationException} with specified message.
     */
    // NOTE: Violates Java naming conventions for method because it should look like a constructor
    // for invocations in jython.
    public static ValidationException ValidationException(String message)
    {
        return new ValidationException(message);
    }

    /**
     * @return a factory object that can be used to create {@link IElement}-s.
     */
    public static IElementFactory getElementFactory()
    {
        return ELEMENT_FACTORY_INSTANCE;
    }

    /**
     * @return a converter that can translate {@link IElement} to/from Strings.
     */
    public static IStructuredPropertyConverter getPropertyConverter()
    {
        return STRUCTURED_PROPERTY_CONVERTER_INSTANCE;
    }

}