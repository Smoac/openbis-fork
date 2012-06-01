package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * {@link IViewLocatorResolver} for entity browser locators.
 * 
 * @author Piotr Buczek
 */
public class BrowserLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String BROWSE_ACTION = "BROWSE";

    public final static String TYPE_PARAMETER_KEY = "type";

    public final static String SPACE_PARAMETER_KEY = "space";

    public final static String PROJECT_PARAMETER_KEY = "project";

    public BrowserLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(BROWSE_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(locator);
        final String entityTypeOrNull = locator.getParameters().get(TYPE_PARAMETER_KEY);
        final String spaceOrNull = locator.getParameters().get(SPACE_PARAMETER_KEY);
        final String projectOrNull = locator.getParameters().get(PROJECT_PARAMETER_KEY);
        switch (entityKind)
        {
            case EXPERIMENT:
                openExperimentBrowser(spaceOrNull, projectOrNull, entityTypeOrNull);
                break;
            case SAMPLE:
                openSampleBrowser(spaceOrNull, entityTypeOrNull);
                break;
            case MATERIAL:
                openMaterialBrowser(entityTypeOrNull);
                break;
            default:
                throw new UserFailureException("Browsing " + entityKind.getDescription()
                        + "s using URLs is not supported.");
        }
    }

    private void openExperimentBrowser(String initialSpaceOrNull, String initialProjectOrNull,
            String initialExperimentTypeOrNull)
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext).getExperimentBrowser(
                initialSpaceOrNull, initialProjectOrNull, initialExperimentTypeOrNull));
    }

    private void openSampleBrowser(String initialGroupOrNull, String initialSampleTypeOrNull)
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext).getSampleBrowser(
                initialGroupOrNull, initialSampleTypeOrNull));
    }

    private void openMaterialBrowser(String initialMaterialTypeOrNull)
    {
        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext)
                .getMaterialBrowser(initialMaterialTypeOrNull));
    }

}