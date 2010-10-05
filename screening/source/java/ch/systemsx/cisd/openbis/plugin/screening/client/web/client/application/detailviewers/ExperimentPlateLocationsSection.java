package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer.IURLProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineItemsField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;

/**
 * Experiment section panel which allows to find wells were selected genes have been inhibited.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentPlateLocationsSection extends TabContent
{
    public static final String ID_SUFFIX = "ExperimentPlateLocationsSection";

    private static final int TEXT_AREA_WIDTH = 500;

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    private final MultilineItemsField materialListField;

    private final CheckBoxField exactMatchOnly;

    private List<MaterialType> materialTypesOrNull;

    public ExperimentPlateLocationsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getMessage(Dict.EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION),
                screeningViewContext, experiment);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
        this.materialListField = createMaterialListArea();
        this.exactMatchOnly =
                new CheckBoxField(screeningViewContext.getMessage(Dict.EXACT_MATCH_ONLY), false);
        exactMatchOnly.setBoxLabel(screeningViewContext.getMessage(Dict.EXACT_MATCH_ONLY));
        exactMatchOnly.setValue(true);
        setIds(DisplayTypeIDGenerator.EXPERIMENT_PLATE_LOCATIONS_SECTION);
        screeningViewContext.getCommonService().listMaterialTypes(
                new AbstractAsyncCallback<List<MaterialType>>(screeningViewContext)
                    {
                        @Override
                        protected void process(List<MaterialType> result)
                        {
                            materialTypesOrNull = result;
                        }
                    });
    }

    private MultilineItemsField createMaterialListArea()
    {
        MultilineItemsField area = new MultilineItemsField("", true, 10);
        area.setWidth(TEXT_AREA_WIDTH);
        area.setEmptyText(screeningViewContext
                .getMessage(Dict.PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS));
        area.setLabelSeparator("");

        return area;
    }

    @Override
    protected void showContent()
    {
        LayoutContainer container = new LayoutContainer(new RowLayout());

        container.add(exactMatchOnly);
        container.add(materialListField);
        container.add(createSearchLink());

        container.setScrollMode(Scroll.AUTO);
        add(container, new MarginData(10));

    }

    private Widget createSearchLink()
    {
        Button searchButton = new Button(screeningViewContext.getMessage(Dict.SEARCH_BUTTON));
        searchButton.setWidth(TEXT_AREA_WIDTH);
        IDelegatedAction normalModeAction = new IDelegatedAction()
            {
                public void execute()
                {
                    showPlateMaterialReviewer();
                }
            };
        IURLProvider urlProvider = new IURLProvider()
            {
                public String tryGetURL()
                {
                    MaterialSearchCodesCriteria materialCriteria = tryGetMaterialSearchCriteria();
                    if (materialCriteria == null)
                    {
                        return null;
                    }
                    return ScreeningLinkExtractor.createWellsSearchLink(experiment.getPermId(),
                            materialCriteria);
                }
            };
        return LinkRenderer.createLink(searchButton, normalModeAction, urlProvider, viewContext);
    }

    private void showPlateMaterialReviewer()
    {
        MaterialSearchCodesCriteria materialCriteria = tryGetMaterialSearchCriteria();
        if (materialCriteria == null)
        {
            return;
        }
        ExperimentSearchCriteria experimentCriteria = getExperimentSearchCriteria();
        WellSearchGrid.openTab(screeningViewContext, experimentCriteria,
                MaterialSearchCriteria.create(materialCriteria));
    }

    private ExperimentSearchCriteria getExperimentSearchCriteria()
    {
        return ExperimentSearchCriteria.createExperiment(experiment.getId(),
                experiment.getIdentifier());
    }

    private MaterialSearchCodesCriteria tryGetMaterialSearchCriteria()
    {
        String[] materialItemList = materialListField.tryGetModifiedItemList();
        if (materialItemList == null || materialItemList.length == 0)
        {
            return null;
        }
        // TODO 2010-07-23, Tomasz Pylak: allow user to choose the types
        // Now we search using all types available.
        if (materialTypesOrNull == null)
        {
            return null;
        }
        String[] materialTypeCodes = Code.extractCodesToArray(materialTypesOrNull);
        return new MaterialSearchCodesCriteria(materialItemList, materialTypeCodes,
                exactMatchOnly.getValue());
    }
}