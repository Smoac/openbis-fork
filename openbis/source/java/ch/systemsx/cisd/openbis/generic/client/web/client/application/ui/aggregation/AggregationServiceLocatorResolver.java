package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.aggregation;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainPagePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.IViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * An {@link IViewLocatorResolver} that shows the results of an aggregation service as a table
 * model.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AggregationServiceLocatorResolver extends AbstractViewLocatorResolver
{
    final static String ACTION = "AGGREGATION_SERVICE";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public AggregationServiceLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        DispatcherHelper.dispatchNaviEvent(new AbstractTabItemFactory()
            {

                private final static String ID = GenericConstants.ID_PREFIX + ACTION;

                @Override
                public ITabItem create()
                {
                    return DefaultTabItem.createUnaware(getTabTitle(), new AggregationServicePanel(
                            viewContext, MainPagePanel.PREFIX, locator), false, viewContext);
                }

                @Override
                public String getId()
                {
                    return ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

                @Override
                public String getTabTitle()
                {
                    return viewContext.getMessage(Dict.APPLICATION_NAME);
                }

                @Override
                public String tryGetLink()
                {
                    return locator.getHistoryToken();
                }

            });
    }
}