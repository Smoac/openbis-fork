/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ParameterNames;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.ImageUrlUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * A widget which displays one logical image pointed by {@link LogicalImageReference}.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageViewer
{
    private static final String NO_IMAGES_AVAILABLE_MSG = "No images available";

    private static final int ONE_IMAGE_SIZE_PX = 120;

    private static final int CHANNEL_SPLITER_AND_LABEL_HEIGHT_PX = 120;

    private static final int ADJUST_COLORS_AND_REFRESH_BUTTON_WIDTH_PX = 80;

    // ----------------

    private final LogicalImageReference logicalImageReference;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final IDefaultChannelState channelState;

    private final String experimentIdentifier;

    private final boolean showColorAdjustmentButton;

    private String currentlySelectedChannelCode;

    public LogicalImageViewer(LogicalImageReference logicalImageReference,
            IViewContext<IScreeningClientServiceAsync> viewContext, String experimentIdentifier,
            String experimentPermId, boolean showColorAdjustmentButton)
    {
        this.logicalImageReference = logicalImageReference;
        this.viewContext = viewContext;
        this.experimentIdentifier = experimentIdentifier;
        this.channelState = createDefaultChannelState(viewContext, experimentPermId);
        this.showColorAdjustmentButton = showColorAdjustmentButton && isImageEditorEnabled();
    }

    /** Creates a widget which displays a series of images. */
    public Widget getViewerWidget(List<ImageChannelStack> channelStackImages)
    {
        if (logicalImageReference.isMultidimensional())
        {
            return createSeriesImageWidget(channelStackImages);
        } else
        {
            return getStaticImageWidget();
        }
    }

    /**
     * Creates a widget which displays a series of images. If there are image series fetches
     * information about them from the server.
     */
    public Widget getViewerWidget()
    {
        if (logicalImageReference.isMultidimensional())
        {
            return getSeriesImageWidget();
        } else
        {
            return getStaticImageWidget();
        }
    }

    /** Creates a widget which displays a series of images. */
    private Widget getSeriesImageWidget()
    {
        final LayoutContainer container = new LayoutContainer();
        container.add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        container.setLayout(new RowLayout());

        // We have set the height explicitly here because the viewer shows images which have zero
        // height before they are loaded. This prevents us from calculating the reasonable height
        // for the dialog. Later on we set the height to null to avoid cutting images if the
        // calculation was not precise.
        container.setHeight(getSeriesImageWidgetHeight());

        // loads the channel stacks asynchroniously, when done replaces the "Loading..." message
        // with the viewer.
        viewContext.getService().getImageDatasetInfo(logicalImageReference.getDatasetCode(),
                logicalImageReference.getDatastoreCode(),
                logicalImageReference.tryGetWellLocation(),
                new AbstractAsyncCallback<LogicalImageInfo>(viewContext)
                    {
                        @Override
                        protected void process(final LogicalImageInfo imageInfo)
                        {
                            container.removeAll();
                            List<ImageChannelStack> channelStackImages =
                                    imageInfo.getChannelStacks();
                            if (channelStackImages.size() > 0)
                            {
                                container.add(createSeriesImageWidget(channelStackImages));
                                container.setHeight(null);
                            } else
                            {
                                container.add(new Text(NO_IMAGES_AVAILABLE_MSG));
                            }
                            container.layout();
                        }
                    });
        return container;
    }

    private int getSeriesImageWidgetHeight()
    {
        return CHANNEL_SPLITER_AND_LABEL_HEIGHT_PX
                + getImageHeight(ONE_IMAGE_SIZE_PX, logicalImageReference)
                * logicalImageReference.getTileRowsNum();
    }

    private Widget createSeriesImageWidget(final List<ImageChannelStack> channelStackImages)
    {
        final Button adjustColorsButton = createAdjustColorsButton();
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public Widget create(LogicalImageChannelsReference channelReferences)
                {
                    currentlySelectedChannelCode = getSelectedChannelCode(channelReferences);
                    String sessionId = getSessionId(viewContext);
                    setAdjustColorsButtonState(adjustColorsButton,
                            channelReferences.getChannelCodes());
                    int imageWidth = getImageWidth(ONE_IMAGE_SIZE_PX, logicalImageReference);
                    int imageHeight = getImageHeight(ONE_IMAGE_SIZE_PX, logicalImageReference);
                    return LogicalImageSeriesGrid.create(sessionId, channelStackImages,
                            channelReferences, imageWidth, imageHeight);
                }
            };
        return createViewerWithChannelChooser(viewerFactory, adjustColorsButton);
    }

    private LayoutContainer createViewerWithChannelChooser(
            final IChanneledViewerFactory viewerFactory, final Button adjustColorsButton)
    {
        LayoutContainer container = createMainEmptyContainer();
        if (hasNoChannels())
        {
            container.add(new Label(NO_IMAGES_AVAILABLE_MSG));
            return container;
        }

        final ChannelChooser channelChooser =
                new ChannelChooser(logicalImageReference, viewerFactory, channelState);
        channelChooser.addViewerTo(container, viewContext);

        if (showColorAdjustmentButton)
        {
            LayoutContainer buttonToolbar = new LayoutContainer();
            buttonToolbar.setLayout(new ColumnLayout());
            buttonToolbar.add(adjustColorsButton);
            Button refreshButton =
                    new Button(viewContext.getMessage(Dict.BUTTON_REFRESH),
                            new SelectionListener<ButtonEvent>()
                                {
                                    @Override
                                    public void componentSelected(ButtonEvent ce)
                                    {
                                        updateDatasetAndRefresh(channelChooser);
                                    }
                                });
            buttonToolbar.add(refreshButton);

            adjustColorsButton.setWidth(ADJUST_COLORS_AND_REFRESH_BUTTON_WIDTH_PX);
            refreshButton.setWidth(ADJUST_COLORS_AND_REFRESH_BUTTON_WIDTH_PX);

            RowData layoutData = new RowData();
            layoutData.setMargins(new Margins(10, 2, 0, 2));
            container.add(buttonToolbar, layoutData);
        }
        return container;
    }

    private Button createAdjustColorsButton()
    {
        final Button adjustColorsButton =
                new Button(viewContext.getMessage(Dict.IMAGE_VIEWER_BUTTON),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    launchImageEditor();
                                }
                            });
        return adjustColorsButton;
    }

    private boolean hasNoChannels()
    {
        return logicalImageReference.getChannelsCodes().size() == 0;
    }

    private void updateDatasetAndRefresh(final ChannelChooser channelChooser)
    {
        // dataset update is needed because colors adjustment could have been performed
        viewContext.getService().getImageDatasetReference(logicalImageReference.getDatasetCode(),
                logicalImageReference.getDatastoreCode(),
                new AbstractAsyncCallback<ImageDatasetEnrichedReference>(viewContext)
                    {
                        @Override
                        protected void process(ImageDatasetEnrichedReference refreshedDataset)
                        {
                            LogicalImageReference updatedLogicalImageReference =
                                    logicalImageReference.updateDatasets(refreshedDataset);
                            channelChooser.refresh(updatedLogicalImageReference);
                        }
                    });
    }

    private static LayoutContainer createMainEmptyContainer()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(3, 0, 0, 0));
        container.add(new Text(""), layoutData); // separator
        return container;
    }

    /** Creates a widget which displays images which has no series. */
    private Widget getStaticImageWidget()
    {

        final Button adjustColorsButton = createAdjustColorsButton();
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public Widget create(LogicalImageChannelsReference channelReferences)
                {
                    currentlySelectedChannelCode = getSelectedChannelCode(channelReferences);
                    setAdjustColorsButtonState(adjustColorsButton,
                            channelReferences.getChannelCodes());
                    String sessionId = getSessionId(viewContext);
                    return createTilesGrid(channelReferences, sessionId, ONE_IMAGE_SIZE_PX, true);
                }

            };
        return createViewerWithChannelChooser(viewerFactory, adjustColorsButton);
    }

    private static IDefaultChannelState createDefaultChannelState(
            final IViewContext<?> viewContext, final String experimentPermId)
    {
        final ScreeningDisplayTypeIDGenerator wellSearchChannelIdGenerator =
                ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL;
        final String displayTypeID = wellSearchChannelIdGenerator.createID(experimentPermId);

        return new DefaultChannelState(viewContext, displayTypeID);
    }

    /** Launches external image editor for the displayed image in the chosen channel. */
    public void launchImageEditor()
    {
        final URLMethodWithParameters urlParams =
                new URLMethodWithParameters(Constants.IMAGE_VIEWER_LAUNCH_SERVLET_NAME);
        String sessionToken = viewContext.getModel().getSessionContext().getSessionID();
        urlParams.addParameter("session", sessionToken);
        urlParams.addParameter(ParameterNames.SERVER_URL, GWT.getHostPageBaseURL());
        urlParams.addParameter(ParameterNames.EXPERIMENT_ID, experimentIdentifier);

        if (currentlySelectedChannelCode != null)
        {
            urlParams.addParameter(ParameterNames.CHANNEL, currentlySelectedChannelCode);
        }
        WellLocation wellLocation = logicalImageReference.tryGetWellLocation();
        if (wellLocation != null)
        {
            String imagePointer =
                    logicalImageReference.getDatasetCode() + ":" + wellLocation.getRow() + "."
                            + wellLocation.getColumn();
            urlParams.addParameter(ParameterNames.DATA_SET_AND_WELLS, imagePointer);
        } else
        {
            urlParams.addParameter(ParameterNames.DATA_SET_AND_WELLS,
                    logicalImageReference.getDatasetCode());
        }

        Window.open(urlParams.toString(), "_blank", "resizable=yes,scrollbars=yes,dependent=yes");
    }

    public boolean isImageEditorEnabled()
    {
        return "true".equals(viewContext.getPropertyOrNull("image-viewer-enabled"));
    }

    /** Creates a widget with a representative image of the specified logical image. */
    public static LayoutContainer createTilesGrid(LogicalImageChannelsReference channelReferences,
            String sessionId, int imageSizePx, boolean createImageLinks)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        return createTilesGrid(channelReferences, sessionId, getImageWidth(imageSizePx, images),
                getImageHeight(imageSizePx, images), createImageLinks);
    }

    private static LayoutContainer createTilesGrid(LogicalImageChannelsReference channelReferences,
            String sessionId, int logicalImageWidth, int logicalImageHeight,
            boolean createImageLinks)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        LayoutContainer container = new LayoutContainer(new TableLayout(images.getTileColsNum()));
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                ImageUrlUtils.addImageUrlWidget(container, sessionId, channelReferences, row, col,
                        logicalImageWidth, logicalImageHeight, createImageLinks);
            }
        }
        return container;
    }

    private static String getSelectedChannelCode(LogicalImageChannelsReference channelReferences)
    {
        String newChannelCode = null;
        List<String> channelCodes = channelReferences.getChannelCodes();
        if (channelCodes != null && false == channelCodes.isEmpty())
        {
            newChannelCode = channelCodes.get(0);
        }
        return newChannelCode;
    }

    private static void setAdjustColorsButtonState(Button adjustColorsButton,
            List<String> channelCodes)
    {
        boolean enabled = channelCodes != null && channelCodes.size() == 1;
        adjustColorsButton.setEnabled(enabled);

    }

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }

    private static int getImageHeight(int imageSizePx, LogicalImageReference images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (imageSizePx * imageSizeMultiplyFactor);
    }

    private static int getImageWidth(int imageSizePx, LogicalImageReference images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (imageSizePx * imageSizeMultiplyFactor);
    }

    private static float getImageSizeMultiplyFactor(LogicalImageReference images)
    {
        float dim = Math.max(images.getTileRowsNum(), images.getTileColsNum());
        // if there are more than 4 tiles, make them smaller, if there are less, make them bigger
        return 4.0F / dim;
    }
}
