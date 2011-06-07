package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Material section panel showing feature vector summaries from all experiments.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeaturesFromAllExpermentsSection extends DisposableTabContent
{

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier material;

    private final ExperimentSearchByProjectCriteria experimentCriteria;

    public MaterialFeaturesFromAllExpermentsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier material,
            ExperimentSearchByProjectCriteria experimentCriteria)
    {
        super(screeningViewContext.getMessage(Dict.MATERIAL_FEATURES_FROM_ALL_EXPERIMENTS_SECTION),
                screeningViewContext, material);
        this.screeningViewContext = screeningViewContext;
        this.material = material;
        this.experimentCriteria = experimentCriteria;
        setIds(DisplayTypeIDGenerator.MATERIAL_FEATURES_FROM_ALL_EXPERIMENTS_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return MaterialFeaturesFromAllExperimentsGrid.create(screeningViewContext, material,
                experimentCriteria);
    }

}