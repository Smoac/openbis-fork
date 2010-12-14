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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
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
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
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

    private static final int ONE_IMAGE_WIDTH_PX = 200;

    private static final int ONE_IMAGE_HEIGHT_PX = 120;

    // ----------------

    private final LogicalImageReference logicalImageReference;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final IDefaultChannelState channelState;

    private final String experimentIdentifier;
    
    private String currentlySelectedChannelCode;

    public LogicalImageViewer(LogicalImageReference logicalImageReference,
            IViewContext<IScreeningClientServiceAsync> viewContext, String experimentIdentifier,
            String experimentPermId)
    {
        this.logicalImageReference = logicalImageReference;
        this.viewContext = viewContext;
        this.experimentIdentifier = experimentIdentifier;
        this.channelState = createDefaultChannelState(viewContext, experimentPermId);
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
                            } else
                            {
                                container.add(new Text(NO_IMAGES_AVAILABLE_MSG));
                            }
                        }
                    });
        return container;
    }

    private Widget createSeriesImageWidget(final List<ImageChannelStack> channelStackImages)
    {
        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(String channel)
                {
                    currentlySelectedChannelCode = channel;
                    String sessionId = getSessionId(viewContext);
                    int imageWidth = getImageWidth(logicalImageReference);
                    int imageHeight = getImageHeight(logicalImageReference);
                    return LogicalImageSeriesViewer.create(sessionId, channelStackImages,
                            logicalImageReference, channel, imageWidth, imageHeight);
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState,
                logicalImageReference.getChannelsCodes());
    }

    /** Creates a widget which displays images which has no series. */
    private Widget getStaticImageWidget()
    {

        final IChanneledViewerFactory viewerFactory = new IChanneledViewerFactory()
            {
                public LayoutContainer create(String channel)
                {
                    currentlySelectedChannelCode = channel;
                    String sessionId = getSessionId(viewContext);
                    return createTilesGrid(logicalImageReference, channel, sessionId);
                }
            };
        return ChannelChooser.createViewerWithChannelChooser(viewerFactory, channelState,
                logicalImageReference.getChannelsCodes());
    }

    protected void setCurrentChannel(String channel)
    {
        this.currentlySelectedChannelCode = channel;
    }

    private static IDefaultChannelState createDefaultChannelState(
            final IViewContext<?> viewContext, final String experimentPermId)
    {
        final ScreeningDisplaySettingsManager screeningDisplaySettingManager =
                ScreeningViewContext.getTechnologySpecificDisplaySettingsManager(viewContext);
        final ScreeningDisplayTypeIDGenerator wellSearchChannelIdGenerator =
                ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL;
        final String displayTypeID = wellSearchChannelIdGenerator.createID(experimentPermId);

        return new IDefaultChannelState()
            {
                public void setDefaultChannel(String channel)
                {
                    screeningDisplaySettingManager.setDefaultChannel(displayTypeID, channel);
                }

                public String tryGetDefaultChannel()
                {
                    return screeningDisplaySettingManager.tryGetDefaultChannel(displayTypeID);
                }
            };
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
            urlParams
                    .addParameter(ParameterNames.DATA_SETS, logicalImageReference.getDatasetCode());
        }

        Window.open(urlParams.toString(), "_blank", "resizable=yes,scrollbars=yes,dependent=yes");
    }

    private static LayoutContainer createTilesGrid(final LogicalImageReference images,
            String channel, String sessionId)
    {
        return createRepresentativeImage(images, channel, sessionId, getImageWidth(images),
                getImageHeight(images), true);
    }

    /** Creates a widget with a representative image of the specified logical image. */
    // TODO 2010-12-10, Tomasz Pylak: implement me!
    public static LayoutContainer createRepresentativeImage(LogicalImageReference images,
            String channel, String sessionId, int imageWidth, int imageHeight,
            boolean createImageLinks)
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

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }

    private static int getImageHeight(LogicalImageReference images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (ONE_IMAGE_HEIGHT_PX * imageSizeMultiplyFactor);
    }

    private static int getImageWidth(LogicalImageReference images)
    {
        float imageSizeMultiplyFactor = getImageSizeMultiplyFactor(images);
        return (int) (ONE_IMAGE_WIDTH_PX * imageSizeMultiplyFactor);
    }

    private static float getImageSizeMultiplyFactor(LogicalImageReference images)
    {
        float dim = Math.max(images.getTileRowsNum(), images.getTileColsNum());
        // if there are more than 3 tiles, make them smaller, if there are less, make them bigger
        return 4.0F / dim;
    }
}
