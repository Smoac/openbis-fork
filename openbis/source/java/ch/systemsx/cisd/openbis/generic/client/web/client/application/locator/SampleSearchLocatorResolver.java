/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.ArrayList;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

class SampleSearchLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final ViewLocator locator;

    SampleSearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext,
            ViewLocator locator)
    {
        this.viewContext = viewContext;
        this.locator = locator;
    }

    void openInitialEntitySearch() throws UserFailureException
    {
        openEntitySearch();
    }

    private void openEntitySearch()
    {
        ListSampleDisplayCriteria displayCriteria = getListSampleDisplayCriteria();

        viewContext.getCommonService().listSamples(displayCriteria,
                new OpenEntitySearchTabCallback(displayCriteria));
    }

    /**
     * Convert the locator parameters into a ListSampleDisplayCriteria -- a LSDC is used to pass the
     * search parameters to the server.
     */
    private ListSampleDisplayCriteria getListSampleDisplayCriteria()
    {
        // Loop over the parameters and create a detailed search criteria for each parameter
        // -- a parameter key could refer to an attribute (valid options known at compile time)
        // -- or a property (valid options must be retrieved from server)
        Map<String, String> parameters = locator.getParameters();
        ArrayList<DetailedSearchCriterion> criterionList = new ArrayList<DetailedSearchCriterion>();

        DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
        // Default to match all
        searchCriteria.setConnection(SearchLocatorResolver.DEFAULT_MATCH_CONNECTION);

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
            } else if (key.equals("gwt.codesvr"))
            {
                // ignore this gwt keyword
            } else
            {
                DetailedSearchCriterion searchCriterion = getSearchCriterionForKeyValue(key, value);
                criterionList.add(searchCriterion);
            }
        }

        // Default the search criteria if none is provided
        if (criterionList.isEmpty())
        {
            DetailedSearchCriterion searchCriterion =
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(SampleAttributeSearchFieldKind.CODE),
                            SearchLocatorResolver.DEFAULT_SEARCH_STRING);
            criterionList.add(searchCriterion);
        }

        searchCriteria.setCriteria(criterionList);

        // Create a display criteria object for the search string
        ListSampleDisplayCriteria displayCriteria = ListSampleDisplayCriteria.createForSearch();
        displayCriteria.updateSearchCriteria(searchCriteria);
        return displayCriteria;
    }

    /**
     * Convert the key/value to a search criterion. The kind of field depends on whether the key
     * refers to an attribute or property.
     */
    private DetailedSearchCriterion getSearchCriterionForKeyValue(String key, String value)
    {
        DetailedSearchField field;

        try
        {
            SampleAttributeSearchFieldKind attributeKind =
                    SampleAttributeSearchFieldKind.valueOf(key.toUpperCase());
            field = DetailedSearchField.createAttributeField(attributeKind);
        } catch (IllegalArgumentException ex)
        {
            // this is not an attribute
            field = DetailedSearchField.createPropertyField(key.toUpperCase());
        }
        return new DetailedSearchCriterion(field, value);
    }

    private class OpenEntitySearchTabCallback implements
            AsyncCallback<ResultSetWithEntityTypes<Sample>>
    {
        private final ListSampleDisplayCriteria displayCriteria;

        private OpenEntitySearchTabCallback(ListSampleDisplayCriteria displayCriteria)
        {
            this.displayCriteria = displayCriteria;
        }

        public final void onFailure(Throwable caught)
        {
            // Error in the search -- notify the user
            MessageBox.alert("Error", caught.getMessage(), null);
        }

        public final void onSuccess(ResultSetWithEntityTypes<Sample> result)
        {
            switch (result.getResultSet().getTotalLength())
            {
                // Nothing found -- notify the user
                case 0:
                    MessageBox.alert("Error", "No samples matching criteria ["
                            + displayCriteria.getSearchCriteria().toString() + "] were found.",
                            null);
                    break;
                // One result found -- show it in the details view
                case 1:
                    Sample sample = result.getResultSet().getList().get(0).getOriginalObject();
                    OpenEntityDetailsTabAction detailsAction =
                            new OpenEntityDetailsTabAction(sample, viewContext);
                    detailsAction.execute();
                    break;

                // Multiple results found -- show them in a grid
                default:
                    OpenEntitySearchGridTabAction searchAction =
                            new OpenEntitySearchGridTabAction(displayCriteria, viewContext);

                    DispatcherHelper.dispatchNaviEvent(searchAction);

                    break;
            }
        }
    }

    private static class OpenEntitySearchGridTabAction implements ITabItemFactory
    {
        private final ListSampleDisplayCriteria displayCriteria;

        private final IViewContext<ICommonClientServiceAsync> viewContext;

        private OpenEntitySearchGridTabAction(ListSampleDisplayCriteria displayCriteria,
                IViewContext<ICommonClientServiceAsync> viewContext)
        {
            this.displayCriteria = displayCriteria;
            this.viewContext = viewContext;
        }

        private String getMessage(String key)
        {
            return viewContext.getMessage(key);
        }

        private ITabItem createTab(String dictionaryMsgKey, IDisposableComponent component)
        {
            String title = getMessage(dictionaryMsgKey);
            return DefaultTabItem.create(title, component, viewContext);
        }

        public ITabItem create()
        {
            // CRDEBUG
            System.err.println(displayCriteria.getSearchCriteria().toString());
            IDisposableComponent browser =
                    SampleSearchHitGrid.createWithInitialDisplayCriteria(viewContext,
                            displayCriteria);
            return createTab(Dict.SAMPLE_SEARCH, browser);
        }

        public String getId()
        {
            return SampleSearchHitGrid.SEARCH_BROWSER_ID;
        }

        public HelpPageIdentifier getHelpPageIdentifier()
        {
            return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.SEARCH);
        }
    }
}