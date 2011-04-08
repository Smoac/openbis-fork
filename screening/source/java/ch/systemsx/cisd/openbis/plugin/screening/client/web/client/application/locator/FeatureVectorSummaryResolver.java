package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.FeatureVectorSummaryGrid;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * Locator resolver for plate metadata browser.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorSummaryResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public FeatureVectorSummaryResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.FEATURE_VECTOR_SUMMARY_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getMandatoryParameter(
                        locator,
                        ScreeningLinkExtractor.FEATURE_VECTOR_SUMMARY_EXPERIMENT_PERMID_PARAMETER_KEY);

        FeatureVectorSummaryGrid.openTab(viewContext, experimentPermId);

    }
}