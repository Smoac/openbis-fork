package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils.withLabel;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Section panel presenting wells from selected experiment with given gene inhibited.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
class GenePlateLocationsSection extends SingleSectionPanel
{
    private static final int IMAGE_WIDTH_PX = 100;

    private static final int IMAGE_HEIGHT_PX = 60;

    public static final String ID_SUFFIX = "LocationsSection";

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final TechId materialId;

    private final ExperimentIdentifier experimentIdentifierOrNull;

    private final DefaultChannelState channelState;

    public GenePlateLocationsSection(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final TechId materialId, ExperimentIdentifier experimentIdentifierOrNull)
    {
        super(
                screeningViewContext
                        .getMessage(ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.PLATE_LOCATIONS),
                screeningViewContext);
        setHeaderVisible(false);
        this.screeningViewContext = screeningViewContext;
        this.materialId = materialId;
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
        this.channelState = new DefaultChannelState();
        setDisplayID(DisplayTypeIDGenerator.SAMPLE_SECTION, ID_SUFFIX);
    }

    @Override
    protected void showContent()
    {
        LayoutContainer locationsPanel = createLocationsPanel();
        add(locationsPanel, new MarginData(10));
        if (experimentIdentifierOrNull != null)
        {
            loadGeneLocationsPanel(experimentIdentifierOrNull, locationsPanel);
        }
    }

    private LayoutContainer createLocationsPanel()
    {
        final LayoutContainer container = new LayoutContainer();

        ExperimentChooserFieldAdaptor experimentChooser =
                ExperimentChooserField.create("", true, null, screeningViewContext
                        .getCommonViewContext());
        ExperimentChooserField chooserField = experimentChooser.getChooserField();
        chooserField.addChosenEntityListener(new IChosenEntityListener<Experiment>()
            {
                public void entityChosen(Experiment entity)
                {
                    if (entity != null)
                    {
                        ExperimentIdentifier experimentIdentifier =
                                new ExperimentIdentifier(entity.getIdentifier());
                        loadGeneLocationsPanel(experimentIdentifier, container);
                    }
                }
            });
        chooserField.setEditable(false);
        if (experimentIdentifierOrNull != null)
        {
            chooserField.updateValue(experimentIdentifierOrNull);
        }

        container.add(GuiUtils.withLabel(experimentChooser.getField(), "Experiment:", 10));
        container.add(new Text(
                "Choose an experiment to find wells where this gene has been suppressed."));
        container.setScrollMode(Scroll.AUTO);
        return container;
    }

    private void loadGeneLocationsPanel(ExperimentIdentifier experimentIdentifier,
            final LayoutContainer container)
    {
        GuiUtils.replaceLastItem(container, new Text(screeningViewContext
                .getMessage(Dict.LOAD_IN_PROGRESS)));
        screeningViewContext.getService().getPlateLocations(materialId, experimentIdentifier,
                new AbstractAsyncCallback<List<WellContent>>(screeningViewContext)
                    {
                        @Override
                        protected void process(List<WellContent> wellLocations)
                        {
                            Widget geneLocationsPanel = createGeneLocationPanel(wellLocations);
                            GuiUtils.replaceLastItem(container, geneLocationsPanel);
                        }
                    });
    }

    private Widget createGeneLocationPanel(final List<WellContent> wellLocations)
    {
        if (wellLocations.size() == 0)
        {
            return new Text(
                    "This gene has not been suppressed in any plate measured in the chosen experiment.");
        }
        List<String> totalChannels = findMaxChannelCollection(wellLocations);
        return ChannelChooser.createViewerWithChannelChooser(new IChanneledViewerFactory()
            {
                public Widget create(int channel)
                {
                    return createGeneLocationPanel(wellLocations, channel);
                }
            }, channelState, totalChannels);
    }

    private static List<String> findMaxChannelCollection(List<WellContent> wells)
    {// FIXME 2010-06-14, IA: channel number-name may not work!!!!
        List<String> channels = new ArrayList<String>();
        for (WellContent well : wells)
        {
            DatasetImagesReference images = well.tryGetImages();
            if (images != null)
            {
                for (String val : images.getImageParameters().getChannelsNames())
                {
                    if (channels.contains(val) == false)
                    {
                        channels.add(val);
                    }
                }
            }
        }
        return channels;
    }

    private Widget createGeneLocationPanel(List<WellContent> wellLocations, int channel)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(3));
        TableData cellLayout = new TableData();
        cellLayout.setPadding(20);
        for (WellContent loc : wellLocations)
        {
            container.add(createLocationDescription(loc, channel), cellLayout);
        }
        return container;
    }

    private LayoutContainer createLocationDescription(WellContent wellContent, int channel)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        int margin = 4;

        Widget plateLink = createEntityLink(wellContent.getPlate());
        container.add(withLabel(plateLink, "Plate: ", margin));

        Widget wellLink = createLabel(wellContent.getWell());
        container.add(withLabel(wellLink, "Well: ", margin));

        Widget contentLink = createEntityLink(wellContent.getMaterialContent());
        container.add(withLabel(contentLink, "Content: ", margin));

        DatasetImagesReference images = wellContent.tryGetImages();
        if (images != null)
        {
            Widget datasetLink = createEntityLink(images.getDatasetReference(), "show all images");
            container.add(withLabel(datasetLink, "Dataset: ", margin));

            container.add(createImageViewer(images, wellContent.tryGetLocation(), channel));
        }
        return container;
    }

    private Widget createImageViewer(DatasetImagesReference images, WellLocation locationOrNull,
            int channel)
    {
        if (locationOrNull == null)
        {
            return new Text("Incorrect well code.");
        }
        if (channel > images.getImageParameters().getChannelsNames().size())
        {
            return new Text("No images available for this channel.");
        }
        WellImages wellImages = new WellImages(images, locationOrNull);
        return WellContentDialog.createTilesGrid(wellImages, channel, screeningViewContext,
                IMAGE_WIDTH_PX, IMAGE_HEIGHT_PX);
    }

    private Widget createLabel(IEntityInformationHolder entityInformationHolder)
    {
        return new Label(entityInformationHolder.getCode());
    }

    private Widget createEntityLink(IEntityInformationHolder entityInformationHolder)
    {
        return createEntityLink(entityInformationHolder, entityInformationHolder.getCode());
    }

    private Widget createEntityLink(IEntityInformationHolder entityInformationHolder, String label)
    {
        final ClickHandler listener =
                new OpenEntityDetailsTabClickListener(entityInformationHolder, screeningViewContext);
        return LinkRenderer.getLinkWidget(label, listener);
    }
}