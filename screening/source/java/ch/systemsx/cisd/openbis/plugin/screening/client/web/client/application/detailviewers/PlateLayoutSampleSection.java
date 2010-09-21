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
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
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
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
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
                    addImageAnalysisChooser(plateContent);
                    addPlateMetadataReportLink(plateContent);
                    addUnknownDatasetsLinks(plateContent.getUnknownDatasets());

                    layout();
                }
            };
    }

    private void addImageAnalysisChooser(final PlateContent plateContent)
    {
        Widget analysisPanel;
        List<DatasetReference> analysisDatasets = plateContent.getImageAnalysisDatasets();
        if (analysisDatasets.size() > 1)
        {
            final DatasetChooserComboBox<DatasetReference> datasetChooser =
                    new DatasetChooserComboBox<DatasetReference>(viewContext, analysisDatasets,
                            createDatasetLabels(analysisDatasets));
            final Anchor detailsButton = createImageAnalysisDetailsButton(datasetChooser);
            datasetChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<DatasetChoosableItem<DatasetReference>>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<DatasetChoosableItem<DatasetReference>>> se)
                            {
                                updateDatasetSimpleViewModeLink(datasetChooser, detailsButton);
                            }
                        });
            analysisPanel =
                    GuiUtils.renderInRow(new Text(IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL),
                            datasetChooser, detailsButton);
        } else if (analysisDatasets.size() == 1)
        {
            Widget datasetDetailsLink = createDatasetDetailsLink(analysisDatasets.get(0));
            analysisPanel = withLabel(datasetDetailsLink, IMAGE_ANALYSIS_DATASET_CHOOSER_LABEL);
        } else
        {
            analysisPanel = new Text(NO_IMAGE_ANALYSIS_DATASET_LABEL);
        }
        add(analysisPanel, PlateLayouter.createRowLayoutMarginData());
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
        add(c, PlateLayouter.createRowLayoutMarginData());
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

    private static class DatasetChoosableItem<T>
    {
        private final T item;

        private final String label;

        public DatasetChoosableItem(T item, String label)
        {
            this.item = item;
            this.label = label;
        }

        public T getItem()
        {
            return item;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    private static class DatasetChooserComboBox<T> extends SimpleComboBox<DatasetChoosableItem<T>>
    {
        /**
         * Creates a combobox and selects the first value.
         */
        public DatasetChooserComboBox(IMessageProvider messageProvider, List<T> items,
                String[] labels)
        {
            setTriggerAction(TriggerAction.ALL);
            setAllowBlank(false);
            setEditable(false);
            setEmptyText(messageProvider.getMessage(Dict.COMBO_BOX_CHOOSE));
            setWidth(DATASET_COMBOBOX_CHOOSER_WIDTH_PX);
            int i = 0;
            for (T item : items)
            {
                add(new DatasetChoosableItem<T>(item, labels[i]));
                i++;
            }
            autoselect();
        }

        /**
         * Selects first element if nothing was selected before.
         */
        private void autoselect()
        {
            if (getStore().getModels().size() > 0 && getValue() == null)
            {
                setValue(getStore().getModels().get(0));
            }
        }
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
        LayoutContainer container = new LayoutContainer();
        PlateMetadata plateMetadata = plateContent.getPlateMetadata();
        List<DatasetImagesReference> imageDatasets = plateContent.getImageDatasets();
        RowData margin = PlateLayouter.createRowLayoutMarginData();
        if (imageDatasets.size() == 0)
        {
            container.add(new Text(NO_IMAGES_DATASET_LABEL));
            container.add(PlateLayouter.createVisualization(plateMetadata, viewContext));
        } else
        {
            Widget renderedPlate;
            if (imageDatasets.size() > 1)
            {
                renderedPlate = renderPlateWithManyImageDatasets(imageDatasets, plateMetadata);
            } else
            {
                renderedPlate = renderPlateWithOneImageDataset(imageDatasets.get(0), plateMetadata);
            }
            container.add(renderedPlate);
        }

        add(container, margin);
    }

    private Widget renderPlateWithOneImageDataset(DatasetImagesReference datasetImagesReference,
            PlateMetadata plateMetadata)
    {
        LayoutContainer container = new LayoutContainer();
        PlateImages plateImages = new PlateImages(plateMetadata, datasetImagesReference);
        container.add(PlateLayouter.createVisualization(plateImages, viewContext));

        Widget datasetDetailsButton =
                createDatasetDetailsLink(datasetImagesReference.getDatasetReference(),
                        SHOW_CHOSEN_IMAGE_DATASET_DETAILS_BUTTON);
        container.add(withLabel(datasetDetailsButton, SINGLE_IMAGE_DATASET_DETAILS_LABEL));

        return container;
    }

    private LayoutContainer renderPlateWithManyImageDatasets(
            List<DatasetImagesReference> imageDatasets, final PlateMetadata plateMetadata)
    {
        final DatasetChooserComboBox<DatasetImagesReference> imageDatasetChooser =
                new DatasetChooserComboBox<DatasetImagesReference>(viewContext, imageDatasets,
                        createDatasetLabels(asReferences(imageDatasets)));

        DatasetImagesReference chosenImageDataset = getChosenDataset(imageDatasetChooser);
        final PlateLayouter plateLayouter =
                new PlateLayouter(viewContext, plateMetadata, chosenImageDataset);

        final Anchor imageDetailsButton = createImageDetailsButton(imageDatasetChooser);
        imageDatasetChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<DatasetChoosableItem<DatasetImagesReference>>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<DatasetChoosableItem<DatasetImagesReference>>> se)
                        {
                            DatasetImagesReference imageDataset =
                                    se.getSelectedItem().getValue().getItem();
                            plateLayouter.changeDisplayedImageDataset(imageDataset);
                            updateImageDatasetSimpleViewModeLink(imageDatasetChooser,
                                    imageDetailsButton);
                        }
                    });

        Widget plateLayout = plateLayouter.renderVisualizationWidget();

        Widget chooserRow =
                GuiUtils.renderInRow(new Text(IMAGES_DATASET_CHOOSER_LABEL), imageDatasetChooser,
                        imageDetailsButton);
        LayoutContainer layoutWithToolbar = layoutWithToolbar(plateLayout, chooserRow);
        return layoutWithToolbar;
    }

    private static <T> T getChosenDataset(DatasetChooserComboBox<T> imageDatasetChooser)
    {
        return imageDatasetChooser.getSimpleValue().getItem();
    }

    private Anchor createImageAnalysisDetailsButton(
            final DatasetChooserComboBox<DatasetReference> datasetChooser)
    {
        return LinkRenderer.getLinkAnchor(viewContext.getMessage(Dict.BUTTON_SHOW),
                new ClickHandler()
                    {
                        public void onClick(ClickEvent event)
                        {
                            openDatasetDetails(getChosenDataset(datasetChooser));
                        }
                    }, createDatasetSimpleViewModeHref(datasetChooser));
    }

    private Anchor createImageDetailsButton(
            final DatasetChooserComboBox<DatasetImagesReference> imageDatasetChooser)
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
            final DatasetChooserComboBox<DatasetImagesReference> imageDatasetChooser,
            final Anchor anchor)
    {
        if (ClientStaticState.isSimpleMode())
        {
            anchor.setHref("#" + createImageDatasetSimpleViewModeHref(imageDatasetChooser));
        }
    }

    private void updateDatasetSimpleViewModeLink(
            final DatasetChooserComboBox<DatasetReference> datasetChooser, final Anchor anchor)
    {
        if (ClientStaticState.isSimpleMode())
        {
            anchor.setHref("#" + createDatasetSimpleViewModeHref(datasetChooser));
        }
    }

    private static String createImageDatasetSimpleViewModeHref(
            final DatasetChooserComboBox<DatasetImagesReference> imageDatasetChooser)
    {
        return LinkExtractor.tryExtract(getChosenDatasetReference(imageDatasetChooser));
    }

    private String createDatasetSimpleViewModeHref(
            DatasetChooserComboBox<DatasetReference> datasetChooser)
    {
        return LinkExtractor.tryExtract(getChosenDataset(datasetChooser));
    }

    private static DatasetReference getChosenDatasetReference(
            final DatasetChooserComboBox<DatasetImagesReference> imageDatasetChooser)
    {
        return getChosenDataset(imageDatasetChooser).getDatasetReference();
    }

    private Widget createDatasetDetailsLink(final DatasetReference dataset)
    {
        return createDatasetDetailsLink(dataset, viewContext.getMessage(Dict.BUTTON_SHOW));
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

    private static LayoutContainer layoutWithToolbar(Widget mainComponent, Widget toolbar)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolbar);
        container.add(mainComponent);
        return container;
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

    private static String[] createDatasetLabels(List<DatasetReference> datasetReferences)
    {
        String[] labels = new String[datasetReferences.size()];
        int i = 0;
        for (DatasetReference dataset : datasetReferences)
        {
            labels[i] = createDatasetLabel(dataset);
            i++;
        }
        return labels;
    }

    private void addPlateMetadataReportLink(final PlateContent plateContent)
    {
        Sample plate = plateContent.getPlateMetadata().getPlate();
        Widget generateLink = createPlateMetadataLink(plate, viewContext);
        add(withLabel(generateLink, PLATE_METADATA_REPORT_LABEL),
                PlateLayouter.createRowLayoutMarginData());
    }

    /** @return a button which shows a grid with the plate metadata */
    private static Widget createPlateMetadataLink(final Sample plate,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return LinkRenderer.getLinkWidget(viewContext.getMessage(Dict.BUTTON_SHOW),
                new ClickHandler()
                    {
                        public void onClick(ClickEvent event)
                        {
                            DispatcherHelper.dispatchNaviEvent(createPlateMetadataTabFactory());
                        }

                        private AbstractTabItemFactory createPlateMetadataTabFactory()
                        {
                            return new AbstractTabItemFactory()
                                {
                                    @Override
                                    public ITabItem create()
                                    {
                                        return DefaultTabItem.create(getTabTitle(),
                                                PlateMetadataBrowser.create(viewContext,
                                                        new TechId(plate.getId())), viewContext);
                                    }

                                    @Override
                                    public String getId()
                                    {
                                        return GenericConstants.ID_PREFIX + "plate-metadata-"
                                                + plate.getId();
                                    }

                                    @Override
                                    public HelpPageIdentifier getHelpPageIdentifier()
                                    {
                                        return new HelpPageIdentifier(HelpPageDomain.SAMPLE,
                                                HelpPageAction.VIEW);
                                    }

                                    @Override
                                    public String getTabTitle()
                                    {
                                        return "Plate Report: " + plate.getCode();
                                    }
                                };
                        }
                    });
    }
}
