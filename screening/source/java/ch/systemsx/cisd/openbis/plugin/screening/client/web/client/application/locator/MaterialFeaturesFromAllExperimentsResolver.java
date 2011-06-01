package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.MATERIAL_CODE_KEY;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.MATERIAL_TYPE_CODE_KEY;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.PROJECT_CODE_KEY;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor.SPACE_CODE_KEY;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.MaterialFeaturesFromAllExperimentsViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Locator resolver for material summary from all experiments.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeaturesFromAllExperimentsResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public MaterialFeaturesFromAllExperimentsResolver(
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(ScreeningLinkExtractor.MATERIAL_FEATURES_FROM_ALL_EXPERIMENTS_ACTION);
        this.viewContext = viewContext;
    }

    public void resolve(final ViewLocator locator) throws UserFailureException
    {
        String materialCode = getMandatoryParameter(locator, MATERIAL_CODE_KEY);

        String materialTypeCode = getMandatoryParameter(locator, MATERIAL_TYPE_CODE_KEY);

        MaterialIdentifier materialIdentifier =
                new MaterialIdentifier(materialCode, materialTypeCode);

        String projectCodeOrNull = getOptionalParameter(locator, PROJECT_CODE_KEY);
        String spaceCodeOrNull = getOptionalParameter(locator, SPACE_CODE_KEY);
        
        
        ExperimentSearchByProjectCriteria experimentCriteria = null;
        if (StringUtils.isBlank(projectCodeOrNull) || StringUtils.isBlank(spaceCodeOrNull))
        {
            experimentCriteria =
                    ExperimentSearchByProjectCriteria.createAllExperimentsForAllProjects();
        } else
        {
            BasicProjectIdentifier projectIdentifier =
                    new BasicProjectIdentifier(spaceCodeOrNull, projectCodeOrNull);
            experimentCriteria =
                    ExperimentSearchByProjectCriteria
                            .createAllExperimentsForProject(projectIdentifier);
        }

        MaterialFeaturesFromAllExperimentsViewer.openTab(viewContext, materialIdentifier,
                experimentCriteria);

    }
}