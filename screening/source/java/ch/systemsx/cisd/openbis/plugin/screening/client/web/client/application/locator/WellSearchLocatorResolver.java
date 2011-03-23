package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchGrid;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;

/**
 * Locator resolver for Well Search which displays wells containing specified materials optionally
 * restricted to one experiment.
 * 
 * @author Tomasz Pylak
 */
public class WellSearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public WellSearchLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.WELL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getOptionalParameter(locator,
                        ScreeningLinkExtractor.EXPERIMENT_PERM_ID_PARAMETER_KEY);
        String materialCodesOrProperties =
                getMandatoryParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_MATERIAL_ITEMS_PARAMETER_KEY);
        String materialTypeCodes =
                getMandatoryParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_MATERIAL_TYPES_PARAMETER_KEY);
        boolean exactMatchOnly =
                getMandatoryBooleanParameter(locator,
                        ScreeningLinkExtractor.WELL_SEARCH_IS_EXACT_PARAMETER_KEY);

        MaterialSearchCodesCriteria materialCodesCriteria =
                new MaterialSearchCodesCriteria(decodeList(materialCodesOrProperties),
                        decodeList(materialTypeCodes), exactMatchOnly);

        MaterialSearchCriteria materialSearchCriteria =
                MaterialSearchCriteria.create(materialCodesCriteria);
        if (StringUtils.isBlank(experimentPermId))
        {
            WellSearchGrid.openTab(viewContext, ExperimentSearchCriteria.createAllExperiments(),
                    materialSearchCriteria);
        } else
        {
            WellSearchGrid.openTab(viewContext, experimentPermId, materialSearchCriteria);
        }

    }

    private String[] decodeList(String itemsList)
    {
        return URLListEncoder.decodeItemList(itemsList);
    }
}