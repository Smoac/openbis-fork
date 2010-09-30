package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateMaterialReviewer2;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.MaterialSearchCodesCriteria;

/**
 * Locator resolver for plate metadata browser.
 * 
 * @author Tomasz Pylak
 */
public class PlateMaterialReviewerLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public PlateMaterialReviewerLocatorResolver(
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.WELL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(ViewLocator locator) throws UserFailureException
    {
        String experimentPermId =
                getMandatoryParameter(locator,
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
        PlateMaterialReviewer2.openTab(viewContext, experimentPermId, materialCodesCriteria);
    }

    private String[] decodeList(String itemsList)
    {
        return URLListEncoder.decodeItemList(itemsList);
    }
}