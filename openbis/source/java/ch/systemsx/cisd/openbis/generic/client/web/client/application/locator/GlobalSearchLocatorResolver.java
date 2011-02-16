package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GlobalSearchTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * {@link IViewLocatorResolver} to handle global search.
 * 
 * @author Kaloyan Enimanev
 */
public class GlobalSearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String GLOBAL_SEARCH_ACTION = "GLOBAL_SEARCH";

    public final static String ENTITY_PARAMETER_KEY = "type";

    public final static String QUERY_PARAMETER_KEY = "query";


    public GlobalSearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(GLOBAL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }


    public void resolve(ViewLocator locator) throws UserFailureException
    {
        final SearchableEntity selectedSearchableEntity = getSearchableEntity(locator);
        // TODO KE: 2011-02-16 we should parse queries that can contain spaces
        final String queryText = getMandatoryParameter(locator, QUERY_PARAMETER_KEY);

        AbstractTabItemFactory tabItemFactory =
                GlobalSearchTabItemFactory.create(viewContext, selectedSearchableEntity, queryText);

        DispatcherHelper.dispatchNaviEvent(tabItemFactory);
    }


    private SearchableEntity getSearchableEntity(ViewLocator locator)
    {
        SearchableEntity result = null;
        String entity = getOptionalParameter(locator, ENTITY_PARAMETER_KEY);
        if (entity != null)
        {
            result = new SearchableEntity();
            result.setName(entity);
            return result;
        }
        return null;
    }
}