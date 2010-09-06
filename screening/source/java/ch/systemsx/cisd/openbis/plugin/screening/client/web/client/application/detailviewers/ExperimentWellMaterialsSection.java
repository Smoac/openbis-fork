package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
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
    protected IDisposableComponent createDisposableContent()
    {
        return ExperimentWellMaterialBrowserGrid.createForExperiment(screeningViewContext, TechId
                .create(experiment));
    }

}