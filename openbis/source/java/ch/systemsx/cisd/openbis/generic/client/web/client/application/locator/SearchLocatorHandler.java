package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.ArrayList;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorHandlerRegistry.AbstractViewLocatorHandler;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * ViewLocatorHandler for Permlink locators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchLocatorHandler extends AbstractViewLocatorHandler
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final static String SEARCH_ACTION = ViewLocator.SEARCH_ACTION;

    private static final String DEFAULT_SEARCH_STRING = "*";

    public SearchLocatorHandler(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    public void invoke(ViewLocator locator) throws UserFailureException
    {
        // If a searchlink has been specified, open a search on the specified object
        String searchEntityKindValueOrNull = locator.tryGetEntity();
        String codeStringOrNull =
                locator.getParameters().get(SearchlinkUtilities.CODE_PARAMETER_KEY);
        if (null != searchEntityKindValueOrNull)
        {
            openInitialEntitySearch(searchEntityKindValueOrNull, codeStringOrNull);
        }
    }

    private void openInitialEntitySearch(String entityKindValue, String codeStringOrNull)
            throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(entityKindValue);
        if (EntityKind.SAMPLE != entityKind)
        {
            throw new UserFailureException(
                    "URLs for searching openBIS only support SAMPLE searches. Entity " + entityKind
                            + " is not supported.");
        }

        if (codeStringOrNull != null)
        {
            openEntitySearch(entityKind, codeStringOrNull);
        } else
        {
            // default the search string
            openEntitySearch(entityKind, DEFAULT_SEARCH_STRING);
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
    private ListSampleDisplayCriteria getListSampleDisplayCriteriaForCodeString(String codeString)
    {
        // Create a display criteria object for the search string
        ListSampleDisplayCriteria displayCriteria = ListSampleDisplayCriteria.createForSearch();
        DetailedSearchCriterion searchCriterion =
                new DetailedSearchCriterion(DetailedSearchField
                        .createAttributeField(SampleAttributeSearchFieldKind.CODE), codeString);

        DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
        searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        ArrayList<DetailedSearchCriterion> criterionList = new ArrayList<DetailedSearchCriterion>();
        criterionList.add(searchCriterion);
        searchCriteria.setCriteria(criterionList);
        displayCriteria.updateSearchCriteria(searchCriteria);
        return displayCriteria;
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

        public ITabItem create()
        {
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