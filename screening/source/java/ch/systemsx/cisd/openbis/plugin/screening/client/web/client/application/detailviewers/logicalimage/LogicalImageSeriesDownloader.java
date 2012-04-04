/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame.ImagesDownloadListener;

/**
 * @author pkupczyk
 */
/**
 * Takes into account the current position of the sliders to optimize the download of images. Images
 * downloads are grouped together in smaller chunks, which allows us to steer the download process
 * by following the slider movement.
 * <p>
 * In the beginning a small group of images is prefetched to allow smooth slider movement across the
 * first few frames.
 */
class LogicalImageSeriesDownloader
{

    public static int NUM_FRAMES_TO_PREFETCH = 15;

    public static int NUM_FRAMES_IN_DOWLOAD_CHUNK = 1;

    private List<LazyImageSeriesFrame> frames;

    private boolean fullDownloadStarted;

    private boolean keepDownloading;

    private int selectedFrameIndex = 0;

    private int shownFrameIndex = 0;

    private ImagesDownloadListener imageDownloadListener;

    LogicalImageSeriesDownloader(List<LazyImageSeriesFrame> frames,
            ImagesDownloadListener imagesDownloadListener)
    {
        this.frames = frames;
        this.imageDownloadListener = imagesDownloadListener;
        prefetchFirstFrames(NUM_FRAMES_TO_PREFETCH);
        setInitialState();
    }

    /**
     * downloads the contents of the first <code>prefetchSize</code> frames.
     */
    private void prefetchFirstFrames(int prefetchSize)
    {
        int numFrames = Math.min(prefetchSize, frames.size());

        if (!frames.isEmpty())
        {
            frames.get(0).addImagesDownloadListener(imageDownloadListener);
        }

        for (int i = 0; i < numFrames; i++)
        {
            frames.get(i).downloadImagesFromServer();
        }
    }

    public void setInitialState()
    {
        fullDownloadStarted = false;
        keepDownloading = true;
    }

    public void stop()
    {
        keepDownloading = false;
    }

    public void frameSelectionChanged(int newSelectionIndex, AsyncCallback<Void> callback)
    {
        frameSelectionChanged(selectedFrameIndex, newSelectionIndex, callback);
    }

    public void frameSelectionChanged(final int oldSelectionIndex, final int newSelectionIndex,
            final AsyncCallback<Void> callback)
    {
        final LazyImageSeriesFrame newSelectedFrame = frames.get(newSelectionIndex);

        selectedFrameIndex = newSelectionIndex;

        ImagesDownloadListener listener = new ImagesDownloadListener()
            {
                public void imagesDownloaded(LazyImageSeriesFrame frame)
                {
                    // do not display the frame if selection changed during loading
                    if (newSelectionIndex == selectedFrameIndex)
                    {
                        LazyImageSeriesFrame shownFrame = frames.get(shownFrameIndex);
                        shownFrameIndex = newSelectionIndex;
                        shownFrame.hide();
                        newSelectedFrame.show();
                    }
                    if (callback != null)
                    {
                        callback.onSuccess(null);
                    }
                }
            };

        if (newSelectedFrame.areImagesDownloaded())
        {
            listener.imagesDownloaded(newSelectedFrame);
        } else
        {
            newSelectedFrame.addImagesDownloadListener(listener);
        }

        if (false == fullDownloadStarted)
        {
            fullDownloadStarted = true;
            scheduleDownloadNextChunkOfImages();
        }

    }

    private void scheduleDownloadNextChunkOfImages()
    {
        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                public void execute()
                {
                    downloadNextChunkOfImages();
                }
            });
    }

    private void downloadNextChunkOfImages()
    {
        // prevent race condition by copying the state here
        int pivot = selectedFrameIndex;
        final List<LazyImageSeriesFrame> framesToDownload = new ArrayList<LazyImageSeriesFrame>();

        for (int i = 0; i < frames.size(); i++)
        {
            int pos = (pivot + i) % frames.size();
            final LazyImageSeriesFrame frame = frames.get(pos);
            if (frame.needsImageDownload())
            {
                framesToDownload.add(frame);
            }
            if (framesToDownload.size() == NUM_FRAMES_IN_DOWLOAD_CHUNK)
            {
                break;
            }
        }

        ImagesDownloadListener downloadListener = new ImagesDownloadListener()
            {
                private final List<LazyImageSeriesFrame> localFramesToDownload =
                        new ArrayList<LazyImageSeriesFrame>(framesToDownload);

                public void imagesDownloaded(LazyImageSeriesFrame frame)
                {
                    if (frame.isVisible())
                    {
                        frame.layout(true);
                    }
                    localFramesToDownload.remove(frame);
                    if (localFramesToDownload.isEmpty() && shouldContinueDownloading())
                    {
                        scheduleDownloadNextChunkOfImages();
                    }

                }
            };

        for (LazyImageSeriesFrame frame : framesToDownload)
        {
            frame.addImagesDownloadListener(downloadListener);
            frame.downloadImagesFromServer();
        }
    }

    private boolean shouldContinueDownloading()
    {
        return keepDownloading && fullDownloadStarted;
    }

    public void setImagesDownloadListener(ImagesDownloadListener imageDownloadListener)
    {
        this.imageDownloadListener = imageDownloadListener;
    }

}
