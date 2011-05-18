package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import java.util.Map;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.dto.ExperimentIdentifierSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Material detail view for screening materials. Overrides {@link MaterialLocatorResolver}.
 * 
 * @author Piotr Buczek
 */
public class ImagingMaterialLocatorResolver extends MaterialLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public ImagingMaterialLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
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

        ExperimentIdentifierSearchCriteria experimentCriteriaOrNull =
                tryGetExperimentIdentifierSearchCriteria(locator.getParameters());
        openInitialMaterialViewer(extractMaterialIdentifier(locator), experimentCriteriaOrNull);
    }

    private static ExperimentIdentifierSearchCriteria tryGetExperimentIdentifierSearchCriteria(
            Map<String, String> parameters)
    {
        String experimentIdentifierOrNull =
                parameters
                        .get(ScreeningLinkExtractor.MATERIAL_DETAIL_EXPERIMENT_IDENT_PARAMETER_KEY);
        if (experimentIdentifierOrNull == null)
        {
            String searchAllExp =
                    parameters
                            .get(ScreeningLinkExtractor.MATERIAL_DETAIL_SEARCH_ALL_EXPERIMENTS_PARAMETER_KEY);
            if (StringUtils.isBlank(searchAllExp) == false
                    && searchAllExp.equalsIgnoreCase("false") == false)
            {
                return ExperimentIdentifierSearchCriteria.createSearchAll();
            } else
            {
                return null;
            }
        } else
        {
            return new ExperimentIdentifierSearchCriteria(experimentIdentifierOrNull);
        }
    }

    /**
     * Open the gene material details tab for the specified identifier. Optionally select experiment
     * in the viewer.
     */
    protected void openInitialMaterialViewer(MaterialIdentifier identifier,
            ExperimentIdentifierSearchCriteria experimentCriteriaOrNull)
            throws UserFailureException
    {
        viewContext.getCommonService().getMaterialInformationHolder(identifier,
                new OpenEntityDetailsTabCallback(viewContext, experimentCriteriaOrNull));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final IViewContext<IScreeningClientServiceAsync> viewContext;

        private final ExperimentIdentifierSearchCriteria experimentCriteriaOrNull;

        private OpenEntityDetailsTabCallback(
                final IViewContext<IScreeningClientServiceAsync> viewContext,
                ExperimentIdentifierSearchCriteria experimentCriteriaOrNull)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.experimentCriteriaOrNull = experimentCriteriaOrNull;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolderWithPermId material)
        {
            if (experimentCriteriaOrNull == null)
            {
                openImagingMaterialViewer(material, null);
            } else
            {
                if (experimentCriteriaOrNull.searchAllExperiments())
                {
                    openImagingMaterialViewer(material,
                            ExperimentSearchCriteria.createAllExperiments());
                } else
                {
                    fetchExperimentAndShowLocations(material,
                            experimentCriteriaOrNull.tryGetExperimentIdentifier());
                }
            }
        }

        private void fetchExperimentAndShowLocations(
                final IEntityInformationHolderWithPermId material, String experimentIdentifier)
        {
            viewContext.getCommonService().getExperimentInfo(experimentIdentifier,
                    new AbstractAsyncCallback<Experiment>(viewContext)
                        {
                            @Override
                            protected void process(Experiment experiment)
                            {
                                ExperimentSearchCriteria experimentCriteria =
                                        ExperimentSearchCriteria.createExperiment(experiment);
                                openImagingMaterialViewer(material, experimentCriteria);
                            }

                        });
        }

        private final void openImagingMaterialViewer(
                final IEntityInformationHolderWithPermId material,
                final ExperimentSearchCriteria experimentSearchCriteriaOrNull)
        {
            ClientPluginFactory.openImagingMaterialViewer(material, experimentSearchCriteriaOrNull,
                    viewContext);
        }
    }

}