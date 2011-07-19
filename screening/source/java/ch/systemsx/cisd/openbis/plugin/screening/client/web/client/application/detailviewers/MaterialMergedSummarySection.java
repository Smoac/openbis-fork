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
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;
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

    private final ExperimentSearchCriteriaHolder experimentSearchCriteriaHolder;

    private final AnalysisProcedureListenerHolder analysisProcedureListenerHolder =
            new AnalysisProcedureListenerHolder();

    private AnalysisProcedureCriteria initialAnalysisProcedureCriteriaOrNull;

    // TODO 2011-07-19, Tomasz Pylak: use analysisProcedureCriteria
    public MaterialMergedSummarySection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Material material,
            ExperimentSearchCriteria experimentCriteriaOrNull,
            AnalysisProcedureCriteria initialAnalysisProcedureCriteriaOrNull,
            boolean restrictGlobalScopeLinkToProject)
    {
        super(screeningViewContext.getMessage(Dict.MATERIAL_MERGED_SUMMARY_SECTION_TITLE),
                screeningViewContext, material);
        this.screeningViewContext = screeningViewContext;
        this.material = material;
        this.initialAnalysisProcedureCriteriaOrNull = initialAnalysisProcedureCriteriaOrNull;
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
                        showMaterialReplicaSummaryComponent(experimentPermId);
                    } else
                    {
                        IDisposableComponent allExperimentsComponent =
                                createMaterialFeaturesFromAllExperimentsComponent(criteriaOrNull);
                        replaceContent(allExperimentsComponent);
                    }
                }
            };
    }

    private void showMaterialReplicaSummaryComponent(final String experimentPermId)
    {
        screeningViewContext.getCommonService().getExperimentInfoByPermId(experimentPermId,
                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                    {
                        @Override
                        protected void process(Experiment experiment)
                        {
                            IDisposableComponent viewer =
                                    createMaterialReplicaSummaryComponent(experiment);
                            replaceContent(viewer);
                        }
                    });
    }

    private IDisposableComponent createMaterialFeaturesFromAllExperimentsComponent(
            ExperimentSearchCriteria criteriaOrNull)
    {
        final ExperimentSearchByProjectCriteria experimentSearchCriteria =
                criteriaOrNull == null ? null : criteriaOrNull.tryAsSearchByProjectCriteria();
        IDisposableComponent allExperimentsComponent =
                MaterialFeaturesFromAllExperimentsComponent.createComponent(screeningViewContext,
                        material, experimentSearchCriteria, analysisProcedureListenerHolder);
        setInitialAnalysisProcedureCriteriaAndReset();
        return allExperimentsComponent;
    }

    private IDisposableComponent createMaterialReplicaSummaryComponent(Experiment experiment)
    {
        IDisposableComponent viewer =
                MaterialReplicaSummaryComponent
                        .createViewer(screeningViewContext, experiment, material,
                                restrictGlobalScopeLinkToProject, analysisProcedureListenerHolder);
        setInitialAnalysisProcedureCriteriaAndReset();
        return viewer;
    }

    /**
     * The first time when the grid is shown, we set the initial analysis procedure. Later on the
     * user choice is kept.
     */
    private void setInitialAnalysisProcedureCriteriaAndReset()
    {
        if (initialAnalysisProcedureCriteriaOrNull != null)
        {
            analysisProcedureListenerHolder.getAnalysisProcedureListener()
                    .analysisProcedureSelected(initialAnalysisProcedureCriteriaOrNull);
            initialAnalysisProcedureCriteriaOrNull = null;
        }
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
        final SingleOrAllExperimentsChooser experimentsChooser = createExperimentChooser();
        getHeader().addTool(experimentsChooser);
        getHeader().addTool(createAnalysisProcedureChooser());

        // WORKAROUND to GXT private widgetPanel in Header with fixed "float: right" set onRender
        experimentsChooser.getParent().addStyleName("force-float-left");
    }

    private SingleOrAllExperimentsChooser createExperimentChooser()
    {
        return new SingleOrAllExperimentsChooser(screeningViewContext,
                experimentSearchCriteriaHolder, restrictGlobalScopeLinkToProject,
                createRefreshAction(experimentSearchCriteriaHolder));
    }

    private AnalysisProcedureChooser createAnalysisProcedureChooser()
    {
        return AnalysisProcedureChooser.createVertical(screeningViewContext,
                experimentSearchCriteriaHolder, null, createAnalysisProcedureListener());
    }

    private IAnalysisProcedureSelectionListener createAnalysisProcedureListener()
    {
        return new IAnalysisProcedureSelectionListener()
            {
                public void analysisProcedureSelected(AnalysisProcedureCriteria criteria)
                {
                    IAnalysisProcedureSelectionListener delegateListener =
                            analysisProcedureListenerHolder.getAnalysisProcedureListener();
                    if (delegateListener != null)
                    {
                        // dispatch the event to the currently shown grid component
                        delegateListener.analysisProcedureSelected(criteria);
                    }
                }
            };
    }
}