package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * ViewLocatorHandler for Search locators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final static String SEARCH_ACTION = ViewLocator.SEARCH_ACTION;

    protected static final String DEFAULT_SEARCH_STRING = "*";

    protected static final String MATCH_KEY = "searchmatch";

    protected static final String MATCH_ANY_VALUE = "any";

    protected static final String MATCH_ALL_VALUE = "all";

    protected static final SearchCriteriaConnection DEFAULT_MATCH_CONNECTION =
            SearchCriteriaConnection.MATCH_ALL;

    public SearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String searchEntityKindValueOrNull = locator.tryGetEntity();
        if (null == searchEntityKindValueOrNull)
            return;

        // Find the specific resolver based on entity type
        EntityKind entityKind = getEntityKind(searchEntityKindValueOrNull);
        if (EntityKind.SAMPLE == entityKind)
        {
            SampleSearchLocatorResolver resolver =
                    new SampleSearchLocatorResolver(viewContext, locator);
            resolver.openInitialEntitySearch();
        } else
        {
            throw new UserFailureException(
                    "URLs for searching openBIS only support SAMPLE searches. Entity " + entityKind
                            + " is not supported.");
        }

    }

    private EntityKind getEntityKind(String entityKindValueOrNull)
    {
        try
        {
            return EntityKind.valueOf(entityKindValueOrNull);
        } catch (IllegalArgumentException exception)
        {
            throw new UserFailureException("Invalid '"
                    + PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY + "' URL parameter value.");
        }
    }
}