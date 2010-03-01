/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * A class with helper methods for URL parameters handling and opening initial tab.
 * 
 * @author Piotr Buczek
 */
public final class UrlParamsHelper
{

    private static final String DEFAULT_SEARCH_STRING = "*";

    // viewLocator is initialized by a method called from the constructor
    private ViewLocator viewLocator;

    private IViewContext<?> viewContext;

    public UrlParamsHelper(IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
    }

    private final String tryGetUrlParamValue(String paramKey)
    {
        // Handle Action and Entity specially
        if (ViewLocator.ACTION_PARAMETER.equalsIgnoreCase(paramKey))
        {
            return viewLocator.tryGetAction();
        }
        if (ViewLocator.ENTITY_PARAMETER.equalsIgnoreCase(paramKey))
        {
            return viewLocator.tryGetEntity();
        }

        // Otherwise, get look in the paramters for the value
        return viewLocator.getParameters().get(paramKey);
    }

    public final void initUrlParams()
    {
        final String paramString = GWTUtils.getParamString();
        if (StringUtils.isBlank(paramString) == false)
        {
            initializeUrlParameters(paramString);
        }
    }

    /**
     * Parse the parameter string and store the result.
     * 
     * @param nonEmptyParameterString A non-empty URL parameter string
     */
    private final void initializeUrlParameters(String nonEmptyParameterString)
    {
        // setUrlParams(parseParamString(nonEmptyParameterString));
        viewLocator = new ViewLocator(nonEmptyParameterString);
    }

    /**
     * A public version of initializeUrlParamters used by a test case
     */
    public final void initializeUrlParametersForTest(String nonEmptyParameterString)
    {
        initializeUrlParameters(nonEmptyParameterString);
    }

    public final IDelegatedAction getOpenInitialTabAction()
    {
        return new OpenInitialTabAction();
    }

    /**
     * An action that opens the initial tab specified by the URL parameters. This class is given
     * public visibility so it can be used by a test.
     */
    public class OpenInitialTabAction implements IDelegatedAction
    {

        public void execute()
        {
            openInitialTab();
        }

        /**
         * Opens the initial tab and handles any user failure exceptions that may result in the
         * process.
         */
        private void openInitialTab()
        {
            try
            {
                openInitialTabUnderExceptionHandler();
            } catch (UserFailureException exception)
            {
                MessageBox.alert("Error", exception.getMessage(), null);
            }
        }

