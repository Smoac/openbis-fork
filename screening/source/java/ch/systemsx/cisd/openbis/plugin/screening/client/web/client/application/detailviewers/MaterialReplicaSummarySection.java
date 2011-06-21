package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;

/**
 * Section in material detail view which presents {@link MaterialReplicaSummaryComponent}.
 * 
 * @author Tomasz Pylak
 */
class MaterialReplicaSummarySection extends TabContent
{
    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final Material material;

    private final String experimentPermId;

    private final boolean restrictGlobalScopeLinkToProject;

    private IDisposableComponent viewer;

    public MaterialReplicaSummarySection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Material material,
            String experimentPermId, boolean restrictGlobalScopeLinkToProject)
    {
        super(screeningViewContext.getMessage(Dict.MATERIAL_REPLICA_SUMMARY_SECTION_TITLE, ""),
                screeningViewContext, material);
        this.screeningViewContext = screeningViewContext;
        this.material = material;
        this.experimentPermId = experimentPermId;
        this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;

        setHeaderVisible(false);
        setIds(DisplayTypeIDGenerator.MATERIAL_REPLICA_SUMMARY_SECTION);
    }

    @Override
    protected void showContent()
    {
        screeningViewContext.getCommonService().getExperimentInfoByPermId(experimentPermId,
                new AbstractAsyncCallback<Experiment>(screeningViewContext)
                    {
                        @Override
                        protected void process(Experiment experiment)
                        {
                            setHeading(screeningViewContext.getMessage(
                                    Dict.MATERIAL_REPLICA_SUMMARY_SECTION_TITLE,
                                    experiment.getCode()));
                            createAndShowViewer(experiment);
                            // NOTE: we need this because the viewer has been shown asynchronously
                            // and the sections framework could perform the layout to early
                            layout();
                            syncSize();
                        }
                    });
    }

    private void createAndShowViewer(Experiment experiment)
    {
        this.viewer =
                MaterialReplicaSummaryComponent.createViewer(screeningViewContext, experiment,
                        material, restrictGlobalScopeLinkToProject);
        add(viewer.getComponent());
    }

    @Override
    public void disposeComponents()
    {
        if (viewer != null)
        {
            viewer.dispose();
        }
    }
}