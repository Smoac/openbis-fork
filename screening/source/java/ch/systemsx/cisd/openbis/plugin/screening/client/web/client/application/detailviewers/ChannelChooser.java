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
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.CheckBoxGroupWithModel.CheckBoxGroupListner;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.ImageDatasetChannel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetOverlayImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

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

    private String imageTransformationCodeOrNull;

    public ChannelChooser(LogicalImageReference basicImage, IChanneledViewerFactory viewerFactory,
            IDefaultChannelState defaultChannelState)
    {
        this.basicImage = basicImage;
        this.viewerFactory = viewerFactory;
        this.imageContainer = new LayoutContainer();

        this.basicChannelCodes =
                getInitialChannelCodes(defaultChannelState, basicImage.getChannelsCodes());
        this.imageTransformationCodeOrNull =
                tryGetInitialImageTransformationCode(defaultChannelState, basicChannelCodes,
                        basicImage.getImagetParameters());
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
                        imageTransformationCodeOrNull, selectedOverlayChannels);
        Widget view = viewerFactory.create(state);
        imageContainer.removeAll();
        imageContainer.add(view);

        imageContainer.layout();
    }

    public void addViewerTo(LayoutContainer container, IViewContext<?> viewContext)
    {
        // overlays
        List<DatasetOverlayImagesReference> overlayDatasets = basicImage.getOverlayDatasets();
        if (overlayDatasets.size() > 0)
        {
            container.add(createOverlayChannelsChooser(overlayDatasets, viewContext));
        }
        // basic channels
        List<String> channels = basicImage.getChannelsCodes();

        if (channels.size() > 1)
        {
            Widget channelChooserWithLabel = createBasicChannelChooser(channels, viewContext);
            container.add(channelChooserWithLabel);
        }
        // images
        container.add(imageContainer);

        refresh();
    }

    private static Map<String, List<DatasetOverlayImagesReference>> groupByAnalysisProcedure(
            List<DatasetOverlayImagesReference> overlayDatasets)
    {
        return GroupByMap.create(overlayDatasets,
                new IGroupKeyExtractor<String, DatasetOverlayImagesReference>()
                    {
                        public String getKey(DatasetOverlayImagesReference dataset)
                        {
                            return dataset.tryGetAnalysisProcedure();
                        }
                    }).getMap();
    }

    private Widget createOverlayChannelsChooser(
            List<DatasetOverlayImagesReference> overlayDatasets, IViewContext<?> viewContext)
    {
        final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap =
                groupByAnalysisProcedure(overlayDatasets);
        if (datasetsByAnalysisProcMap.size() > 1)
        {
            AnalysisProcedures analysisProcedures =
                    new AnalysisProcedures(datasetsByAnalysisProcMap.keySet());

            LayoutContainer chooserPanel = new LayoutContainer();
            chooserPanel.setLayout(new RowLayout());

            final LayoutContainer objectsChooserContainer = new LayoutContainer();

            IAnalysisProcedureSelectionListener selectionListener =
                    createAnalysisProcedureSelectionListener(datasetsByAnalysisProcMap,
                            objectsChooserContainer);
            AnalysisProcedureChooser analysisProcedureChooser =
                    AnalysisProcedureChooser.create(viewContext, analysisProcedures, null,
                            selectionListener);
            chooserPanel.add(analysisProcedureChooser);
            chooserPanel.add(objectsChooserContainer);
            return chooserPanel;
        } else
        {
            return createOverlayChannelsChooserForOneAnalysisProcedure(overlayDatasets);
        }
    }

    private IAnalysisProcedureSelectionListener createAnalysisProcedureSelectionListener(
            final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap,
            final LayoutContainer objectsChooserContainer)
    {
        return new IAnalysisProcedureSelectionListener()
            {
                public void analysisProcedureSelected(AnalysisProcedureCriteria criteria)
                {
                    refreshObjectChooser(criteria, datasetsByAnalysisProcMap,
                            objectsChooserContainer);
                }
            };
    }

    private void refreshObjectChooser(AnalysisProcedureCriteria criteria,
            final Map<String, List<DatasetOverlayImagesReference>> datasetsByAnalysisProcMap,
            final LayoutContainer objectsChooserContainer)
    {
        String analysisProcedureCode = criteria.tryGetAnalysisProcedureCode();
        List<DatasetOverlayImagesReference> overlayDatasetsForOneAnalysisProc =
                datasetsByAnalysisProcMap.get(analysisProcedureCode);

        objectsChooserContainer.removeAll();
        if (overlayDatasetsForOneAnalysisProc != null
                && overlayDatasetsForOneAnalysisProc.size() > 0)
        {
            Widget objectsChooser =
                    createOverlayChannelsChooserForOneAnalysisProcedure(overlayDatasetsForOneAnalysisProc);
            objectsChooserContainer.add(objectsChooser);
        }
        objectsChooserContainer.layout();

        setSelectedOverlayChannels(new HashSet<ImageDatasetChannel>());
    }

    private Widget createOverlayChannelsChooserForOneAnalysisProcedure(
            List<DatasetOverlayImagesReference> overlayDatasets)
    {
        List<LabeledItem<ImageDatasetChannel>> overlayChannelItems =
                createOverlayChannelItems(overlayDatasets);
        CheckBoxGroupWithModel<ImageDatasetChannel> checkBoxGroup =
                new CheckBoxGroupWithModel<ImageDatasetChannel>(overlayChannelItems);
        checkBoxGroup.addListener(new CheckBoxGroupListner<ImageDatasetChannel>()
            {
                public void onChange(Set<ImageDatasetChannel> selected)
                {
                    setSelectedOverlayChannels(selected);
                }
            });
        return GuiUtils.withLabel(checkBoxGroup, OVERLAYS_MSG);
    }

    private void setSelectedOverlayChannels(Set<ImageDatasetChannel> selected)
    {
        selectedOverlayChannels = selected;
        refresh();
    }

    private static List<LabeledItem<ImageDatasetChannel>> createOverlayChannelItems(
            List<? extends DatasetImagesReference> overlayDatasets)
    {
        List<LabeledItem<ImageDatasetChannel>> items =
                new ArrayList<LabeledItem<ImageDatasetChannel>>();
        for (DatasetImagesReference overlayDataset : overlayDatasets)
        {
            ImageDatasetParameters imageParams = overlayDataset.getImageParameters();
            for (int i = 0; i < imageParams.getChannelsNumber(); i++)
            {
                ImageChannel channel = imageParams.getChannels().get(i);
                String channelCode = channel.getCode();
                String channelLabel = channel.getLabel();
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

    private Widget createBasicChannelChooser(List<String> channels, IViewContext<?> viewContext)
    {
        final ChannelChooserPanel channelChooser =
                new ChannelChooserPanel(viewContext, defaultChannelState, channels,
                        basicChannelCodes, basicImage.getImagetParameters());

        channelChooser
                .addSelectionChangedListener(new ChannelChooserPanel.ChannelSelectionListener()
                    {
                        public void selectionChanged(List<String> newlySelectedChannels,
                                @SuppressWarnings("hiding") String imageTransformationCodeOrNull)
                        {
                            basicChannelCodes = newlySelectedChannels;
                            ChannelChooser.this.imageTransformationCodeOrNull =
                                    imageTransformationCodeOrNull;
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

    private static String tryGetInitialImageTransformationCode(
            IDefaultChannelState defaultChannelState, List<String> channels,
            ImageDatasetParameters imageParameters)
    {
        if (imageParameters != null && channels.size() == 1)
        {
            String channel = channels.get(0);
            String initialTransformation = defaultChannelState.tryGetDefaultTransformation(channel);
            if (ChannelChooserPanel.DEFAULT_TRANSFORMATION_CODE.equals(initialTransformation))
            {
                return null;
            }

            String defaultSelection = null;
            List<ImageTransformationInfo> transformations =
                    imageParameters.getAvailableImageTransformationsFor(channel);
            for (ImageTransformationInfo transformation : transformations)
            {
                if (transformation.getCode().equals(initialTransformation))
                {
                    return initialTransformation;
                }
                if (transformation.isDefault() && defaultSelection == null)
                {
                    defaultSelection = transformation.getCode();
                }
            }

            return defaultSelection;
        }

        return null;
    }
}
