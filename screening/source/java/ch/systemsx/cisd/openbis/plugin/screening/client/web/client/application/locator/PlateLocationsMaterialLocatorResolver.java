package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;

/**
 * {@link MaterialLocatorResolver} for screening materials.
 * 
 * @author Piotr Buczek
 */
public class PlateLocationsMaterialLocatorResolver extends MaterialLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final static String EXPERIMENT_PARAMETER_KEY = "experiment";

    public PlateLocationsMaterialLocatorResolver(
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext());
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        // If there is exactly one material matching given parameters open its detail view,
        // otherwise show an error message.
        assert (EntityKind.MATERIAL.name().equals(locator.tryGetEntity()));

        String experimentIdentifierOrNull = locator.getParameters().get(EXPERIMENT_PARAMETER_KEY);
        openInitialMaterialViewer(extractMaterialIdentifier(locator), experimentIdentifierOrNull);
    }

    /**
     * Open the gene material details tab for the specified identifier. Optionally select experiment
     * in the viewer.
     */
    protected void openInitialMaterialViewer(MaterialIdentifier identifier,
            String experimentIdentifierOrNull) throws UserFailureException
    {
        viewContext.getCommonService().getMaterialInformationHolder(identifier,
                new OpenEntityDetailsTabCallback(viewContext, experimentIdentifierOrNull));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolder>
    {
        private final IViewContext<IScreeningClientServiceAsync> viewContext;

        private final ExperimentIdentifier experimentIdentifierOrNull;

        private OpenEntityDetailsTabCallback(
                final IViewContext<IScreeningClientServiceAsync> viewContext,
                String experimentIdentifierOrNull)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.experimentIdentifierOrNull =
                    experimentIdentifierOrNull != null ? new ExperimentIdentifier(
                            experimentIdentifierOrNull) : null;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolder material)
        {
            if (experimentIdentifierOrNull == null)
            {
                ClientPluginFactory.openPlateLocationsMaterialViewer(material, null, viewContext);
            } else
            {
                fetchExperimentAndShowLocations(material, experimentIdentifierOrNull);
            }
        }

        private void fetchExperimentAndShowLocations(final IEntityInformationHolder material,
                ExperimentIdentifier experimentIdentifier)
        {
            viewContext.getCommonService().getExperimentInfo(experimentIdentifier.getIdentifier(),
                    new AbstractAsyncCallback<Experiment>(viewContext)
                        {
                            @Override
                            protected void process(Experiment experiment)
                            {
                                ExperimentSearchCriteria experimentCriteria =
                                        ExperimentSearchCriteria.createExperiment(experiment
                                                .getId(), experiment.getIdentifier());
                                ClientPluginFactory.openPlateLocationsMaterialViewer(material,
                                        experimentCriteria,
                                        OpenEntityDetailsTabCallback.this.viewContext);
                            }

                        });
        }
    }

}