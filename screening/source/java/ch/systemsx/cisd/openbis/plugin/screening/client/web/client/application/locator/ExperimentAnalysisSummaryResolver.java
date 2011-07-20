package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ExperimentAnalysisSummaryViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

/**
 * Locator resolver for experiment analysis summary view.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummaryResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public ExperimentAnalysisSummaryResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.EXPERIMENT_ANALYSIS_SUMMARY_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getMandatoryParameter(
                        locator,
                        ScreeningLinkExtractor.EXPERIMENT_ANALYSIS_SUMMARY_EXPERIMENT_PERMID_PARAMETER_KEY);
        boolean restrictGlobalScopeLinkToProject =
                getOptionalBooleanParameter(locator,
                        ScreeningLinkExtractor.RESTRICT_GLOBAL_SEARCH_TO_PROJECT, false);

        AnalysisProcedureCriteria analysisProcedureCriteria =
                ScreeningResolverUtils.extractAnalysisProcedureCriteria(locator);

        ExperimentAnalysisSummaryViewer.openTab(viewContext, experimentPermId,
                restrictGlobalScopeLinkToProject, analysisProcedureCriteria);

    }

}