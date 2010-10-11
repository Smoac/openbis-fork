package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.ArrayList;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * ViewLocatorHandler for Search locators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchLocatorResolver extends AbstractViewLocatorResolver
{
    public static final String SEARCH_ACTION = SearchlinkUtilities.SEARCH_ACTION;

    protected static final String DEFAULT_SEARCH_STRING = "*";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    protected static final String MATCH_KEY = "searchmatch";

    protected static final String MATCH_ANY_VALUE = "any";

    protected static final String MATCH_ALL_VALUE = "all";

    protected static final SearchCriteriaConnection DEFAULT_MATCH_CONNECTION =
            SearchCriteriaConnection.MATCH_ALL;

    protected static final boolean DEFAULT_USE_WILDCARDS = false;

    public SearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        // Extract the search criteria from the ViewLocator and dispatch to a resolver that can
        // handle the entity type.
        EntityKind entityKind = getEntityKind(locator);

        DetailedSearchCriteria searchCriteria =
                new ViewLocatorToDetailedSearchCriteriaConverter(locator, entityKind)
                        .getDetailedSearchCriteria();
        if (EntityKind.SAMPLE == entityKind)
        {
            SampleSearchLocatorResolver resolver = new SampleSearchLocatorResolver(viewContext);
            resolver.openEntitySearch(searchCriteria, locator.getHistoryToken());
        } else if (EntityKind.DATA_SET == entityKind)
        {
            DataSetSearchLocatorResolver resolver = new DataSetSearchLocatorResolver(viewContext);
            resolver.openEntitySearch(searchCriteria, locator.getHistoryToken());
        } else
        {
            throw new UserFailureException(
                    "URLs for searching openBIS only support SAMPLE and DATA_SET searches. Entity "
                            + entityKind + " is not supported.");
        }
    }

    protected static class ViewLocatorToDetailedSearchCriteriaConverter
    {
        private final ViewLocator locator;

        private final EntityKind entityKind;

        protected ViewLocatorToDetailedSearchCriteriaConverter(ViewLocator locator,
                EntityKind entityKind)
        {
            this.locator = locator;
            this.entityKind = entityKind;
        }

        protected DetailedSearchCriteria getDetailedSearchCriteria()
        {
            // Loop over the parameters and create a detailed search criteria for each parameter
            // -- a parameter key could refer to an attribute (valid options known at compile time)
            // -- or a property (valid options must be retrieved from server)
            Map<String, String> parameters = locator.getParameters();
            ArrayList<DetailedSearchCriterion> criterionList =
                    new ArrayList<DetailedSearchCriterion>();

            DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
            // Default to match all
            searchCriteria.setConnection(SearchLocatorResolver.DEFAULT_MATCH_CONNECTION);

            // Default to use wildcards
            searchCriteria.setUseWildcardSearchMode(SearchLocatorResolver.DEFAULT_USE_WILDCARDS);

            for (String key : parameters.keySet())
            {
                String value = parameters.get(key);
                // The match key is handled separately
                if (key.equals(SearchLocatorResolver.MATCH_KEY))
                {
                    if (value.equalsIgnoreCase(SearchLocatorResolver.MATCH_ANY_VALUE))
                    {
                        searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ANY);
                    }
                } else
                {
                    DetailedSearchCriterion searchCriterion =
                            getSearchCriterionForKeyValueAndEntityKind(key, value);
                    criterionList.add(searchCriterion);
                }
            }

            // Default the search criteria if none is provided
            if (criterionList.isEmpty())
            {
                DetailedSearchCriterion searchCriterion =
                        new DetailedSearchCriterion(
                                DetailedSearchField.createAttributeField(AttributeSearchFieldKindProvider
                                        .getAttributeFieldKind(entityKind, "CODE")),
                                SearchLocatorResolver.DEFAULT_SEARCH_STRING);
                criterionList.add(searchCriterion);
            }

            searchCriteria.setCriteria(criterionList);
            return searchCriteria;
        }

        /**
         * Convert the key/value to a search criterion. The kind of field depends on whether the key
         * refers to an attribute or property.
         */
        protected DetailedSearchCriterion getSearchCriterionForKeyValueAndEntityKind(String key,
                String value)
        {
            DetailedSearchField field;

            try
            {
                IAttributeSearchFieldKind searchFieldKind =
                        AttributeSearchFieldKindProvider.getAttributeFieldKind(entityKind,
                                key.toUpperCase());
                field = DetailedSearchField.createAttributeField(searchFieldKind);
            } catch (IllegalArgumentException ex)
            {
                // this is not an attribute
                field = DetailedSearchField.createPropertyField(key.toUpperCase());
            }
            return new DetailedSearchCriterion(field, value);
        }
    }
}