package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class GenericViewContext extends AbstractPluginViewContext<IGenericClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "generic";

    private final IMessageProvider messageProvider;

    private final IGenericClientServiceAsync service;

    public GenericViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
        // We currently use the message provider of the common view context.
        // this.messageProvider =
        // new CompositeMessageProvider(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME),
        // commonViewContext.getMessageProvider());
        this.messageProvider = commonViewContext.getMessageProvider();
        this.service = createScreeningClientService();
    }

    private final static IGenericClientServiceAsync createScreeningClientService()
    {
        final IGenericClientServiceAsync service = GWT.create(IGenericClientService.class);
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

    public final IGenericClientServiceAsync getService()
    {
        return service;
    }
}