package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * ViewLocatorHandler for Project locators. We don't have permIds for projects so we need a
 * different way of handling permlinks for them. We use project and space codes to identify project.
 * 
 * @author Piotr Buczek
 */
public class ProjectLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String PROJECT = "PROJECT";

    public final static String CODE_PARAMETER_KEY = "code";

    public final static String SPACE_PARAMETER_KEY = "space";

    public ProjectLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(ViewLocator.PERMLINK_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        return super.canHandleLocator(locator) && PROJECT.equals(entityKindValueOrNull);
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        assert (PROJECT.equals(locator.tryGetEntity()));

        openInitialProjectViewer(extractProjectIdentifier(locator));
    }

    static BasicProjectIdentifier extractProjectIdentifier(ViewLocator locator)
    {
        String codeValueOrNull = locator.getParameters().get(CODE_PARAMETER_KEY);
        String spaceValueOrNull = locator.getParameters().get(SPACE_PARAMETER_KEY);
        checkRequiredParameter(codeValueOrNull, CODE_PARAMETER_KEY);
        checkRequiredParameter(spaceValueOrNull, SPACE_PARAMETER_KEY);
        return new BasicProjectIdentifier(spaceValueOrNull, codeValueOrNull);
    }

    /**
     * Open the material details tab for the specified identifier.
     */
    protected void openInitialProjectViewer(BasicProjectIdentifier identifier)
            throws UserFailureException
    {
        viewContext.getService().getProjectInfo(identifier,
                new OpenProjectDetailsTabCallback(viewContext));
    }

    private static class OpenProjectDetailsTabCallback extends AbstractAsyncCallback<Project>
    {

        private OpenProjectDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final Project result)
        {
            // TODO 2010-05-03, Piotr Buczek: Project data are loaded twice
            final String href = LinkExtractor.tryExtract(result);
            OpenEntityDetailsTabHelper.open(viewContext, result, false, href);
        }
    }

}