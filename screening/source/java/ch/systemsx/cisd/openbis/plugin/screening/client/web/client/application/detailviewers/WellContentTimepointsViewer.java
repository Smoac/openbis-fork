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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * Allows to view timepoint images of the well.
 * 
 * @author Tomasz Pylak
 */
class WellContentTimepointsViewer
{

    public static LayoutContainer createTilesGrid(String sessionId,
            List<ImageChannelStack> channelStackImages, LogicalImageReference images, String channel,
            int imageWidth, int imageHeight)
    {
        Map<Float, List<ImageChannelStack>> channelStackImagesByTimepoint =
                groupImagesByTimepoint(channelStackImages);

        List<LayoutContainer> frames =
                createTimepointFrames(channelStackImagesByTimepoint, images, channel, sessionId,
                        imageWidth, imageHeight);

        Float[] timepoints = channelStackImagesByTimepoint.keySet().toArray(new Float[0]);
        return createMoviePlayer(frames, timepoints);
    }

    private static LayoutContainer createMoviePlayer(final List<LayoutContainer> frames,
            final Float[] timepoints)
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
                    mainContainer.insert(new Label(createTimepointLabel(timepoints, newValue)), 0);
                    mainContainer.layout();
                }
            });
        mainContainer.insert(slider, 0);
        mainContainer.insert(new Label(createTimepointLabel(timepoints, 1)), 0);
        slider.setValue(1);

        return mainContainer;
    }

    private static List<LayoutContainer> createTimepointFrames(
            Map<Float, List<ImageChannelStack>> channelStackImagesByTimepoint,
            LogicalImageReference images, String channel, String sessionId, int imageWidth, int imageHeight)
    {
        final List<LayoutContainer> frames = new ArrayList<LayoutContainer>();
        int counter = 0;
        for (Entry<Float, List<ImageChannelStack>> entry : channelStackImagesByTimepoint
                .entrySet())
        {
            List<ImageChannelStack> imageReferences = entry.getValue();
            final LayoutContainer container =
                    createTilesGridForTimepoint(imageReferences, images, channel, sessionId,
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
            List<ImageChannelStack> channelStackReferences, LogicalImageReference images, String channel,
            String sessionId, int imageWidth, int imageHeight)
    {
        final LayoutContainer container =
                new LayoutContainer(new TableLayout(images.getTileColsNum()));

        ImageChannelStack[/* tileRow */][/* tileCol */] tilesMap =
                createTilesMap(channelStackReferences, images);
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

    private static ImageChannelStack[][] createTilesMap(
            List<ImageChannelStack> stackReferences, LogicalImageReference images)
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

    private static String createTimepointLabel(Float[] timepoints, int sequenceNumber)
    {
        Float timepoint = timepoints[sequenceNumber - 1];
        int numberOfSequences = timepoints.length;
        return "Timepoint: " + timepoint + "sec (" + sequenceNumber + "/" + numberOfSequences + ")";
    }

    private static Map<Float, List<ImageChannelStack>> groupImagesByTimepoint(
            List<ImageChannelStack> channelStackImages)
    {
        Map<Float, List<ImageChannelStack>> result =
                new TreeMap<Float, List<ImageChannelStack>>();
        for (ImageChannelStack ref : channelStackImages)
        {
            Float t = ref.tryGetTimepoint();
            List<ImageChannelStack> imageReferences = result.get(t);
            if (imageReferences == null)
            {
                imageReferences = new ArrayList<ImageChannelStack>();
                result.put(t, imageReferences);
            }
            imageReferences.add(ref);
        }
        return result;
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
}
