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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.LazyImageSeriesFrame.ImagesDownloadListener;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * Allows to view logical image which has series (e.g. timepoints).
 * 
 * @author Tomasz Pylak
 */
class LogicalImageSeriesGrid
{
    public static LayoutContainer create(String sessionId,
            List<ImageChannelStack> channelStackImages,
            LogicalImageChannelsReference channelReferences, int imageWidth, int imageHeight)
    {
        LogicalImageSeriesViewerModel model = new LogicalImageSeriesViewerModel(channelStackImages);
        List<List<ImageChannelStack>> sortedChannelStackSeriesPoints = model.getSortedChannelStackSeriesPoints();
        List<LazyImageSeriesFrame> frames =
                createSeriesFrames(sortedChannelStackSeriesPoints, channelReferences,
                        sessionId, imageWidth, imageHeight);
        final LazyImageDownloader imagesDownloader = new LazyImageDownloader(frames);
        final LayoutContainer imageSeriesGrid =
                (model.isMatrixViewPossible()) ? createTimeAndDepthViewer(imagesDownloader, model)
                        : createMoviePlayer(imagesDownloader, model);
        for (LazyImageSeriesFrame frame : frames)
        {
            imageSeriesGrid.add(frame);
        }

        return imageSeriesGrid;
    }
    
    private static LayoutContainer createTimeAndDepthViewer(
            final LazyImageDownloader imageDownloader,
            final LogicalImageSeriesViewerModel model)
    {
        final LayoutContainer mainContainer = createMainContainer(imageDownloader);
        LayoutContainer sliderContainer = new LayoutContainer(new HBoxLayout());
        mainContainer.add(sliderContainer);
        LayoutContainer timeSliderContainer = new LayoutContainer();
        sliderContainer.add(timeSliderContainer);
        final int numberOfTimepoints = model.getNumberOfTimepoints();
        final Label timeSliderLabel = new Label();
        timeSliderContainer.add(timeSliderLabel);
        final Slider timeSlider = createSlider(numberOfTimepoints);
        timeSliderContainer.add(timeSlider);
        Label spacer = new Label();
        spacer.setWidth(80);
        sliderContainer.add(spacer);
        LayoutContainer depthSliderContainer = new LayoutContainer();
        sliderContainer.add(depthSliderContainer);
        final int numberOfDepthLevels = model.getNumberOfDepthLevels();
        final Label depthSliderLabel = new Label();
        depthSliderContainer.add(depthSliderLabel);
        final Slider depthSlider = createSlider(numberOfDepthLevels);
        depthSliderContainer.add(depthSlider);
        Listener<SliderEvent> listener = new Listener<SliderEvent>()
            {
                private int currentFrameIndex;
                
                public void handleEvent(SliderEvent be)
                {
                    int oldIndex = currentFrameIndex;
                    currentFrameIndex =
                            (timeSlider.getValue() - 1) * numberOfDepthLevels
                                    + (depthSlider.getValue() - 1);
                    imageDownloader.frameSelectionChanged(oldIndex, currentFrameIndex);
                    int timeSliderValue = timeSlider.getValue();
                    int depthSliderValue = depthSlider.getValue();
                    setSliderLabels(model, timeSliderLabel, timeSliderValue, depthSliderLabel, depthSliderValue);
                    mainContainer.layout();
                }
            };
        timeSlider.addListener(Events.Change, listener);
        depthSlider.addListener(Events.Change, listener);
        setSliderLabels(model, timeSliderLabel, 1, depthSliderLabel, 1);
        
        return mainContainer;
    }
    
    private static void setSliderLabels(LogicalImageSeriesViewerModel model, Label timeSliderLabel,
            int timeSliderValue, Label depthSliderLabel, int depthSliderValue)
    {
        int numberOfTimepoints = model.getNumberOfTimepoints();
        int numberOfDepthLevels = model.getNumberOfDepthLevels();
        ImageSeriesPoint imageSeriesPoint = model.get(timeSliderValue - 1, depthSliderValue - 1);
        Float time = imageSeriesPoint.getTimePointOrNull();
        timeSliderLabel.setText("Time: " + time + " sec (" + timeSliderValue + "/" + numberOfTimepoints + ")");
        Float depth = imageSeriesPoint.getDepthOrNull();
        depthSliderLabel.setText("Depth: " + depth + " (" + depthSliderValue + "/"
                + numberOfDepthLevels + ")");
    }

