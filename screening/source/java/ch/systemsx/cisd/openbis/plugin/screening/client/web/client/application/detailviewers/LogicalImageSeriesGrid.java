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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

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
        List<LayoutContainer> frames =
                createSeriesFrames(model.getSortedChannelStackSeriesPoints(), channelReferences,
                        sessionId, imageWidth, imageHeight);

        return createMoviePlayer(frames, model.getSortedPoints());
    }

    private static List<ImageSeriesPoint> sortPoints(Set<ImageSeriesPoint> points)
    {
        ArrayList<ImageSeriesPoint> pointsList = new ArrayList<ImageSeriesPoint>(points);
        Collections.sort(pointsList);
        return pointsList;
    }

    private static LayoutContainer createMoviePlayer(final List<LayoutContainer> frames,
            final List<ImageSeriesPoint> sortedPoints)
    {
        final LayoutContainer mainContainer = new LayoutContainer();

        final Slider slider = createSeriesSlider(frames.size(), new Listener<SliderEvent>()
            {
                private boolean isFirstMove = true;

                public void handleEvent(SliderEvent e)
                {
                    if (isFirstMove)
                    {
                        // The first slider move has been made, so we add all hidden frames to the
                        // DOM. The browser will start fetching images referenced in URLs in the
                        // background.
                        for (int i = 1; i < frames.size(); i++)
                        {
                            mainContainer.add(frames.get(i));
                        }
                        isFirstMove = false;
                    }
                    int oldValue = e.getOldValue();
                    int newValue = e.getNewValue();
                    if (oldValue > 0)
                    {
                        frames.get(oldValue - 1).hide();
                    }
                    frames.get(newValue - 1).show();
                    removeFirstItem(mainContainer);
                    mainContainer.insert(createSeriesPointLabel(sortedPoints, newValue), 0);
                    mainContainer.layout();
                }

            });
        // slider.setValue(1);

        mainContainer.add(createSeriesPointLabel(sortedPoints, 1));
        mainContainer.add(slider);
        // add only first frame to avoid loading images before the slider is touched
        mainContainer.add(frames.get(0));

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
    private static List<LayoutContainer> createSeriesFrames(
            List<List<ImageChannelStack>> sortedChannelStackSeriesPoints,
            LogicalImageChannelsReference channelReferences, String sessionId, int imageWidth,
            int imageHeight)
    {
        final List<LayoutContainer> frames = new ArrayList<LayoutContainer>();
        for (List<ImageChannelStack> seriesPointStacks : sortedChannelStackSeriesPoints)
        {
            final LayoutContainer container =
                    createFrameForSeriesPoint(seriesPointStacks, channelReferences, sessionId,
                            imageWidth, imageHeight);
            boolean isFirstFrame = (frames.size() == 0);
            container.setVisible(isFirstFrame);
            frames.add(container);
        }
        return frames;
    }

    private static LayoutContainer createFrameForSeriesPoint(
            List<ImageChannelStack> seriesPointStacks,
            LogicalImageChannelsReference channelReferences, String sessionId, int imageWidth,
            int imageHeight)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        final LayoutContainer container =
                new LayoutContainer(new TableLayout(images.getTileColsNum()));

        ImageChannelStack[/* tileRow */][/* tileCol */] tilesMap =
                createTilesMap(seriesPointStacks, images);
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                ImageChannelStack stackRef = tilesMap[row - 1][col - 1];
                if (stackRef != null)
                {
                    ImageUrlUtils.addImageUrlWidget(container, sessionId, channelReferences,
                            stackRef, imageWidth, imageHeight);
                } else
                {
                    addDummyImage(container, imageWidth, imageHeight);
                }
            }
        }
        return container;
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

    private static Widget createSeriesPointLabel(List<ImageSeriesPoint> sortedPoints,
            int sequenceNumber)
    {
        ImageSeriesPoint point = sortedPoints.get(sequenceNumber - 1);
        int numberOfSequences = sortedPoints.size();
        String labelText = point.getLabel() + " (" + sequenceNumber + "/" + numberOfSequences + ")";
        return new Label(labelText);
    }

    private static final Slider createSeriesSlider(int maxValue, Listener<SliderEvent> listener)
    {
        final Slider slider = new Slider();
        // we do not want the slider to be long when there are just few points
        slider.setWidth(Math.min(230, maxValue * 10));
        slider.setIncrement(1);
        slider.setMinValue(1);
        slider.setMaxValue(maxValue);
        slider.setClickToChange(true);
        slider.setUseTip(false);
        slider.addListener(Events.Change, listener);
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
            if (tOrNull != null)
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Time: " + tOrNull + " sec";
            }
            if (zOrNull != null)
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Depth: " + zOrNull;
            }
            if (seriesNumberOrNull != null)
            {
                if (desc.length() > 0)
                {
                    desc += ". ";
                }
                desc += "Series: " + seriesNumberOrNull;
            }
            return desc;
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
                            + ((seriesNumberOrNull == null) ? 0 : seriesNumberOrNull.hashCode());
            result = prime * result + ((tOrNull == null) ? 0 : tOrNull.hashCode());
            result = prime * result + ((zOrNull == null) ? 0 : zOrNull.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ImageSeriesPoint other = (ImageSeriesPoint) obj;
            if (seriesNumberOrNull == null)
            {
                if (other.seriesNumberOrNull != null)
                    return false;
            } else if (!seriesNumberOrNull.equals(other.seriesNumberOrNull))
                return false;
            if (tOrNull == null)
            {
                if (other.tOrNull != null)
                    return false;
            } else if (!tOrNull.equals(other.tOrNull))
                return false;
            if (zOrNull == null)
            {
                if (other.zOrNull != null)
                    return false;
            } else if (!zOrNull.equals(other.zOrNull))
                return false;
            return true;
        }
    }

    // private
    static class LogicalImageSeriesViewerModel
    {
        private final List<ImageSeriesPoint> sortedPoints;

        private final List<List<ImageChannelStack>> sortedChannelStackSeriesPoints;

        public LogicalImageSeriesViewerModel(List<ImageChannelStack> channelStackImages)
        {
            Map<ImageSeriesPoint, List<ImageChannelStack>> channelStackImagesBySeries =
                    groupImagesBySeries(channelStackImages);
            this.sortedPoints = sortPoints(channelStackImagesBySeries.keySet());
            this.sortedChannelStackSeriesPoints =
                    getSortedSeries(channelStackImagesBySeries, sortedPoints);
        }

        private static Map<ImageSeriesPoint, List<ImageChannelStack>> groupImagesBySeries(
                List<ImageChannelStack> channelStackImages)
        {
            Map<ImageSeriesPoint, List<ImageChannelStack>> result =
                    new TreeMap<ImageSeriesPoint, List<ImageChannelStack>>();
            for (ImageChannelStack ref : channelStackImages)
            {
                ImageSeriesPoint point = new ImageSeriesPoint(ref);
                List<ImageChannelStack> imageReferences = result.get(point);
                if (imageReferences == null)
                {
                    imageReferences = new ArrayList<ImageChannelStack>();
                    result.put(point, imageReferences);
                }
                imageReferences.add(ref);
            }
            return result;
        }

        private static List<List<ImageChannelStack>> getSortedSeries(
                Map<ImageSeriesPoint, List<ImageChannelStack>> channelStackImagesBySeries,
                List<ImageSeriesPoint> sortedPoints)
        {
            List<List<ImageChannelStack>> sortedSeries = new ArrayList<List<ImageChannelStack>>();
            for (ImageSeriesPoint point : sortedPoints)
            {
                List<ImageChannelStack> series = channelStackImagesBySeries.get(point);
                sortedSeries.add(series);
            }
            return sortedSeries;
        }

        public List<ImageSeriesPoint> getSortedPoints()
        {
            return sortedPoints;
        }

        public List<List<ImageChannelStack>> getSortedChannelStackSeriesPoints()
        {
            return sortedChannelStackSeriesPoints;
        }
    }

}
