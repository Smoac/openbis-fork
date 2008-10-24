package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IGenericImageBundle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IPageController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.AbstractDictionaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * The <i>screening</i> specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class ScreeningViewContext extends AbstractViewContext<IScreeningClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "screening";

    private final IViewContext<IGenericClientServiceAsync> originalViewContext;

    private final IMessageProvider messageProvider;

    private final IScreeningClientServiceAsync service;

    public ScreeningViewContext(final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        this.originalViewContext = originalViewContext;
        this.messageProvider =
                new CompositeMessageProvider(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME),
                        (AbstractDictionaryBasedMessageProvider) originalViewContext
                                .getMessageProvider());
        this.service = createScreeningClientService();
    }

    private final static IScreeningClientServiceAsync createScreeningClientService()
    {
        final IScreeningClientServiceAsync service = GWT.create(IScreeningClientService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.createServicePath(TECHNOLOGY_NAME));
        return service;
    }

    //
    // IViewContext
    //

    public final IMessageProvider getMessageProvider()
    {
        return messageProvider;
    }

    public final IGenericImageBundle getImageBundle()
    {
        return originalViewContext.getImageBundle();
    }

    public final GenericViewModel getModel()
    {
        return originalViewContext.getModel();
    }

    public final IPageController getPageController()
    {
        return originalViewContext.getPageController();
    }

    public final IScreeningClientServiceAsync getService()
    {
        return service;
    }

    public final IClientPluginFactoryProvider getClientPluginFactoryProvider()
    {
        return originalViewContext.getClientPluginFactoryProvider();
    }
}