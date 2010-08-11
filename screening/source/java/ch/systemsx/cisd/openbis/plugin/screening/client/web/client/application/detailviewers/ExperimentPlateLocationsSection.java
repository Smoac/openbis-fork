package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineItemsField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;

/**
 * Experiment section panel which allows to find wells were selected genes have been inhibited.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentPlateLocationsSection extends SingleSectionPanel
{
    public static final String ID_SUFFIX = "ExperimentPlateLocationsSection";

    private static final int TEXT_AREA_WIDTH = 500;

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    private final MultilineItemsField materialListField;

    private List<MaterialType> materialTypesOrNull;

    public ExperimentPlateLocationsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getMessage(Dict.EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION),
                screeningViewContext);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
        this.materialListField = createMaterialListArea();
        setDisplayID(DisplayTypeIDGenerator.PLATE_MATERIAL_REVIEWER, ID_SUFFIX);
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

        Button searchButton = new Button(screeningViewContext.getMessage(Dict.SEARCH_BUTTON));
        searchButton.setWidth(TEXT_AREA_WIDTH);
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    showPlateMaterialReviewer();
                }
            });

        container.add(new Label(viewContext.getMessage(Dict.PLATE_MATERIAL_REVIEWER_HELP_INFO)));
        container.add(materialListField);
        container.add(searchButton);
        container.setScrollMode(Scroll.AUTO);
        add(container, new MarginData(10));
    }

    private void showPlateMaterialReviewer()
    {
        final IDisposableComponent reviewer = tryCreatePlateMaterialReviewer();
        if (reviewer == null)
        {
            return;
        }
        final AbstractTabItemFactory tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    String tabItemText = viewContext.getMessage(Dict.PLATE_MATERIAL_REVIEWER_TITLE);
                    return DefaultTabItem.create(tabItemText, reviewer, viewContext);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Well Reviewing Panel");
                }

                @Override
                public String getId()
                {
                    final String reportDate =
                            DateTimeFormat.getMediumTimeFormat().format(new Date());
                    return GenericConstants.ID_PREFIX + "-PlateMaterialReviewer-" + reportDate;
                }
            };
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

    private IDisposableComponent tryCreatePlateMaterialReviewer()
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
        return PlateMaterialReviewer.create(screeningViewContext, experiment, materialItemList,
                materialTypeCodes);
    }

}