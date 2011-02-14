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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel.CheckBoxGroupListner;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.ImageDatasetChannel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;

/**
 * Handles displaying images in different channels and allows to choose the overlays.
 * 
 * @author Tomasz Pylak
 */
class ChannelChooser
{

    public static interface IChanneledViewerFactory
    {
        Widget create(LogicalImageChannelsReference channelReferences);
    }

    // ---

    private static final String OVERLAYS_MSG = "Overlays:";

    private static final String CHANNEL_MSG = "Channel:";

    // ---

    private final IChanneledViewerFactory viewerFactory;

    private final IDefaultChannelState defaultChannelState;

    private final LayoutContainer imageContainer;

    // --- state

    private LogicalImageReference basicImage;

    private Set<ImageDatasetChannel> selectedOverlayChannels;

    private List<String> basicChannelCodes;

    public ChannelChooser(LogicalImageReference basicImage, IChanneledViewerFactory viewerFactory,
            IDefaultChannelState defaultChannelState)
    {
        this.basicImage = basicImage;
        this.viewerFactory = viewerFactory;
        this.imageContainer = new LayoutContainer();

        this.basicChannelCodes =
                getInitialChannelCodes(defaultChannelState, basicImage.getChannelsCodes());
        this.defaultChannelState = defaultChannelState;
        this.selectedOverlayChannels = new HashSet<ImageDatasetChannel>();
    }

    /** Refreshes the displayed images, but not the rest of the GUI */
    public void refresh(LogicalImageReference updatedBasicImage)
    {
        this.basicImage = updatedBasicImage;
        refresh();
    }

    /** Refreshes the displayed images, but not the rest of the GUI */
    public void refresh()
    {
        LogicalImageChannelsReference state =
                new LogicalImageChannelsReference(basicImage, basicChannelCodes,
                        selectedOverlayChannels);
        Widget view = viewerFactory.create(state);
        imageContainer.removeAll();
        imageContainer.add(view);

        imageContainer.layout();
    }

    public void addViewerTo(LayoutContainer container)
    {
        // overlays
        List<DatasetImagesReference> overlayDatasets = basicImage.getOverlayDatasets();
        if (overlayDatasets.size() > 0)
        {
            container.add(createOverlayChannelsChooser(overlayDatasets));
        }
        // basic channels
        List<String> channels = basicImage.getChannelsCodes();
        if (channels.size() > 1)
        {
            Widget channelChooserWithLabel = createBasicChannelChooser(channels);
            container.add(channelChooserWithLabel);
        }
        // images
        container.add(imageContainer);

        refresh();
    }

    private Widget createOverlayChannelsChooser(List<DatasetImagesReference> overlayDatasets)
    {
        List<LabeledItem<ImageDatasetChannel>> overlayChannelItems =
                createOverlayChannelItems(overlayDatasets);
        CheckBoxGroupWithModel<ImageDatasetChannel> checkBoxGroup =
                new CheckBoxGroupWithModel<ImageDatasetChannel>(overlayChannelItems);
        checkBoxGroup.addListener(new CheckBoxGroupListner<ImageDatasetChannel>()
            {
                public void onChange(Set<ImageDatasetChannel> selected)
                {
                    selectedOverlayChannels = selected;
                    refresh();
                }
            });
        return GuiUtils.withLabel(checkBoxGroup, OVERLAYS_MSG);
    }

    private static List<LabeledItem<ImageDatasetChannel>> createOverlayChannelItems(
            List<DatasetImagesReference> overlayDatasets)
    {
        List<LabeledItem<ImageDatasetChannel>> items =
                new ArrayList<LabeledItem<ImageDatasetChannel>>();
        for (DatasetImagesReference overlayDataset : overlayDatasets)
        {
            ImageDatasetParameters imageParams = overlayDataset.getImageParameters();
            List<String> channelsCodes = imageParams.getChannelsCodes();
            List<String> channelsLabels = imageParams.getChannelsLabels();
            for (int i = 0; i < imageParams.getChannelsNumber(); i++)
            {
                String channelCode = channelsCodes.get(i);
                String channelLabel = channelsLabels.get(i);
                LabeledItem<ImageDatasetChannel> item =
                        createLabeledItem(overlayDataset, channelCode, channelLabel);
                items.add(item);
            }
        }
        return items;
    }

    private static LabeledItem<ImageDatasetChannel> createLabeledItem(
            DatasetImagesReference overlayDataset, String channelCode, String channelLabel)
    {
        ImageDatasetChannel overlayChannel = createImageDatasetChannel(overlayDataset, channelCode);
        return new LabeledItem<ImageDatasetChannel>(overlayChannel, channelLabel);
    }

    private static ImageDatasetChannel createImageDatasetChannel(DatasetImagesReference dataset,
            String channelCode)
    {
        return new ImageDatasetChannel(dataset.getDatasetCode(), dataset.getDatastoreHostUrl(),
                channelCode);
    }

    private Widget createBasicChannelChooser(List<String> channels)
    {
        final ChannelChooserPanel channelChooser =
                new ChannelChooserPanel(defaultChannelState, channels, basicChannelCodes);

        channelChooser
                .setSelectionChangedListener(new ChannelChooserPanel.ChannelSelectionListener()
                    {
                        public void selectionChanged(List<String> newlySelectedChannels)
                        {
                            basicChannelCodes = newlySelectedChannels;
                            refresh();
                        }
                    });

        return GuiUtils.withLabel(channelChooser, CHANNEL_MSG);
    }

    private static List<String> getInitialChannelCodes(IDefaultChannelState defaultChannelState,
            List<String> channels)
    {
        List<String> defaultChannels = defaultChannelState.tryGetDefaultChannels();
        if (defaultChannels == null || false == channels.containsAll(defaultChannels))
        {
            return channels;
        }
        return defaultChannels;
    }

}
