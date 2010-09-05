package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;

/**
 * Experiment section panel which shows all materials in the experiment.
 * 
 * @author Piotr Buczek
 */
public class ExperimentWellMaterialsSection extends DisposableSectionPanel
{
    public static final String ID_SUFFIX = "ExperimentWellMaterialsSection";

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    private Collection<Long> allowedMaterialIds;

    public ExperimentWellMaterialsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getMessage(Dict.EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION),
                screeningViewContext);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
        setDisplayID(DisplayTypeIDGenerator.PLATE_MATERIAL_BROWSER, ID_SUFFIX);
    }

    @Override
    protected void showContent()
    {
        IDelegatedAction onSuccessAction = new IDelegatedAction()
            {
                public void execute()
                {
                    ExperimentWellMaterialsSection.super.showContent();
                }
            };
        screeningViewContext.getService().listExperimentMaterials(
                TechId.create(experiment),
                new ListMaterialIdsCallback(viewContext.getCommonViewContext(), this,
                        onSuccessAction));
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return MaterialBrowserGrid.createWithTypeChooser(viewContext.getCommonViewContext(),
                allowedMaterialIds);
    }

    private final class ListMaterialIdsCallback extends AbstractAsyncCallback<Collection<Long>>
    {

        private final IDelegatedAction onSuccessAction;

        private ExperimentWellMaterialsSection section;

        public ListMaterialIdsCallback(final IViewContext<ICommonClientServiceAsync> viewContext,
                ExperimentWellMaterialsSection section, IDelegatedAction onSuccessAction)
        {
            super(viewContext);
            this.section = section;
            this.onSuccessAction = onSuccessAction;
        }

        @Override
        protected void process(Collection<Long> result)
        {
            section.allowedMaterialIds = result;
            onSuccessAction.execute();
        }

    }

}