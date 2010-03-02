package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Default implementation of the IViewLocatorHandler interface. Designed to be subclassed.
 * <p>
 * The default implementation is bound to one particular action. The method
 * {@link #canHandleLocator(ViewLocator)} returns true if the locator's action matches my
 * handledAction.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public abstract class AbstractViewLocatorResolver implements IViewLocatorResolver
{
    private final String handledAction;

    public AbstractViewLocatorResolver(String handledAction)
    {
        assert handledAction != null;
        this.handledAction = handledAction;
    }

    public boolean canHandleLocator(ViewLocator locator)
    {
        return handledAction.equals(locator.tryGetAction());
    }

    /**
     * Utility method that throws an exception with a standard error message if the required
     * paramter is not specified
     */
    protected void checkRequiredParameter(String valueOrNull, String parameter)
            throws UserFailureException
    {
        if (valueOrNull == null)
        {
            throw new UserFailureException("Missing URL parameter: " + parameter);
        }
    }
}