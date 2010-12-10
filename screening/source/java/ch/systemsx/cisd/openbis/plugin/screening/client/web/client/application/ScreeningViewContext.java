package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractPluginViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.PlateLocationsMaterialLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.PlateMaterialReviewerLocatorResolver;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator.PlateMetadataBrowserLocatorResolver;

/**
 * The <i>screening</i> plugin specific {@link IViewContext} implementation.
 * 
 * @author Tomasz Pylak
 */
public final class ScreeningViewContext extends
        AbstractPluginViewContext<IScreeningClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "screening";

    public ScreeningViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        super(commonViewContext);
    }

    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    protected IScreeningClientServiceAsync createClientServiceAsync()
    {
        return GWT.create(IScreeningClientService.class);
    }

    @Override
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry)
    {
        super.initializeLocatorHandlerRegistry(handlerRegistry);

        handlerRegistry.registerHandler(new PlateLocationsMaterialLocatorResolver(this));
        handlerRegistry.registerHandler(new PlateMetadataBrowserLocatorResolver(this));
        handlerRegistry.registerHandler(new PlateMaterialReviewerLocatorResolver(this));
    }

    public static ScreeningDisplaySettingsManager getTechnologySpecificDisplaySettingsManager(
            IViewContext<?> viewContext)
    {
        return new ScreeningDisplaySettingsManager(viewContext);
    }

}
