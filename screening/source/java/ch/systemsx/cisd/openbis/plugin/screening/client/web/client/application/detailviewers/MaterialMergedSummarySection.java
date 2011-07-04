package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;

/**
 * Section in material detail view which presents either
 * {@link MaterialFeaturesFromAllExperimentsComponent} or {@link MaterialReplicaSummaryComponent}
 * for a selected experiment.
 * 
 * @author Piotr Buczek
 */
class MaterialMergedSummarySection extends DisposableTabContent
{
    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final Material material;

    private final boolean restrictGlobalScopeLinkToProject;

    private ExperimentSearchCriteriaHolder experimentSearchCriteriaHolder;

    public MaterialMergedSummarySection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Material material,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            boolean restrictGlobalScopeLinkToProject)
    {
        super(screeningViewContext.getMessage(Dict.MATERIAL_MERGED_SUMMARY_SECTION_TITLE),
                screeningViewContext, material);
        this.screeningViewContext = screeningViewContext;
        this.material = material;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        this.experimentSearchCriteriaHolder =
                new ExperimentSearchCriteriaHolder(experimentCriteriaOrNull);
        setIds(DisplayTypeIDGenerator.MATERIAL_MERGED_SUMMARY_SECTION);
    }

    private IDelegatedAction createRefreshAction(
            final ExperimentSearchCriteriaHolder searchCriteriaHolder)
    {
        return new IDelegatedAction()
            {

                public void execute()
                {
                    ExperimentSearchCriteria criteriaOrNull = searchCriteriaHolder.tryGetCriteria();
                    if (criteriaOrNull != null && criteriaOrNull.tryGetExperiment() != null)
                    {
                        final String experimentPermId =
                                criteriaOrNull.tryGetExperiment().getExperimentPermId();
                        screeningViewContext.getCommonService().getExperimentInfoByPermId(
                                experimentPermId,
                                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                                    {
                                        @Override
                                        protected void process(Experiment experiment)
                                        {
                                            replaceContent(MaterialReplicaSummaryComponent
                                                    .createViewer(screeningViewContext, experiment,
                                                            material,
                                                            restrictGlobalScopeLinkToProject));
                                        }
                                    });
                    } else
                    {
                        replaceContent(MaterialFeaturesFromAllExperimentsComponent.createComponent(
                                screeningViewContext, material, criteriaOrNull == null ? null
                                        : criteriaOrNull.tryAsSearchByProjectCriteria()));
                    }
                }
            };
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return null; // content will be automatically added on criteria change
    }

    @Override
    protected void showContent()
    {
        super.showContent();
        setHeading("");
        getHeader().addTool(
                new SingleOrAllExperimentsChooser(screeningViewContext,
                        experimentSearchCriteriaHolder, restrictGlobalScopeLinkToProject,
                        createRefreshAction(experimentSearchCriteriaHolder)));
    }

}