/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.PlateLayouter;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.SimpleModelComboBox.SimpleComboboxItem;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadata;

/**
 * A section of a plate detail view which shows plate's wells and allow to check the content of the
 * well quickly.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayoutSampleSection extends TabContent
{
    // --- GUI messages (to be moved to the dictionary)

    private static final String PLATE_METADATA_REPORT_LABEL = "Plate Metadata Report: ";

    private static final String IMAGES_DATASET_CHOOSER_LABEL = "Images acquired on: ";

    private static final String SINGLE_IMAGE_DATASET_DETAILS_LABEL = "Image acquisition details: ";

    private static final String UNKNOWN_DATASETS_LABEL = "Other data connected to this plate:";

    private static final String SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON = "Advanced";

    private static final String SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON = "Show Report";

    private static final String NO_IMAGES_DATASET_LABEL = "No images data has been acquired.";

    private static final String NO_IMAGE_ANALYSIS_DATASET_LABEL =
            "No image analysis data is available.";

    private static final String IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL = "Images analysis results: ";

    // ----

    private static final String LABEL_WIDTH_PX = "160";

    private static final int DATASET_COMBOBOX_CHOOSER_WIDTH_PX = 350;

    private final ScreeningViewContext viewContext;

    private final TechId sampleId;

    public PlateLayoutSampleSection(final ScreeningViewContext viewContext, final TechId sampleId)
    {
        super("Plate Layout", viewContext, sampleId);
        this.viewContext = viewContext;
        this.sampleId = sampleId;
        setIds(DisplayTypeIDGenerator.PLATE_LAYOUT_SAMPLE_SECTION);
    }

    @Override
    protected void showContent()
    {
        add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getPlateContent(sampleId, createDisplayPlateCallback(viewContext));
    }

    private AsyncCallback<PlateContent> createDisplayPlateCallback(
            final ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<PlateContent>(context)
            {
                @Override
                protected void process(PlateContent plateContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    addPlateVisualisation(plateContent);
                    addPlateMetadataReportLink(plateContent);
                    addUnknownDatasetsLinks(plateContent.getUnknownDatasets());

                    layout();
                }
            };
    }

    private static List<DatasetReference> asFeatureVectorReferences(
            List<FeatureVectorDataset> featureVectorDatasets)
    {
        List<DatasetReference> refs = new ArrayList<DatasetReference>();
        for (FeatureVectorDataset dataset : featureVectorDatasets)
        {
            refs.add(dataset.getDatasetReference());
        }
        return refs;
    }

    private void addUnknownDatasetsLinks(List<DatasetReference> unknownDatasets)
    {
        if (unknownDatasets.isEmpty())
        {
            return;
        }
        LayoutContainer c = new LayoutContainer();
        c.add(new Text(UNKNOWN_DATASETS_LABEL));
        for (DatasetReference dataset : unknownDatasets)
        {
            String label = createUnknownDatasetLabel(dataset);
            Widget detailsLink = createDatasetDetailsLink(dataset, label);
            c.add(detailsLink);
        }
        add(c, PlateLayouter.createRowLayoutSurroundingData());
    }

    private static Widget withLabel(Widget widet, String label)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(2));
        Text labelWidget = new Text(label);
        labelWidget.setWidth(LABEL_WIDTH_PX);
        container.add(labelWidget);
        container.add(widet);
        return container;
    }

    private static <T> SimpleModelComboBox<T> createDatasetChooserComboBox(
            IMessageProvider messageProvider, List<T> items, List<String> labels)
    {
        return new SimpleModelComboBox<T>(messageProvider, items, labels,
                DATASET_COMBOBOX_CHOOSER_WIDTH_PX);
    }

    private static String createDatasetLabel(DatasetReference datasetReference)
    {
        String registrationDate =
                DateRenderer.renderDate(datasetReference.getRegistrationDate(),
                        BasicConstant.DATE_WITHOUT_TIME_FORMAT_PATTERN);
        return registrationDate + ", " + datasetReference.getCode() + " ("
                + datasetReference.getFileTypeCode() + ")";
    }

    private static String createUnknownDatasetLabel(DatasetReference datasetReference)
    {
        return datasetReference.getEntityType().getCode() + ", registered on "
                + createDatasetLabel(datasetReference);
    }

    private void addPlateVisualisation(PlateContent plateContent)
    {
        PlateMetadata plateMetadata = plateContent.getPlateMetadata();
        PlateLayouter plateLayouter = new PlateLayouter(viewContext, plateMetadata);

        List<DatasetImagesReference> imageDatasets = plateContent.getImageDatasets();
        Widget imageDatasetDetailsRow = createImageDatasetDetailsRow(imageDatasets, plateLayouter);
        Widget featureVectorDatasetDatailsRow =
                createFeatureVectorDatasetDetailsRow(plateContent, plateLayouter);

        Widget plateLayout = plateLayouter.getView();

        boolean manyImageDatasets = imageDatasets.size() > 1;
        boolean manyFeatureVectorDatasets = plateContent.getFeatureVectorDatasets().size() > 1;
        layoutComponents(plateLayout, imageDatasetDetailsRow, manyImageDatasets,
                featureVectorDatasetDatailsRow, manyFeatureVectorDatasets);
    }

    private void layoutComponents(Widget plateLayout, Widget imageDatasetDetailsRow,
            boolean manyImageDatasets, Widget featureVectorDatasetDatailsRow,
            boolean manyFeatureVectorDatasets)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        if (manyImageDatasets)
        {
            container.add(imageDatasetDetailsRow);
        }
        if (manyFeatureVectorDatasets)
        {
            container.add(featureVectorDatasetDatailsRow);
        }

        container.add(plateLayout);

        RowData horizontalMargin = PlateLayouter.createRowLayoutHorizontalMargin();
        if (manyImageDatasets == false)
        {
            container.add(imageDatasetDetailsRow, horizontalMargin);
        }
        if (manyFeatureVectorDatasets == false)
        {
            container.add(featureVectorDatasetDatailsRow, horizontalMargin);
        }
        add(container, PlateLayouter.createRowLayoutSurroundingData());
    }

    private Widget createImageDatasetDetailsRow(List<DatasetImagesReference> imageDatasets,
            PlateLayouter plateLayouter)
    {
        if (imageDatasets.size() == 0)
        {
            return new Text(NO_IMAGES_DATASET_LABEL);
        } else if (imageDatasets.size() == 1)
        {
            DatasetImagesReference datasetImagesReference = imageDatasets.get(0);
            return createAndConnectImageDatasetInfo(datasetImagesReference, plateLayouter);
        } else
        {
            return createAndConnectImageDatasetChooser(imageDatasets, plateLayouter);
        }
    }

    private Widget createFeatureVectorDatasetDetailsRow(final PlateContent plateContent,
            PlateLayouter plateLayouter)
    {
        List<FeatureVectorDataset> featureVectorDatasets = plateContent.getFeatureVectorDatasets();
        if (featureVectorDatasets.size() == 0)
        {
            return new Text(NO_IMAGE_ANALYSIS_DATASET_LABEL);
        } else if (featureVectorDatasets.size() == 1)
        {
            FeatureVectorDataset featureVectorDataset = featureVectorDatasets.get(0);
            return createAndConnectFeatureVectorDatasetInfo(featureVectorDataset, plateLayouter);
        } else
        {
            return createAndConnectFeatureVectorDatasetChooser(featureVectorDatasets, plateLayouter);
        }
    }

    private Widget createAndConnectImageDatasetInfo(DatasetImagesReference datasetImagesReference,
            PlateLayouter plateLayouter)
    {
        Widget datasetDetailsButton =
                createDatasetDetailsLink(datasetImagesReference.getDatasetReference(),
                        SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON);
        Widget imageDatasetDetailsRow =
                withLabel(datasetDetailsButton, SINGLE_IMAGE_DATASET_DETAILS_LABEL);
        plateLayouter.changeDisplayedImageDataset(datasetImagesReference);
        return imageDatasetDetailsRow;
    }

    private Widget createAndConnectImageDatasetChooser(List<DatasetImagesReference> imageDatasets,
            final PlateLayouter plateLayouter)
    {
        final SimpleModelComboBox<DatasetImagesReference> datasetChooser =
                createDatasetChooserComboBox(viewContext, imageDatasets,
                        createDatasetLabels(asReferences(imageDatasets)));

        final Anchor datasetDetailsButton = createImageDetailsButton(datasetChooser);
        datasetChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<SimpleComboboxItem<DatasetImagesReference>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<SimpleComboboxItem<DatasetImagesReference>>> se)
                        {
                            DatasetImagesReference chosenDataset =
                                    SimpleModelComboBox.getChosenItem(se);
                            plateLayouter.changeDisplayedImageDataset(chosenDataset);
                            updateImageDatasetSimpleViewModeLink(datasetChooser,
                                    datasetDetailsButton);
                        }
                    });
        DatasetImagesReference chosenDataset = datasetChooser.getChosenItem();
        plateLayouter.changeDisplayedImageDataset(chosenDataset);

        return GuiUtils.renderInRow(withLabel(datasetChooser, IMAGES_DATASET_CHOOSER_LABEL),
                datasetDetailsButton);
    }

    private Widget createAndConnectFeatureVectorDatasetChooser(
            List<FeatureVectorDataset> featureVectorDatasets, final PlateLayouter plateLayouter)
    {
        final SimpleModelComboBox<FeatureVectorDataset> datasetChooser =
                createDatasetChooserComboBox(viewContext, featureVectorDatasets,
                        createDatasetLabels(asFeatureVectorReferences(featureVectorDatasets)));
        final Anchor datasetDetailsButton = createImageAnalysisDetailsButton(datasetChooser);
        datasetChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<SimpleComboboxItem<FeatureVectorDataset>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<SimpleComboboxItem<FeatureVectorDataset>>> se)
                        {
                            FeatureVectorDataset chosenDataset =
                                    SimpleModelComboBox.getChosenItem(se);
                            plateLayouter.changeDisplayedFeatureVectorDataset(chosenDataset);
                            updateFeatureVectorDatasetSimpleViewModeLink(datasetChooser,
                                    datasetDetailsButton);
                        }
                    });
        FeatureVectorDataset chosenDataset = datasetChooser.getChosenItem();
        plateLayouter.changeDisplayedFeatureVectorDataset(chosenDataset);

        return GuiUtils.renderInRow(
                withLabel(datasetChooser, IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL),
                datasetDetailsButton);
    }

    private Widget createAndConnectFeatureVectorDatasetInfo(
            FeatureVectorDataset featureVectorDataset, PlateLayouter plateLayouter)
    {
        plateLayouter.changeDisplayedFeatureVectorDataset(featureVectorDataset);

        Widget datasetDetailsLink =
                createDatasetDetailsLink(featureVectorDataset.getDatasetReference(),
                        SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON);
        return withLabel(datasetDetailsLink, IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL);
    }

    private Anchor createImageAnalysisDetailsButton(
            final SimpleModelComboBox<FeatureVectorDataset> datasetChooser)
    {
        return LinkRenderer.getLinkAnchor(SHOW_CHOSEN_ANALYSIS_DATASET_BUTTON, new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    DatasetReference datasetReference =
                            datasetChooser.getChosenItem().getDatasetReference();
                    openDatasetDetails(datasetReference);
                }
            }, createDatasetSimpleViewModeHref(datasetChooser));
    }

    private Anchor createImageDetailsButton(
            final SimpleModelComboBox<DatasetImagesReference> imageDatasetChooser)
    {
        return LinkRenderer.getLinkAnchor(SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON,
                new ClickHandler()
                    {
                        public void onClick(ClickEvent event)
                        {
                            openDatasetDetails(getChosenDatasetReference(imageDatasetChooser));
                        }
                    }, createImageDatasetSimpleViewModeHref(imageDatasetChooser));
    }

    private void updateImageDatasetSimpleViewModeLink(
            final SimpleModelComboBox<DatasetImagesReference> imageDatasetChooser,
            final Anchor anchor)
    {
        if (viewContext.isSimpleMode())
        {
            anchor.setHref("#" + createImageDatasetSimpleViewModeHref(imageDatasetChooser));
        }
    }

    private void updateFeatureVectorDatasetSimpleViewModeLink(
            final SimpleModelComboBox<FeatureVectorDataset> datasetChooser, final Anchor anchor)
    {
        if (viewContext.isSimpleMode())
        {
            anchor.setHref("#" + createDatasetSimpleViewModeHref(datasetChooser));
        }
    }

    private static String createImageDatasetSimpleViewModeHref(
            final SimpleModelComboBox<DatasetImagesReference> imageDatasetChooser)
    {
        return LinkExtractor.tryExtract(getChosenDatasetReference(imageDatasetChooser));
    }

    private String createDatasetSimpleViewModeHref(
            SimpleModelComboBox<FeatureVectorDataset> datasetChooser)
    {
        return LinkExtractor.tryExtract(datasetChooser.getChosenItem().getDatasetReference());
    }

    private static DatasetReference getChosenDatasetReference(
            final SimpleModelComboBox<DatasetImagesReference> imageDatasetChooser)
    {
        return imageDatasetChooser.getChosenItem().getDatasetReference();
    }

    private Widget createDatasetDetailsLink(final DatasetReference dataset, String label)
    {
        String href = LinkExtractor.tryExtract(dataset);
        assert href != null : "invalid link for " + dataset;
        ClickHandler listener = new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    openDatasetDetails(dataset);
                }
            };
        return LinkRenderer.getLinkWidget(label, listener, href);
    }

    private void openDatasetDetails(DatasetReference selectedDatasetReference)
    {
        new OpenEntityDetailsTabAction(selectedDatasetReference, viewContext).execute();
    }

    private static List<DatasetReference> asReferences(List<DatasetImagesReference> imageDatasets)
    {
        List<DatasetReference> refs = new ArrayList<DatasetReference>();
        for (DatasetImagesReference dataset : imageDatasets)
        {
            refs.add(dataset.getDatasetReference());
        }
        return refs;
    }

    private static List<String> createDatasetLabels(List<DatasetReference> datasetReferences)
    {
        List<String> labels = new ArrayList<String>(datasetReferences.size());
        for (DatasetReference dataset : datasetReferences)
        {
            labels.add(createDatasetLabel(dataset));
        }
        return labels;
    }

    private void addPlateMetadataReportLink(final PlateContent plateContent)
    {
        Sample plate = plateContent.getPlateMetadata().getPlate();
        Widget generateLink = createPlateMetadataLink(plate, viewContext);
        add(withLabel(generateLink, PLATE_METADATA_REPORT_LABEL),
                PlateLayouter.createRowLayoutSurroundingData());
    }

    /** @return a button which shows a grid with the plate metadata */
    private static Widget createPlateMetadataLink(final Sample plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        String plateLinkUrl =
                ScreeningLinkExtractor.createPlateMetadataBrowserLink(plate.getPermId());
        return LinkRenderer.getLinkWidget(viewContext.getMessage(Dict.BUTTON_SHOW),
                new ClickHandler()
                    {
                        public void onClick(ClickEvent event)
                        {
                            PlateMetadataBrowser.openTab(plate, viewContext);
                        }
                    }, plateLinkUrl);
    }
}
