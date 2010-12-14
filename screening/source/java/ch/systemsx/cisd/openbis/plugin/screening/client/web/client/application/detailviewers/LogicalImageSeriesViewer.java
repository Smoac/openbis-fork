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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

/**
 * Allows to view logical image which has series (e.g. timepoints).
 * 
 * @author Tomasz Pylak
 */
class LogicalImageSeriesViewer
{
    public static LayoutContainer create(String sessionId,
            List<ImageChannelStack> channelStackImages, LogicalImageReference images,
            String channel, int imageWidth, int imageHeight)
    {
        LogicalImageSeriesViewerModel model = new LogicalImageSeriesViewerModel(channelStackImages);
        List<LayoutContainer> frames =
                createTimepointFrames(model.getSortedChannelStackSeriesPoints(), images, channel,
                        sessionId, imageWidth, imageHeight);

        return createMoviePlayer(frames, model.getSortedPoints());
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
        addAll(mainContainer, frames);

        final Slider slider = createTimepointsSlider(frames.size(), new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent e)
                {
                    int oldValue = e.getOldValue();
                    int newValue = e.getNewValue();
                    if (oldValue > 0)
                    {
                        frames.get(oldValue - 1).hide();
                    }
                    frames.get(newValue - 1).show();
                    mainContainer.remove(mainContainer.getItem(0));
                    mainContainer
                            .insert(new Label(createTimepointLabel(sortedPoints, newValue)), 0);
                    mainContainer.layout();
                }
            });
        mainContainer.insert(slider, 0);
        mainContainer.insert(new Label(createTimepointLabel(sortedPoints, 1)), 0);
        slider.setValue(1);

        return mainContainer;
    }

    /**
     * @param sortedChannelStackSeriesPoints - one element on the list are all tiles for a fixed
     *            series point
     */
    private static List<LayoutContainer> createTimepointFrames(
            List<List<ImageChannelStack>> sortedChannelStackSeriesPoints,
            LogicalImageReference images, String channel, String sessionId, int imageWidth,
            int imageHeight)
    {
        final List<LayoutContainer> frames = new ArrayList<LayoutContainer>();
        int counter = 0;
        for (List<ImageChannelStack> seriesPointStacks : sortedChannelStackSeriesPoints)
        {
            final LayoutContainer container =
                    createTilesGridForTimepoint(seriesPointStacks, images, channel, sessionId,
                            imageWidth, imageHeight);
            frames.add(container);
            if (counter > 0)
            {
                container.setVisible(false);
            }
            counter++;
        }
        return frames;
    }

    private static void addAll(LayoutContainer container, List<LayoutContainer> frames)
    {
        for (LayoutContainer frame : frames)
        {
            container.add(frame);
        }
    }

    private static LayoutContainer createTilesGridForTimepoint(
            List<ImageChannelStack> seriesPointStacks, LogicalImageReference images,
            String channel, String sessionId, int imageWidth, int imageHeight)
    {
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
                    ImageUrlUtils.addImageUrlWidget(container, sessionId, images, channel,
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

    private static String createTimepointLabel(List<ImageSeriesPoint> sortedPoints,
            int sequenceNumber)
    {
        ImageSeriesPoint point = sortedPoints.get(sequenceNumber - 1);
        int numberOfSequences = sortedPoints.size();
        return point.getLabel() + " (" + sequenceNumber + "/" + numberOfSequences + ")";
    }

    private static final Slider createTimepointsSlider(int maxValue, Listener<SliderEvent> listener)
    {
        final Slider slider = new Slider();
        slider.setWidth(230);
        slider.setIncrement(1);
        slider.setMinValue(1);
        slider.setMaxValue(maxValue);
        slider.setClickToChange(true);
        slider.addListener(Events.Change, listener);
        return slider;
    }

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
