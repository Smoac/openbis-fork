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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImage;
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

    /**
     * A dialog which shows the content of the well (static or a timepoints movie).
     */
    public static void showContentDialog(final WellData wellData,
            ImageDatasetEnrichedReference imageDatasetOrNull,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        final WellContentDialog contentDialog = createContentDialog(wellData, viewContext);
        showContentDialog(contentDialog, imageDatasetOrNull, viewContext);
    }

    private static void showContentDialog(final WellContentDialog contentDialog,
            final ImageDatasetEnrichedReference imagesOrNull,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        if (imagesOrNull != null)
        {
            LogicalImageViewer viewer = contentDialog.createImageViewer(imagesOrNull);
            contentDialog.add(viewer.getViewerWidget());
            contentDialog.addImageEditorLaunchButton(viewer);
        }
        contentDialog.show();
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
     * Creates a view for the specified channels.
     * 
     * @param channels the channel names. If "MERGED_CHANNELS" is specified, there must be no other
     *            channel elements present.
     */
    public static Widget createImageViewerForChannel(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final WellImage wellImage, int imageSizePx, List<String> channels)
    {
        final ImageDatasetEnrichedReference imageDataset = tryGetImageDataset(wellImage);
        if (imageDataset == null)
        {
            return new Text(NO_IMAGE_DATASETS_LABEL);
        }
        WellLocation locationOrNull = wellImage.tryGetLocation();
        if (locationOrNull == null)
        {
            return new Text(INCORRECT_WELL_CODE_LABEL);
        }
        ImageDatasetParameters imageParameters = imageDataset.getImageDatasetParameters();
        if (imageParameters.getChannelsCodes().containsAll(channels) == false
                && channels.contains(ScreeningConstants.MERGED_CHANNELS) == false)
        {
            return new Text(UNKNOWN_CHANNEL_LABEL);
        }

        boolean createImageLinks = (imageParameters.isMultidimensional() == false);
        String sessionId = getSessionId(viewContext);
        final LogicalImageReference wellImages =
                new LogicalImageReference(imageDataset, locationOrNull);
        LogicalImageChannelsReference channelReferences =
                LogicalImageChannelsReference.createWithoutOverlays(wellImages, channels);
        LayoutContainer staticTilesGrid =
                LogicalImageViewer.createTilesGrid(channelReferences, sessionId, imageSizePx,
                        createImageLinks);

        if (imageParameters.isMultidimensional())
        {
            // NOTE: this supresses the action which just shows image magnification
            staticTilesGrid.sinkEvents(Events.OnClick.getEventCode());
            staticTilesGrid.addListener(Events.OnClick, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        showContentDialog(viewContext, wellImage, imageDataset);
                    }
                });
        }

        return staticTilesGrid;
    }

    private static ImageDatasetEnrichedReference tryGetImageDataset(final WellImage wellImage)
    {
        if (wellImage.tryGetImageDataset() != null)
        {
            return new ImageDatasetEnrichedReference(wellImage.tryGetImageDataset());
        } else
        {
            return null;
        }
    }

    private static void showContentDialog(IViewContext<IScreeningClientServiceAsync> viewContext,
            WellImage wellImage, ImageDatasetEnrichedReference imageDatasetOrNull)
    {
        WellContentDialog contentDialog =
                new WellContentDialog(wellImage.getWell(), null, wellImage.tryGetLocation(),
                        getExperiment(wellImage.getExperiment()), viewContext);

        showContentDialog(contentDialog, imageDatasetOrNull, viewContext);
    }

    private LogicalImageViewer createImageViewer(ImageDatasetEnrichedReference imageDatasetOrNull)
    {
        final LogicalImageReference imagesOrNull =
                new LogicalImageReference(imageDatasetOrNull, wellLocationOrNull);
        return new LogicalImageViewer(imagesOrNull, viewContext,
                experimentCriteria.getExperimentIdentifier(),
                experimentCriteria.getExperimentPermId(), false);
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
                experimentReference.getPermId(), experimentReference.getIdentifier());
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
            List<IEntityProperty> wellPropertiesOrNull, final WellLocation wellLocationOrNull,
            final SingleExperimentSearchCriteria experimentCriteria,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
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
        setButtons(CLOSE);
        setHeading(WELL_LABEL + getWellDescription());
        setTopComponent(createContentDescription());

        addListener(Events.Show, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    Rectangle bounds = GuiUtils.calculateBounds(getElement());
                    // Note: neither image widget nor html IMG tag has the width set to maintain the
                    // correct aspect ratio of the images. The current dialog width is computed
                    // before the images are loaded, so we have to fix the width.
                    int preferedWidth = Math.max(800, bounds.width + getFrameWidth());
                    int preferedHeight = bounds.height;

                    int maxWidth = (9 * XDOM.getBody().getOffsetWidth()) / 10;
                    int maxHeight = (9 * XDOM.getBody().getOffsetHeight()) / 10;

                    int w = Math.min(maxWidth, preferedWidth);
                    int h = Math.min(maxHeight, preferedHeight);
                    setSize(w, h);
                    center();
                }
            });
    }

    private void addImageEditorLaunchButton(final LogicalImageViewer viewer)
    {
        if (viewer.isImageEditorEnabled() == false)
        {
            return;
        }
        ButtonBar buttonBar = getButtonBar();
        buttonBar.setAlignment(HorizontalAlignment.LEFT);
        Button launchButton =
                new Button(viewContext.getMessage(Dict.IMAGE_VIEWER_BUTTON),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    viewer.launchImageEditor();
                                    hide();
                                }
                            });
        buttonBar.insert(new FillToolItem(), 0);
        buttonBar.insert(launchButton, 0);
    }

    private String getWellDescription()
    {
        return wellLocationOrNull != null ? PlateUtils
                .translateLocationIntoWellCode(wellLocationOrNull) : "?";
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

    private Widget createEntityExternalLink(Material material)
    {
        final LayoutContainer container = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        container.setLayout(layout);
        container.setWidth(300);
        container.add(createPlateLocationsMaterialViewerLink(material));
        LayoutContainer spacer = new LayoutContainer();
        spacer.setWidth(10);
        container.add(spacer);

        container.add(new Text("["));
        String geneSymbolsOrNull = tryGetGeneSymbols(material);
        if (geneSymbolsOrNull != null && StringUtils.isBlank(geneSymbolsOrNull) == false)
        {
            String[] symbols = geneSymbolsOrNull.split(" ");
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
            container
                    .add(new Html(LinkRenderer.renderAsLinkWithAnchor("gene database", viewContext
                            .getMessage(Dict.GENE_LIBRARY_SEARCH_URL, material.getCode()), true)));
        }
        container.add(new Text("]"));
        return container;
    }

    private static String tryGetGeneSymbols(Material material)
    {
        for (IEntityProperty prop : material.getProperties())
        {
            if (prop.getPropertyType().getCode().equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
            {
                return prop.getValue();
            }
        }
        return null;
    }

    private Widget createPlateLocationsMaterialViewerLink(
            final IEntityInformationHolderWithPermId material)
    {
        final String href =
                ScreeningLinkExtractor.createMaterialDetailsLink(material, getExperimentCriteria());
        final ClickHandler listener = new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    WellContentDialog.this.hide();
                    ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory
                            .openImagingMaterialViewer(material, getExperimentCriteria(),
                                    viewContext);
                }

            };
        Anchor link = (Anchor) LinkRenderer.getLinkWidget(material.getCode(), listener, href);
        if (viewContext.isSimpleOrEmbeddedMode())
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

    private ExperimentSearchCriteria getExperimentCriteria()
    {
        return ExperimentSearchCriteria.createExperiment(experimentCriteria, false);
    }

    private Widget createEntityLink(IEntityInformationHolderWithPermId entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

}
