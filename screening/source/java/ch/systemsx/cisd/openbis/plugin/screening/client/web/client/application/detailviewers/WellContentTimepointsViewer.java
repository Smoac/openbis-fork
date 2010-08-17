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
import java.util.TreeMap;
import java.util.Map.Entry;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStackReference;

/**
 * Allows to view timepoint images of the well.
 * 
 * @author Tomasz Pylak
 */
class WellContentTimepointsViewer
{

    public static LayoutContainer createTilesGrid(String sessionId,
            List<ImageChannelStackReference> channelStackImages, WellImages images, String channel,
            int imageWidth, int imageHeight)
    {
        Map<Float, List<ImageChannelStackReference>> channelStackImagesByTimepoint =
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

        final Slider slider = createTimepointsSlider(frames.size() - 1, new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent e)
                {
                    int oldValue = e.getOldValue();
                    int newValue = e.getNewValue();
                    frames.get(oldValue).hide();
                    frames.get(newValue).show();
                    mainContainer.remove(mainContainer.getItem(0));
                    mainContainer.insert(new Label(createTimepointLabel(timepoints, newValue)), 0);
                    mainContainer.layout();
                }
            });
        mainContainer.insert(slider, 0);
        mainContainer.insert(new Label(createTimepointLabel(timepoints, 0)), 0);
        slider.setValue(0);

        return mainContainer;
    }

    private static List<LayoutContainer> createTimepointFrames(
            Map<Float, List<ImageChannelStackReference>> channelStackImagesByTimepoint,
            WellImages images, String channel, String sessionId, int imageWidth, int imageHeight)
    {
        final List<LayoutContainer> frames = new ArrayList<LayoutContainer>();
        int counter = 0;
        for (Entry<Float, List<ImageChannelStackReference>> entry : channelStackImagesByTimepoint
                .entrySet())
        {
            List<ImageChannelStackReference> imageReferences = entry.getValue();
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
            List<ImageChannelStackReference> channelStackReferences, WellImages images,
            String channel, String sessionId, int imageWidth, int imageHeight)
    {
        final LayoutContainer container =
                new LayoutContainer(new TableLayout(images.getTileColsNum()));

        ImageChannelStackReference[/* tileRow */][/* tileCol */] tilesMap =
                createTilesMap(channelStackReferences, images);
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                ImageChannelStackReference stackRef = tilesMap[row - 1][col - 1];
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

    private static ImageChannelStackReference[][] createTilesMap(
            List<ImageChannelStackReference> stackReferences, WellImages images)
    {
        int rows = images.getTileRowsNum();
        int cols = images.getTileColsNum();
        ImageChannelStackReference[][] map = new ImageChannelStackReference[rows][cols];
        for (ImageChannelStackReference stackRef : stackReferences)
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
        Float timepoint = timepoints[sequenceNumber];
        int numberOfSequences = timepoints.length - 1;
        return "Timepoint: " + timepoint + "sec (" + sequenceNumber + "/" + numberOfSequences + ")";
    }

    private static Map<Float, List<ImageChannelStackReference>> groupImagesByTimepoint(
            List<ImageChannelStackReference> channelStackImages)
    {
        Map<Float, List<ImageChannelStackReference>> result =
                new TreeMap<Float, List<ImageChannelStackReference>>();
        for (ImageChannelStackReference ref : channelStackImages)
        {
            Float t = ref.tryGetTimepoint();
            List<ImageChannelStackReference> imageReferences = result.get(t);
            if (imageReferences == null)
            {
                imageReferences = new ArrayList<ImageChannelStackReference>();
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
        slider.setMaxValue(maxValue);
        slider.setClickToChange(true);
        slider.addListener(Events.Change, listener);
        return slider;
    }
}