        /**
         * Opens an initial tab if a parameter is specified in URL.
         */
        private void openInitialTabUnderExceptionHandler() throws UserFailureException
        {
            // If a permlink has been specified, open a viewer on the specified object
            String entityKindValueOrNull =
                    tryGetUrlParamValue(PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
            String permIdValueOrNull = tryGetUrlParamValue(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
            if (null != entityKindValueOrNull || null != permIdValueOrNull)
            {
                // Make sure the permlink has been specified correctly, if not throw an error
                checkMissingURLParameter(entityKindValueOrNull,
                        PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
                checkMissingURLParameter(permIdValueOrNull, PermlinkUtilities.PERM_ID_PARAMETER_KEY);
                openInitialEntityViewer(entityKindValueOrNull, permIdValueOrNull);
            }

            // If a searchlink has been specified, open a search on the specified object
            String searchEntityKindValueOrNull =
                    tryGetUrlParamValue(SearchlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
            if (null != searchEntityKindValueOrNull)
            {
                openInitialEntitySearch(searchEntityKindValueOrNull);
            }
        }

        /**
         * A public version of openInitialTabUnderExceptionHandler() used by the test case.
         */
        public void openInitialTabUnderExceptionHandlerForTest() throws UserFailureException
        {
            openInitialTabUnderExceptionHandler();
        }

        /**
         * Open the entity details tab for the specified entity kind and permId.
         */
        private void openInitialEntityViewer(String entityKindValue, String permIdValue)
                throws UserFailureException
        {
            EntityKind entityKind = getEntityKind(entityKindValue);
            OpenEntityDetailsTabHelper.open(viewContext, entityKind, permIdValue);
        }

        private void openInitialEntitySearch(String entityKindValue) throws UserFailureException
        {
            EntityKind entityKind = getEntityKind(entityKindValue);
            if (EntityKind.SAMPLE != entityKind)
            {
                throw new UserFailureException(
                        "URLs for searching openBIS only support SAMPLE searches. Entity "
                                + entityKind + " is not supported.");
            }

            String codeStringOrNull = tryGetUrlParamValue(SearchlinkUtilities.CODE_PARAMETER_KEY);
            if (codeStringOrNull != null)
            {
                openEntitySearch(entityKind, codeStringOrNull);
            } else
            {
                // default the search string
                openEntitySearch(entityKind, DEFAULT_SEARCH_STRING);
            }
        }

        private void checkMissingURLParameter(String valueOrNull, String parameter)
                throws UserFailureException
        {
            if (valueOrNull == null)
            {
                throw new UserFailureException("Missing URL parameter: " + parameter);
            }
        }

        private void openEntitySearch(EntityKind entityKind, String codeString)
        {
            ListSampleDisplayCriteria displayCriteria =
                    getListSampleDisplayCriteriaForCodeString(codeString);

            viewContext.getCommonService().listSamples(displayCriteria,
                    new OpenEntitySearchTabCallback(codeString, displayCriteria));
        }

        /**
         * Convert the code into a ListSampleDisplayCriteria -- a LSDC is used to pass the search
         * parameters to the server.
         */
        private ListSampleDisplayCriteria getListSampleDisplayCriteriaForCodeString(
                String codeString)
        {
            // Create a display criteria object for the search string
            ListSampleDisplayCriteria displayCriteria = ListSampleDisplayCriteria.createForSearch();
            DetailedSearchCriterion searchCriterion =
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(SampleAttributeSearchFieldKind.CODE), codeString);

            DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
            searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
            ArrayList<DetailedSearchCriterion> criterionList =
                    new ArrayList<DetailedSearchCriterion>();
            criterionList.add(searchCriterion);
            searchCriteria.setCriteria(criterionList);
            displayCriteria.updateSearchCriteria(searchCriteria);
            return displayCriteria;
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

    private class OpenEntitySearchTabCallback implements
            AsyncCallback<ResultSetWithEntityTypes<Sample>>
    {
        private final String codeString;

        private final ListSampleDisplayCriteria displayCriteria;

        private OpenEntitySearchTabCallback(String codeString,
                ListSampleDisplayCriteria displayCriteria)
        {
            this.codeString = codeString;
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
                    MessageBox.alert("Error",
                            "No samples with code " + codeString + " were found.", null);
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
                            new OpenEntitySearchGridTabAction(codeString, displayCriteria);

                    DispatcherHelper.dispatchNaviEvent(searchAction);

                    break;
            }
        }
    }

    private class OpenEntitySearchGridTabAction implements ITabItemFactory
    {
        private final ListSampleDisplayCriteria displayCriteria;

        private OpenEntitySearchGridTabAction(String codeString,
                ListSampleDisplayCriteria displayCriteria)
        {
            this.displayCriteria = displayCriteria;
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

        @SuppressWarnings("unchecked")
        public ITabItem create()
        {
            IDisposableComponent browser =
                    SampleSearchHitGrid.createWithInitialDisplayCriteria(
                            (IViewContext<ICommonClientServiceAsync>) viewContext, displayCriteria);
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

    public static final String createTemplateURL(EntityKind kind, EntityType type,
            boolean withCodes, boolean withExperiments, BatchOperationKind operationKind)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.TEMPLATE_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.ENTITY_KIND_KEY_PARAMETER, kind.name());
        methodWithParameters.addParameter(GenericConstants.ENTITY_TYPE_KEY_PARAMETER, type
                .getCode());
        methodWithParameters.addParameter(GenericConstants.AUTO_GENERATE, withCodes);
        methodWithParameters.addParameter(GenericConstants.WITH_EXPERIMENTS, withExperiments);
        methodWithParameters.addParameter(GenericConstants.BATCH_OPERATION_KIND, operationKind
                .name());
        return methodWithParameters.toString();
    }

}