    private static LayoutContainer createMoviePlayer(final LazyImageDownloader imageDownloader,
            LogicalImageSeriesViewerModel model)
    {
        final List<ImageSeriesPoint> sortedPoints = model.getSortedPoints();
        final LayoutContainer mainContainer = createMainContainer(imageDownloader);

        Listener<SliderEvent> listener = new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent e)
                {
                    int oldValue = e.getOldValue();
                    int newValue = e.getNewValue();
                    imageDownloader.frameSelectionChanged(oldValue - 1, newValue - 1);
                    removeFirstItem(mainContainer);
                    mainContainer.insert(createSeriesPointLabel(sortedPoints, newValue), 0);
                    mainContainer.layout();
                }

            };
        final Slider slider = createSlider(model.getSortedPoints().size());
        slider.addListener(Events.Change, listener);
        // slider.setValue(1);

        mainContainer.add(createSeriesPointLabel(sortedPoints, 1));
        mainContainer.add(slider);

        return mainContainer;
    }

    private static LayoutContainer createMainContainer(final LazyImageDownloader imageDownloader)
    {
        final LayoutContainer mainContainer = new LayoutContainer()
            {

                @Override
                protected void onUnload()
                {
                    super.onUnload();
                    imageDownloader.stop();
                }
            };
        return mainContainer;
    }

    private static void removeFirstItem(LayoutContainer container)
    {
        container.remove(container.getItem(0));
    }

    /**
     * @param sortedChannelStackSeriesPoints - one element on the list are all tiles for a fixed
     *            series point
     */
    private static List<LazyImageSeriesFrame> createSeriesFrames(
            List<List<ImageChannelStack>> sortedChannelStackSeriesPoints,
            LogicalImageChannelsReference channelReferences, String sessionId, int imageWidth,
            int imageHeight)
    {
        final List<LazyImageSeriesFrame> frames = new ArrayList<LazyImageSeriesFrame>();
        for (List<ImageChannelStack> seriesPointStacks : sortedChannelStackSeriesPoints)
        {
            final LazyImageSeriesFrame frame =
                    new LazyImageSeriesFrame(seriesPointStacks, channelReferences, sessionId,
                            imageWidth, imageHeight);
            boolean isFirstFrame = frames.isEmpty();
            if (isFirstFrame)
            {
                frame.downloadImagesFromServer();
            }
            frame.setVisible(isFirstFrame);
            frames.add(frame);
        }
        return frames;
    }

    private static Widget createSeriesPointLabel(List<ImageSeriesPoint> sortedPoints,
            int sequenceNumber)
    {
        ImageSeriesPoint point = sortedPoints.get(sequenceNumber - 1);
        int numberOfSequences = sortedPoints.size();
        String labelText = point.getLabel() + " (" + sequenceNumber + "/" + numberOfSequences + ")";
        return new Label(labelText);
    }

    private static Slider createSlider(int maxValue)
    {
        final Slider slider = new Slider();
        // we do not want the slider to be long when there are just few points
        slider.setWidth(Math.min(230, Math.max(100, maxValue * 10)));
        slider.setIncrement(1);
        slider.setMinValue(1);
        slider.setMaxValue(maxValue);
        slider.setClickToChange(true);
        slider.setUseTip(false);
        return slider;
    }

    // private
    static class ImageSeriesPoint implements Comparable<ImageSeriesPoint>
    {
        private final Float tOrNull, zOrNull;

        private final Integer seriesNumberOrNull;

        public ImageSeriesPoint(ImageChannelStack stack)
        {
            this.tOrNull = stack.tryGetTimepoint();
            this.zOrNull = stack.tryGetDepth();
            this.seriesNumberOrNull = stack.tryGetSeriesNumber();
        }

        public ImageSeriesPoint(Float tOrNull, Float zOrNull, Integer seriesNumberOrNull)
        {
            this.tOrNull = tOrNull;
            this.zOrNull = zOrNull;
            this.seriesNumberOrNull = seriesNumberOrNull;
        }
        
        public String getLabel()
        {
            String desc = "";
            if (isSeriesNumberPresent())
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Series: " + seriesNumberOrNull;
            }
            if (isTimePointPresent())
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Time: " + tOrNull + " sec";
            }
            if (isDepthPresent())
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Depth: " + zOrNull;
            }
            return desc;
        }

        private boolean isDepthPresent()
        {
            return zOrNull != null;
        }
        
        Float getDepthOrNull()
        {
            return zOrNull;
        }

        private boolean isTimePointPresent()
        {
            return tOrNull != null;
        }
        
        Float getTimePointOrNull()
        {
            return tOrNull;
        }

        private boolean isSeriesNumberPresent()
        {
            return seriesNumberOrNull != null;
        }
        
        Integer getSeriesNumberOrNull()
        {
            return seriesNumberOrNull;
        }

        public int compareTo(ImageSeriesPoint o)
        {
            int cmp;
            cmp = compareNullable(seriesNumberOrNull, o.seriesNumberOrNull);
            if (cmp != 0)
                return cmp;
            cmp = compareNullable(tOrNull, o.tOrNull);
            if (cmp != 0)
                return cmp;
            return compareNullable(zOrNull, o.zOrNull);
        }

        private static <T extends Comparable<T>> int compareNullable(T v1OrNull, T v2OrNull)
        {
            if (v1OrNull == null)
            {
                return v2OrNull == null ? 0 : -1;
            } else
            {
                return v2OrNull == null ? 1 : v1OrNull.compareTo(v2OrNull);
            }
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result =
                    prime * result
                            + (isSeriesNumberPresent() ? seriesNumberOrNull.hashCode() : 0);
            result = prime * result + (isTimePointPresent() ? tOrNull.hashCode() : 0);
            result = prime * result + (isDepthPresent() ? zOrNull.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            ImageSeriesPoint other = (ImageSeriesPoint) obj;
            if (isSeriesNumberPresent() == false)
            {
                if (other.isSeriesNumberPresent())
                {
                    return false;
                }
            } else if (seriesNumberOrNull.equals(other.seriesNumberOrNull) == false)
            {
                return false;
            }
            if (isTimePointPresent() == false)
            {
                if (other.isTimePointPresent())
                {
                    return false;
                }
            } else if (tOrNull.equals(other.tOrNull) == false)
            {
                return false;
            }
            if (isDepthPresent() == false)
            {
                if (other.isDepthPresent())
                {    
                    return false;
                }
            } else if (zOrNull.equals(other.zOrNull) == false)
            {
                return false;
            }
            return true;
        }
    }

    // private
    static class LogicalImageSeriesViewerModel
    {
        private final Map<ImageSeriesPoint, List<ImageChannelStack>> channelStackImagesBySeries;
        
        private final List<ImageSeriesPoint> sortedPoints;
        
        private final int numberOfTimepoints;
        
        private final int numberOfDepthLevels;
        
        private final boolean matrixViewPossible;

        private List<List<ImageSeriesPoint>> matrix;

        public LogicalImageSeriesViewerModel(List<ImageChannelStack> channelStackImages)
        {
            channelStackImagesBySeries = new TreeMap<ImageSeriesPoint, List<ImageChannelStack>>();
            TreeMap<Float/* TimePoint */, Map<Float/* Depth */, ImageSeriesPoint>> timePointDepthMatrix =
                    new TreeMap<Float/* TimePoint */, Map<Float/* Depth */, ImageSeriesPoint>>();
            boolean seriesNumberPresent = false;
            boolean timepointOrDepthNotPresent = false;
            for (ImageChannelStack ref : channelStackImages)
            {
                ImageSeriesPoint point = new ImageSeriesPoint(ref);
                List<ImageChannelStack> imageReferences = channelStackImagesBySeries.get(point);
                if (imageReferences == null)
                {
                    imageReferences = new ArrayList<ImageChannelStack>();
                    channelStackImagesBySeries.put(point, imageReferences);
                }
                imageReferences.add(ref);
                Integer seriesNumberOrNull = ref.tryGetSeriesNumber();
                if (seriesNumberOrNull != null)
                {
                    seriesNumberPresent = true;
                }
                Float timepoint = ref.tryGetTimepoint();
                Float depth = ref.tryGetDepth();
                if (timepoint == null || depth == null)
                {
                    timepointOrDepthNotPresent = true;
                } else
                {
                    Map<Float, ImageSeriesPoint> depthMap = timePointDepthMatrix.get(timepoint);
                    if (depthMap == null)
                    {
                        depthMap = new TreeMap<Float, LogicalImageSeriesGrid.ImageSeriesPoint>();
                        timePointDepthMatrix.put(timepoint, depthMap);
                    }
                    depthMap.put(depth, point);
                }
            }
            sortedPoints = new ArrayList<ImageSeriesPoint>(channelStackImagesBySeries.keySet());
            Collections.sort(sortedPoints);
            numberOfTimepoints = timePointDepthMatrix.size();
            Collection<Map<Float, ImageSeriesPoint>> values = timePointDepthMatrix.values();
            matrix = new ArrayList<List<ImageSeriesPoint>>();
            
            Set<Integer> depthMapSizes = new HashSet<Integer>();
            int depthLevelCount = 0;
            for (Map<Float, ImageSeriesPoint> depthMap : values)
            {
                matrix.add(new ArrayList<ImageSeriesPoint>(depthMap.values()));
                depthLevelCount = Math.max(depthLevelCount, depthMap.size());
                depthMapSizes.add(depthMap.size());
            }
            numberOfDepthLevels = depthLevelCount;
            matrixViewPossible =
                    seriesNumberPresent == false && timepointOrDepthNotPresent == false
                            && depthMapSizes.size() == 1;
        }
        
        public ImageSeriesPoint get(int timeIndex, int depthIndex)
        {
            return matrix.get(timeIndex).get(depthIndex);
        }

        public final int getNumberOfTimepoints()
        {
            return numberOfTimepoints;
        }

        public final int getNumberOfDepthLevels()
        {
            return numberOfDepthLevels;
        }

        public final boolean isMatrixViewPossible()
        {
            return matrixViewPossible;
        }

        public List<ImageSeriesPoint> getSortedPoints()
        {
            return sortedPoints;
        }

        public List<List<ImageChannelStack>> getSortedChannelStackSeriesPoints()
        {
            List<List<ImageChannelStack>> sortedSeries = new ArrayList<List<ImageChannelStack>>();
            for (ImageSeriesPoint point : sortedPoints)
            {
                List<ImageChannelStack> series = channelStackImagesBySeries.get(point);
                sortedSeries.add(series);
            }
            return sortedSeries;
        }
    }
    
    /**
     * 
     * Takes into account the current position of the sliders to optimize the download of images. 
     * Images downloads are grouped together in smaller chunks, which allows us to steer the download 
     * process by following the slider movement.
     * <p>
     * In the beginning a small group of images is prefetched to allow smooth slider movement across the first few frames.

     */
    static class LazyImageDownloader {

        public static int NUM_FRAMES_TO_PREFETCH = 15;

        public static int NUM_FRAMES_IN_DOWLOAD_CHUNK = 5;

        private List<LazyImageSeriesFrame> frames;
        
        private boolean fullDownloadStarted = false;

        private boolean keepDownloading = true;

        private int selectedFrameIndex = -1;

        LazyImageDownloader(List<LazyImageSeriesFrame> frames)
        {
            this.frames = frames;
            prefetchFirstFrames(NUM_FRAMES_TO_PREFETCH);
        }
        
        /**
         * downloads the contents of the first <code>prefetchSize</code> frames.
         */
        private void prefetchFirstFrames(int prefetchSize)
        {
            int numFrames = Math.min(prefetchSize, frames.size());
            for (int i = 0; i < numFrames; i++)
            {
                frames.get(i).downloadImagesFromServer();
            }
        }

        public void stop()
        {
            keepDownloading = false;
        }

        public void frameSelectionChanged(int oldSelectionIndex, int newSelectionIndex)
        {
            if (oldSelectionIndex >= 0)
            {
                frames.get(oldSelectionIndex).hide();
            }

            selectedFrameIndex = newSelectionIndex;
            LazyImageSeriesFrame selectedFrame = frames.get(selectedFrameIndex);
            selectedFrame.show();

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
            
            
            for (LazyImageSeriesFrame frame : framesToDownload) {
                frame.setImagesDownloadListener(downloadListener);
                frame.downloadImagesFromServer();
            }
        }

        private boolean shouldContinueDownloading()
        {
            return keepDownloading && fullDownloadStarted;
        }
    }


}
