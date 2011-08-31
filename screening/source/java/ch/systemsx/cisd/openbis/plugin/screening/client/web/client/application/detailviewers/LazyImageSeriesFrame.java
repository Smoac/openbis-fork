/*
 * Copyright 2011 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.reveregroup.gwt.imagepreloader.FitImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.FitImageLoadHandler;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils.ImageUrlUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * A frame wrapping all tile images for an unique time/depth/series combination. 
 * The tiles are loaded lazily. In order to download and display the tile images one has to call
 * the method {@link #downloadImagesFromServer()}.
 * 
 * @author Kaloyan Enimanev
 */
public class LazyImageSeriesFrame extends LayoutContainer
{
    public interface ImagesDownloadListener
    {
        /**
         * called when all images for a frame have been downloaded.
         */
        void imagesDownloaded(LazyImageSeriesFrame frame);
    }

    private final int imageWidth;

    private final int imageHeight;

    private final String sessionId;

    private final LogicalImageChannelsReference channelReferences;

    private ImageChannelStack[/* tileRow */][/* tileCol */] tilesMap;

    private boolean needsImageDownload;

    private ImagesDownloadListener imagesDownloadListener;

    LazyImageSeriesFrame(List<ImageChannelStack> seriesPointStacks,
            LogicalImageChannelsReference channelReferences, String sessionId, int imageWidth,
            int imageHeight)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        setLayout(new TableLayout(images.getTileColsNum()));
        setHeight(images.getTileRowsNum() * imageHeight);

        tilesMap = createTilesMap(seriesPointStacks, images);
        this.channelReferences = channelReferences;
        this.sessionId = sessionId;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        
        needsImageDownload = areImagesExistingOnServer();
    }

    public boolean needsImageDownload()
    {
        return needsImageDownload;
    }

    public synchronized void downloadImagesFromServer()
    {
        if (false == needsImageDownload)
        {
            // images already download
            return;
        }
        needsImageDownload = false;

        FitImageLoadHandler loadAndErrorhandler = createLoadHandler();
        LogicalImageReference images = channelReferences.getBasicImage();

        for (int row = 0; row < images.getTileRowsNum(); row++)
        {
            for (int col = 0; col < images.getTileColsNum(); col++)
            {
                ImageChannelStack stackRef = tilesMap[row][col];
                if (stackRef != null)
                {
                    ImageUrlUtils.addImageUrlWidget(this, sessionId, channelReferences, stackRef,
                            imageWidth, imageHeight, loadAndErrorhandler);
                } else
                {
                    addDummyImage(this, imageWidth, imageHeight);
                }
            }
        }
    }

    private FitImageLoadHandler createLoadHandler()
    {
        final int totalTilesToDownload = getNumberOfTilesToDownload();
        return new FitImageLoadHandler()
            {
                int tilesDownloaded = 0;

                public void imageLoaded(FitImageLoadEvent event)
                {
                    tilesDownloaded++;
                    if (tilesDownloaded >= totalTilesToDownload && imagesDownloadListener != null)
                    {
                        imagesDownloadListener.imagesDownloaded(LazyImageSeriesFrame.this);
                    }
                }
            };
    }

    private int getNumberOfTilesToDownload()
    {
        int totalImagesToDownload = 0;
        for (ImageChannelStack[] stackRefArray : tilesMap)
        {
            for (ImageChannelStack stackRef : stackRefArray)
            {
                if (stackRef != null)
                {
                    totalImagesToDownload++;
                }
            }
        }
        return totalImagesToDownload;
    }

    private boolean areImagesExistingOnServer()
    {
        for (ImageChannelStack[] stackRefArray : tilesMap)
        {
            for (ImageChannelStack stackRef : stackRefArray)
            {
                if (stackRef != null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static ImageChannelStack[][] createTilesMap(List<ImageChannelStack> stackReferences,
            LogicalImageReference images)
    {
        int rows = images.getTileRowsNum();
        int cols = images.getTileColsNum();
        ImageChannelStack[][] map = new ImageChannelStack[rows][cols];
        for (ImageChannelStack stackRef : stackReferences)
        {
            map[stackRef.getTileRow() - 1][stackRef.getTileCol() - 1] = stackRef;
        }
        return map;
    }

    private static void addDummyImage(LayoutContainer container, int imageWidth, int imageHeight)
    {
        Label dummy = new Label();
        dummy.setWidth(imageWidth);
        dummy.setHeight(imageHeight);
        container.add(dummy);
    }

    public void setImagesDownloadListener(ImagesDownloadListener imagesDownloadListener)
    {
        this.imagesDownloadListener = imagesDownloadListener;
    }

}
