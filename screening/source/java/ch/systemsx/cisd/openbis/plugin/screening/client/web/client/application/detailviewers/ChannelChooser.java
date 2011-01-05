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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Handles displaying images in different channels and allows to choose the overlays.
 * 
 * @author Tomasz Pylak
 */
class ChannelChooser
{
    public static LayoutContainer createViewerWithChannelChooser(
            IChanneledViewerFactory viewerFactory, IDefaultChannelState defaultChannelState,
            LogicalImageReference logicalImageReference)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.setScrollMode(Scroll.AUTO);

        RowData layoutData = new RowData();
        layoutData.setMargins(new Margins(3, 0, 0, 0));
        container.add(new Text(""), layoutData); // separator

        List<String> channels = logicalImageReference.getChannelsCodes();
        if (channels.size() == 0)
        {
            container.add(new Label(NO_IMAGES_AVAILABLE));
            return container;
        }

        String initialChannelCode = getInitialChannelCode(defaultChannelState, channels);
        ChannelChooser channelChooser =
                new ChannelChooser(logicalImageReference, viewerFactory, initialChannelCode);
        channelChooser.addViewer(container, defaultChannelState);

        return container;
    }

    public static interface IChanneledViewerFactory
    {
        Widget create(LogicalImageChannelsReference channelReferences);
    }

    // ---

    private static final String NO_IMAGES_AVAILABLE = "No images available";

    private static final String OVERLAYS_MSG = "Overlays:";

    private static final String CHANNEL_MSG = "Channel:";

    // ---

    private final LogicalImageReference basicImage;

    private final IChanneledViewerFactory viewerFactory;

    private final LayoutContainer imageContainer;

    // --- state

    private Set<ImageDatasetChannel> selectedOverlayChannels;

    private String basicImageChannelCode;

    private ChannelChooser(LogicalImageReference basicImage, IChanneledViewerFactory viewerFactory,
            String initialChannelCode)
    {
        this.basicImage = basicImage;
        this.viewerFactory = viewerFactory;
        this.imageContainer = new LayoutContainer();

        this.basicImageChannelCode = initialChannelCode;
        this.selectedOverlayChannels = new HashSet<ImageDatasetChannel>();
    }

    private void updateState(LayoutContainer container)
    {
        LogicalImageChannelsReference state =
                new LogicalImageChannelsReference(basicImage, basicImageChannelCode,
                        selectedOverlayChannels);
        Widget view = viewerFactory.create(state);
        imageContainer.removeAll();
        imageContainer.add(view);

        container.layout();
    }

    private void addViewer(LayoutContainer container, IDefaultChannelState defaultChannelState)
    {
        // overlays
        List<DatasetImagesReference> overlayDatasets = basicImage.getOverlayDatasets();
        if (overlayDatasets.size() > 0)
        {
            container.add(createOverlayChannelsChooser(overlayDatasets, container));
        }
        // basic channels
        List<String> channels = basicImage.getChannelsCodes();
        if (channels.size() > 1)
        {
            Widget channelChooserWithLabel =
                    createBasicChannelChooser(channels, defaultChannelState, container);
            container.add(channelChooserWithLabel);
        }
        // images
        container.add(imageContainer);

        updateState(container);
    }

    private Widget createOverlayChannelsChooser(List<DatasetImagesReference> overlayDatasets,
            final LayoutContainer container)
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
                    updateState(container);
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

    private Widget createBasicChannelChooser(List<String> channels,
            final IDefaultChannelState defaultChannelState, final LayoutContainer container)
    {
        ChannelComboBox channelChooser =
                new ChannelComboBox(channels, defaultChannelState, basicImageChannelCode);
        channelChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<String>> se)
                        {
                            String selectedChannelCode = se.getSelectedItem().getValue();
                            basicImageChannelCode = selectedChannelCode;
                            updateState(container);
                        }
                    });
        return GuiUtils.withLabel(channelChooser, CHANNEL_MSG);
    }

    private static String getInitialChannelCode(IDefaultChannelState defaultChannelState,
            List<String> channels)
    {
        String initialChannel = defaultChannelState.tryGetDefaultChannel();
        if (initialChannel == null || channels.indexOf(initialChannel) == -1)
        {
            initialChannel =
                    channels.size() > 1 ? ScreeningConstants.MERGED_CHANNELS : channels.get(0);
        }
        return initialChannel;
    }
}
