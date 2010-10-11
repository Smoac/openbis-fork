/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * A dialog which shows the content of the well (static or a timepoints movie).
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog extends Dialog
{
    private static final String UNKNOWN_WELL_LABEL = "No well information available.";

    private static final String UNKNOWN_CHANNEL_LABEL = "No images available for this channel.";

    private static final String INCORRECT_WELL_CODE_LABEL = "Incorrect well code.";

    private static final String NO_IMAGE_DATASETS_LABEL = "Images not acquired.";

    private static final String WELL_LABEL = "Well: ";

    // ---

    private static final int ONE_IMAGE_WIDTH_PX = 200;

    private static final int ONE_IMAGE_HEIGHT_PX = 120;

    /**
     * A dialog which shows the content of the well (static or a timepoints movie).
     */
    public static void showContentDialog(final WellData wellData,
            DatasetImagesReference imageDatasetOrNull, DefaultChannelState channelState,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        final WellContentDialog contentDialog = createContentDialog(wellData, viewContext);
        showContentDialog(contentDialog, imageDatasetOrNull, channelState, viewContext);
    }

    private static void showContentDialog(final WellContentDialog contentDialog,
            final DatasetImagesReference imagesOrNull, DefaultChannelState channelState,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        if (imagesOrNull != null && imagesOrNull.getImageParameters().isMultidimensional())
        {
            showTimepointImageDialog(contentDialog, imagesOrNull, channelState, viewContext);
        } else
        {
            showStaticImageDialog(contentDialog, imagesOrNull, channelState, viewContext);
        }
    }

    private static WellContentDialog createContentDialog(final WellData wellData,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        WellLocation wellLocation = wellData.getWellLocation();
        WellMetadata wellMetadata = wellData.tryGetMetadata();
        IEntityInformationHolderWithPermId wellOrNull = null;
        List<IEntityProperty> wellPropertiesOrNull = null;
        if (wellMetadata != null)
        {
            wellOrNull = wellMetadata.getWellSample();
            wellPropertiesOrNull = wellMetadata.getWellSample().getProperties();
        }
        return new WellContentDialog(wellOrNull, wellPropertiesOrNull, wellLocation,
                getExperiment(wellData), viewContext);
    }

    /**
     * Creates a view for the specified channel.
     * 
     * @param channel Channel numbers start with 1. Channel 0 consists of all other channels merged.
     */
    public static Widget createImageViewerForChannel(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final WellContent wellContent, int imageWidthPx, int imageHeightPx, String channel)
    {
        final DatasetImagesReference imageDataset = wellContent.tryGetImageDataset();
        if (imageDataset == null)
        {
            return new Text(NO_IMAGE_DATASETS_LABEL);
        }
        WellLocation locationOrNull = wellContent.tryGetLocation();
        if (locationOrNull == null)
        {
            return new Text(INCORRECT_WELL_CODE_LABEL);
        }
        PlateImageParameters imageParameters = imageDataset.getImageParameters();
        if (imageParameters.getChannelsCodes().contains(channel) == false
                && channel.equals(ScreeningConstants.MERGED_CHANNELS) == false)
        {
            return new Text(UNKNOWN_CHANNEL_LABEL);
        }

        boolean createImageLinks = (imageParameters.isMultidimensional() == false);
        String sessionId = getSessionId(viewContext);
        final WellImages wellImages = new WellImages(imageDataset, locationOrNull);
        LayoutContainer staticTilesGrid =
                createTilesGrid(wellImages, channel, sessionId, imageWidthPx, imageHeightPx,
                        createImageLinks);

        if (imageParameters.isMultidimensional())
        {
            staticTilesGrid.sinkEvents(Events.OnClick.getEventCode());
            staticTilesGrid.addListener(Events.OnClick, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        showContentDialog(viewContext, wellContent, imageDataset);
                    }
                });
        }

        return staticTilesGrid;
    }

    private static void showContentDialog(IViewContext<IScreeningClientServiceAsync> viewContext,
            WellContent wellContent, DatasetImagesReference imageDatasetOrNull)
    {
        WellContentDialog contentDialog =
                new WellContentDialog(wellContent.getWell(), null, wellContent.tryGetLocation(),
                        getExperiment(wellContent.getExperiment()), viewContext);

        // NOTE: channel chooser state will be not reused among different dialogs
        DefaultChannelState channelState = new DefaultChannelState();
        showContentDialog(contentDialog, imageDatasetOrNull, channelState, viewContext);
    }

    // --------------- STATIC IMAGES VIEWER

    private static void showStaticImageDialog(final WellContentDialog contentDialog,
            final DatasetImagesReference imageDatasetOrNull, DefaultChannelState channelState,
            final IViewContext<?> viewContext)
    {
        WellLocation wellLocation = contentDialog.wellLocationOrNull;
        if (imageDatasetOrNull != null && wellLocation != null)
        {
            WellImages imagesOrNull = new WellImages(imageDatasetOrNull, wellLocation);
            LayoutContainer imageViewer =
                    createStaticImageViewer(imagesOrNull, channelState, viewContext);
            contentDialog.addComponent(imageViewer);
        }
        contentDialog.show();
    }

    private static LayoutContainer createStaticImageViewer(final WellImages images,
            DefaultChannelState channelState, final IViewContext<?> viewContext)
    {
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(String channel)
                {
                    String sessionId = getSessionId(viewContext);
                    return createTilesGrid(images, channel, sessionId);
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState,
                images.getChannelsCodes());
    }

    private static LayoutContainer createTilesGrid(final WellImages images, String channel,
            String sessionId)
    {
        return createTilesGrid(images, channel, sessionId, getImageWidth(images),
                getImageHeight(images), true);
    }

    private static LayoutContainer createTilesGrid(WellImages images, String channel,
            String sessionId, int imageWidth, int imageHeight, boolean createImageLinks)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(images.getTileColsNum()));
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                ImageUrlUtils.addImageUrlWidget(container, sessionId, images, channel, row, col,
                        imageWidth, imageHeight, createImageLinks);
            }
        }
        return container;
    }

    // --------------- TIMEPOINT IMAGES PLAYER

    private static void showTimepointImageDialog(final WellContentDialog contentDialog,
            final DatasetImagesReference imageDataset, final DefaultChannelState channelState,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        assert imageDataset != null;

        final WellLocation wellLocation = contentDialog.wellLocationOrNull;
        if (wellLocation == null)
        {
            // images stacks cannot be obtained
            showStaticImageDialog(contentDialog, imageDataset, channelState, viewContext);
        }
        viewContext.getService().listImageChannelStacks(imageDataset.getDatasetCode(),
                imageDataset.getDatastoreCode(), wellLocation,
                new AbstractAsyncCallback<List<WellImageChannelStack>>(viewContext)
                    {
                        @Override
                        protected void process(List<WellImageChannelStack> channelStackImages)
                        {
                            if (channelStackImages.size() == 0)
                            {
                                showStaticImageDialog(contentDialog, imageDataset, channelState,
                                        viewContext);
                            } else
                            {
                                WellImages wellImages = new WellImages(imageDataset, wellLocation);
                                LayoutContainer imageViewer =
                                        createTimepointImageViewer(channelStackImages, wellImages,
                                                channelState, viewContext);
                                contentDialog.addComponent(imageViewer);
                                contentDialog.show();
                            }
                        }
                    });
    }

    private static LayoutContainer createTimepointImageViewer(
            final List<WellImageChannelStack> channelStackImages, final WellImages images,
            final DefaultChannelState channelState, IViewContext<?> viewContext)
    {
        final String sessionId = getSessionId(viewContext);
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(String channel)
                {
                    return WellContentTimepointsViewer.createTilesGrid(sessionId,
                            channelStackImages, images, channel, getImageWidth(images),
                            getImageHeight(images));
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState,
                images.getChannelsCodes());
    }

    // ---------------- STATIC METHODS -------------------

    private static int getImageHeight(WellImages images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (ONE_IMAGE_HEIGHT_PX * imageSizeMultiplyFactor);
    }

    private static int getImageWidth(WellImages images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (ONE_IMAGE_WIDTH_PX * imageSizeMultiplyFactor);
    }

    private static SingleExperimentSearchCriteria getExperiment(WellData wellData)
    {
        Experiment experiment = wellData.getExperiment();
        return new SingleExperimentSearchCriteria(experiment.getId(), experiment.getPermId(),
                experiment.getIdentifier());
    }

    private static SingleExperimentSearchCriteria getExperiment(
            ExperimentReference experimentReference)
    {
        return new SingleExperimentSearchCriteria(experimentReference.getId(),
                experimentReference.getPermId(), experimentReference.getExperimentIdentifier());
    }

    private static float getImageSizeMultiplyFactor(WellImages images)
    {
        float dim = Math.max(images.getTileRowsNum(), images.getTileColsNum());
        // if there are more than 3 tiles, make them smaller, if there are less, make them bigger
        return 3.0F / dim;
    }

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }

    // ---------- DIALOG CLASS TO DISPLAY WELL PROPERTIES ------

    private final IEntityInformationHolderWithPermId wellOrNull;

    private final WellLocation wellLocationOrNull;

    private final List<IEntityProperty> wellPropertiesOrNull;

    private final SingleExperimentSearchCriteria experimentCriteria;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private WellContentDialog(IEntityInformationHolderWithPermId wellOrNull,
            List<IEntityProperty> wellPropertiesOrNull, WellLocation wellLocationOrNull,
            SingleExperimentSearchCriteria experimentCriteria,
            IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        this.wellOrNull = wellOrNull;
        this.wellLocationOrNull = wellLocationOrNull;
        this.wellPropertiesOrNull = wellPropertiesOrNull;
        if (wellPropertiesOrNull != null)
        {
            Collections.sort(wellPropertiesOrNull);
        }
        this.experimentCriteria = experimentCriteria;
        this.viewContext = viewContext;
        setScrollMode(Scroll.AUTO);
        setHideOnButtonClick(true);
        setHeading(WELL_LABEL + getWellDescription());
        setTopComponent(createContentDescription());
        addListener(Events.Show, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    Rectangle bounds = GuiUtils.calculateBounds(getElement());
                    int maxWidth = (9 * XDOM.getBody().getOffsetWidth()) / 10;
                    int maxHeight = (9 * XDOM.getBody().getOffsetHeight()) / 10;
                    int w = Math.min(maxWidth, bounds.width + getFrameWidth());
                    int h = Math.min(maxHeight, bounds.height);
                    setSize(w, h);
                    center();
                }
            });
    }

    private void addComponent(LayoutContainer component)
    {
        add(component);
    }

    private String getWellDescription()
    {
        if (wellOrNull != null)
        {
            return wellOrNull.getCode();
        } else
        {
            return "?";
        }
    }

    private LayoutContainer createContentDescription()
    {
        final LayoutContainer container = new LayoutContainer();
        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setCellPadding(3);
        container.setLayout(tableLayout);
        TableData cellLayout = new TableData();
        cellLayout.setMargin(5);
        if (wellOrNull != null)
        {
            container.add(new Text(WELL_LABEL), cellLayout);
            container.add(createEntityLink(wellOrNull));
        } else
        {
            container.add(new Text(UNKNOWN_WELL_LABEL));
        }
        if (wellPropertiesOrNull != null)
        {
            addProperties(container, cellLayout, wellPropertiesOrNull);
        }
        return container;
    }

    private void addProperties(LayoutContainer container, TableData cellLayout,
            List<IEntityProperty> properties)
    {
        for (IEntityProperty property : properties)
        {
            addProperty(container, cellLayout, property);
        }
    }

    private void addProperty(LayoutContainer container, TableData cellLayout,
            IEntityProperty property)
    {
        String propertyLabel = property.getPropertyType().getLabel();
        String propertyValue = property.tryGetAsString();

        container.add(new Text(propertyLabel + ": "), cellLayout);
        Material material = property.getMaterial();
        if (material != null)
        {

            if (material.getMaterialType().getCode()
                    .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
            {
                container.add(createEntityExternalLink(material));
            } else
            {
                container.add(createPlateLocationsMaterialViewerLink(material));
            }
        } else
        {
            container.add(new Text(propertyValue), cellLayout);
        }

    }

    private Widget createEntityExternalLink(Material gene)
    {
        String value = null;
        for (IEntityProperty prop : gene.getProperties())
        {
            if (prop.getPropertyType().getCode().equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
            {
                value = prop.getValue();
            }
        }
        final LayoutContainer container = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        container.setLayout(layout);
        container.setWidth(300);
        container.add(createPlateLocationsMaterialViewerLink(gene));
        LayoutContainer spacer = new LayoutContainer();
        spacer.setWidth(10);
        container.add(spacer);
        container.add(new Text("["));
        // TODO 2010-10-11, Piotr Buczek: change links in normal view mode not to change URL
        if (value != null && StringUtils.isBlank(value) == false)
        {
            String[] symbols = value.split(" ");
            for (int i = 0; i < symbols.length; i++)
            {
                String symbol = symbols[i];
                if (i > 0)
                {
                    container.add(new Text(","));
                }
                String message = viewContext.getMessage(Dict.GENE_LIBRARY_URL, symbol);

                String link = LinkRenderer.renderAsLinkWithAnchor(symbol, message, true);
                container.add(new Html(link));
            }
        } else
        {
            container.add(new Html(LinkRenderer.renderAsLinkWithAnchor("gene database",
                    viewContext.getMessage(Dict.GENE_LIBRARY_SEARCH_URL, gene.getCode()), true)));
        }
        container.add(new Text("]"));
        return container;
    }

    private Widget createPlateLocationsMaterialViewerLink(
            final IEntityInformationHolderWithPermId material)
    {
        final String href =
                ScreeningLinkExtractor.tryExtractMaterialWithExperiment(material,
                        experimentCriteria.getExperimentIdentifier());
        final ClickHandler listener = new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    WellContentDialog.this.hide();
                    ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory
                            .openPlateLocationsMaterialViewer(material,
                                    ExperimentSearchCriteria.createExperiment(experimentCriteria),
                                    viewContext);
                }
            };
        Anchor link = (Anchor) LinkRenderer.getLinkWidget(material.getCode(), listener, href);
        if (viewContext.isSimpleMode())
        {
            link.addClickHandler(new ClickHandler()
                {
                    public void onClick(ClickEvent event)
                    {
                        WellContentDialog.this.hide();
                    }
                });
        }
        return link;
    }

    private Widget createEntityLink(IEntityInformationHolderWithPermId entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

}
