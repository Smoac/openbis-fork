/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelWidgetWithListener.ISimpleChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;

/**
 * A viewer that comprises several UI elements to produce a holistic UI for material replica feature
 * summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryViewer
{
    private static final String LOADING_IMAGES_DICT_MSG = "Loading images...";

    private static final String REPLICATE_ABBREV_DICT_MSG = "repl.";

    private static final String NO_IMAGES_AVAILABLE_DICT_MSG = "No images available.";

    private static final String MATERIAL_ID_DICT_MSG = "Id";

    private static final int ONE_IMAGE_SIZE_FACTOR_PX = 60;

    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, MaterialIdentifier materialIdentifier)
    {
        MaterialReplicaFeatureSummaryViewer viewer =
                new MaterialReplicaFeatureSummaryViewer(screeningViewContext);
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId, viewer.createExperimentFoundCallback(materialIdentifier));
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private MaterialReplicaFeatureSummaryViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        this.screeningViewContext = screeningViewContext;
    }

    private ExperimentFoundCallback createExperimentFoundCallback(
            MaterialIdentifier materialIdentifier)
    {
        return new ExperimentFoundCallback(materialIdentifier);
    }

    private class ExperimentFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final MaterialIdentifier materialIdentifier;

        ExperimentFoundCallback(MaterialIdentifier materialIdentifier)
        {
            super(screeningViewContext);
            this.materialIdentifier = materialIdentifier;
        }

        @Override
        protected void process(IEntityInformationHolderWithPermId experiment)
        {
            screeningViewContext.getCommonService().getExperimentInfo(new TechId(experiment),
                    new AbstractAsyncCallback<Experiment>(screeningViewContext)
                        {
                            @Override
                            protected void process(Experiment result)
                            {
                                viewContext.getCommonService().getMaterialInformationHolder(
                                        materialIdentifier, new MaterialFoundCallback(result));
                            }
                        });
        }
    }

    private class MaterialFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final Experiment experiment;

        MaterialFoundCallback(Experiment experiment)
        {
            super(screeningViewContext);
            this.experiment = experiment;
        }

        @Override
        protected void process(IEntityInformationHolderWithPermId material)
        {
            screeningViewContext.getService().getMaterialInfo(new TechId(material),
                    new AbstractAsyncCallback<Material>(screeningViewContext)
                        {
                            @Override
                            protected void process(Material result)
                            {
                                AbstractTabItemFactory factory =
                                        createTabFactory(experiment, result);
                                DispatcherHelper.dispatchNaviEvent(factory);
                            }
                        });
        }
    }

    private AbstractTabItemFactory createTabFactory(final Experiment experiment,
            final Material material)
    {
        return new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.MATERIAL_REPLICA_SUMMARY_ACTION
                            + experiment.getCode() + material.getPermId();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent = createViewer(experiment, material);
                    return DefaultTabItem.create(getTabTitle(), tabComponent, screeningViewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.createMaterialReplicaSummaryLink(experiment
                            .getPermId(), material.getCode(), material.getEntityType().getCode());
                }

                @Override
                public String getTabTitle()
                {
                    return getMaterialName(material) + " in " + experiment.getCode() + " Summary";
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

            };
    }

    private class ImagesFoundCallback extends AbstractAsyncCallback<List<WellReplicaImage>>
    {
        private final LayoutContainer imagesPanel;

        ImagesFoundCallback(LayoutContainer imagesPanel)
        {
            super(screeningViewContext);
            this.imagesPanel = imagesPanel;
        }

        @Override
        protected void process(List<WellReplicaImage> images)
        {
            imagesPanel.removeAll();
            imagesPanel.add(createImagePanel(images));
            imagesPanel.layout();
        }
    }

    private Widget createImagePanel(List<WellReplicaImage> images)
    {
        if (images.isEmpty())
        {
            return new Text(NO_IMAGES_AVAILABLE_DICT_MSG);
        }
        String displayTypeId = ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL.createID(null);
        final IDefaultChannelState defaultChannelState =
                new DefaultChannelState(screeningViewContext, displayTypeId);
        ChannelChooserPanel channelChooser = new ChannelChooserPanel(defaultChannelState);
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());
        panel.add(new Html("<br>"), new RowData(1, -1));
        panel.add(channelChooser);

        Map<String, List<WellReplicaImage>> labelToReplicasMap = createSortedImageMap(images);
        String orphanGroupKey = null;
        List<WellReplicaImage> orphanTechnicalReplicates = labelToReplicasMap.get(orphanGroupKey);
        if (orphanTechnicalReplicates != null)
        {
            panel.add(createOrphanTechnicalReplicatesPanel(orphanTechnicalReplicates,
                    channelChooser));
            labelToReplicasMap.remove(orphanGroupKey);
        }
        panel.add(createBiologicalReplicatesImagesPanel(labelToReplicasMap, channelChooser));
        ensureAllImagesVisible(panel);
        return panel;
    }

    // WORKAROUND: in normal mode the height of menu and tab is not taken into account,
    // so we add empty space to make
    private void ensureAllImagesVisible(LayoutContainer panel)
    {
        if (screeningViewContext.isSimpleOrEmbeddedMode() == false)
        {
            Text box = new Text();
            box.setHeight(100);
            panel.add(box);
        }
    }

    private Widget createOrphanTechnicalReplicatesPanel(
            List<WellReplicaImage> orphanTechnicalReplicates, ChannelChooserPanel channelChooser)
    {
        LayoutContainer imagePanel = new LayoutContainer();
        for (WellReplicaImage image : orphanTechnicalReplicates)
        {
            LayoutContainer imageWithLabel = new LayoutContainer();
            imageWithLabel.setLayout(new RowLayout());

            Widget label =
                    createTechnicalReplicateLabel(image.getTechnicalReplicateSequenceNumber());
            imageWithLabel.add(label);

            Widget imageViewer = createImageViewer(image.getWellImage(), channelChooser);
            imageWithLabel.add(imageViewer);

            imagePanel.add(imageWithLabel);
        }
        return imagePanel;
    }

    private LayoutContainer createBiologicalReplicatesImagesPanel(
            Map<String, List<WellReplicaImage>> labelToReplicasMap,
            ChannelChooserPanel channelChooser)
    {
        int maxReplicaNumber = calcMaxReplicaNumber(labelToReplicasMap);
        LayoutContainer imagePanel = new LayoutContainer();
        TableLayout layout = new TableLayout(maxReplicaNumber + 1);
        layout.setBorder(1);
        layout.setCellPadding(5);
        imagePanel.setLayout(layout);

        addImageTableHeader(maxReplicaNumber, imagePanel);
        List<String> sortedLabels = sortCopy(labelToReplicasMap.keySet());
        for (String label : sortedLabels)
        {
            List<WellReplicaImage> sortedTechnicalReplicates = labelToReplicasMap.get(label);

            imagePanel.add(new Text(label));
            for (int i = 0; i < maxReplicaNumber; i++)
            {
                if (i < sortedTechnicalReplicates.size())
                {
                    WellImage wellImage = sortedTechnicalReplicates.get(i).getWellImage();
                    Widget imageViewer = createImageViewer(wellImage, channelChooser);
                    imagePanel.add(imageViewer);
                } else
                {
                    imagePanel.add(createEmptyBox());
                }
            }
        }
        return imagePanel;
    }

    private static List<String> sortCopy(Set<String> values)
    {
        List<String> sorted = new ArrayList<String>(values);
        Collections.sort(sorted);
        return sorted;
    }

    private void addImageTableHeader(int maxReplicaNumber, LayoutContainer imagePanel)
    {
        for (int i = 0; i <= maxReplicaNumber; i++)
        {
            if (i == 0)
            {
                imagePanel.add(createEmptyBox());
            } else
            {
                imagePanel.add(createTechnicalReplicateLabel(i));
            }
        }
    }

    private Widget createTechnicalReplicateLabel(int technicalReplicateSequence)
    {
        return new Text(REPLICATE_ABBREV_DICT_MSG + " " + technicalReplicateSequence);
    }

    private Widget createEmptyBox()
    {
        return new Text();
    }

    private static int calcMaxReplicaNumber(Map<String, List<WellReplicaImage>> labelToReplicasMap)
    {
        int max = 0;
        for (List<WellReplicaImage> technicalReplicates : labelToReplicasMap.values())
        {
            max = Math.max(max, technicalReplicates.size());
        }
        return max;
    }

    private static Map<String/* label */, List<WellReplicaImage>> createSortedImageMap(
            List<WellReplicaImage> images)
    {
        Map<String, List<WellReplicaImage>> map = new HashMap<String, List<WellReplicaImage>>();
        for (WellReplicaImage image : images)
        {
            String label = image.tryGetBiologicalReplicateLabel();
            List<WellReplicaImage> technicalReplicas = map.get(label);
            if (technicalReplicas == null)
            {
                technicalReplicas = new ArrayList<WellReplicaImage>();
            }
            technicalReplicas.add(image);
            map.put(label, technicalReplicas);
        }
        sortTechnicalReplicas(map);
        return map;
    }

    private static void sortTechnicalReplicas(Map<String, List<WellReplicaImage>> map)
    {
        for (List<WellReplicaImage> technicalReplicas : map.values())
        {
            Collections.sort(technicalReplicas, new Comparator<WellReplicaImage>()
                {
                    public int compare(WellReplicaImage arg1, WellReplicaImage arg2)
                    {
                        Integer s1 = arg1.getTechnicalReplicateSequenceNumber();
                        Integer s2 = arg2.getTechnicalReplicateSequenceNumber();
                        return s1.compareTo(s2);
                    }
                });
        }
    }

    private Widget createImageViewer(final WellImage image, ChannelChooserPanel channelChooser)
    {
        assert image.tryGetImageDataset() != null;
        final ISimpleChanneledViewerFactory viewerFactory = new ISimpleChanneledViewerFactory()
            {
                public Widget create(List<String> channels)
                {
                    return WellContentDialog.createImageViewerForChannel(screeningViewContext,
                            image, ONE_IMAGE_SIZE_FACTOR_PX, channels);
                }
            };
        ChannelWidgetWithListener widgetWithListener = new ChannelWidgetWithListener(viewerFactory);
        widgetWithListener.selectionChanged(channelChooser.getSelectedValues());

        ImageDatasetParameters imageParameters = image.tryGetImageDataset().getImageParameters();
        channelChooser.addSelectionChangedListener(widgetWithListener);
        channelChooser.addCodes(imageParameters.getChannelsCodes());

        return widgetWithListener.asWidget();
    }

    private IDisposableComponent createViewer(Experiment experiment, Material material)
    {
        final LayoutContainer panel = new Viewport();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        panel.setScrollMode(Scroll.AUTO);

        Widget materialInfo = createMaterialInfo(screeningViewContext, experiment, material);
        panel.add(materialInfo, new RowData(-1, -1, PropertiesUtil.createHeaderInfoMargin()));

        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.create(screeningViewContext, new TechId(
                        experiment), new TechId(material));
        // NOTE: if the width is 100% then the vertical scrollbar of the grid is not visible
        panel.add(gridComponent.getComponent(), new RowData(0.97, 400));

        LayoutContainer imagesPanel = new LayoutContainer();
        imagesPanel.add(new Text(LOADING_IMAGES_DICT_MSG));
        panel.add(imagesPanel);
        screeningViewContext.getService().listWellImages(new TechId(material.getId()),
                new TechId(experiment.getId()), new ImagesFoundCallback(imagesPanel));

        return new IDisposableComponent()
            {

                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[0];
                }

                public Component getComponent()
                {
                    return panel;
                }

                public void dispose()
                {
                    gridComponent.dispose();
                }
            };
    }

    private static Widget createMaterialInfo(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());

        Widget headerWidget = createHeaderWithLinks(viewContext, experiment, material);
        panel.add(headerWidget, PropertiesUtil.createHeaderTitleLayoutData());

        LayoutContainer materialPropertiesPanel = createMaterialPropertiesPanel(material);
        panel.add(materialPropertiesPanel);

        return panel;
    }

    private static Widget createHeaderWithLinks(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        Widget headingWidget = createHeaderTitle(experiment, material);
        Text emptyBox = new Text();
        emptyBox.setWidth(200);
        Widget assayAnalysisSummaryLink =
                createAssayAnalysisSummaryLink(viewContext, experiment, material);
        LayoutContainer headerPanel = new LayoutContainer();
        headerPanel.setLayout(new ColumnLayout());
        headerPanel.add(headingWidget);
        headerPanel.add(emptyBox);
        headerPanel.add(assayAnalysisSummaryLink);
        return headerPanel;
    }

    private static Html createHeaderTitle(final Experiment experiment, final Material material)
    {
        String headingText =
                getMaterialType(material) + " " + getMaterialName(material) + " in assay "
                        + experiment.getCode();
        return PropertiesUtil.createHeaderTitle(headingText);
    }

    private static String getMaterialType(Material material)
    {
        String materialTypeCode = material.getMaterialType().getCode();

        return formatAsTitle(materialTypeCode);
    }

    // chnages CODE to Code
    private static String formatAsTitle(String text)
    {
        return ("" + text.charAt(0)).toUpperCase() + text.substring(1).toLowerCase();
    }

    private static String getMaterialName(Material material)
    {
        if (material.getEntityType().getCode()
                .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
        {
            String geneSymbol =
                    PropertiesUtil.tryFindProperty(material, ScreeningConstants.GENE_SYMBOLS);
            if (geneSymbol != null)
            {
                return geneSymbol;
            }
        }
        return material.getCode();
    }

    private static Widget createAssayAnalysisSummaryLink(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        // add link to feature vector summary for the experiment
        String linkUrl =
                ScreeningLinkExtractor.createExperimentAnalysisSummaryBrowserLink(experiment
                        .getPermId());
        String linkText = "Show assay " + experiment.getCode() + " summary";
        Widget linkWidget = LinkRenderer.getLinkWidget(linkText, new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    // TODO KE: We bind ourselves with
                    // the implementation of the other view instead of relying on the browser
                    FeatureVectorSummaryViewer.openTab(viewContext, experiment.getPermId());
                }
            }, linkUrl);
        return linkWidget;
    }

    private static LayoutContainer createMaterialPropertiesPanel(final Material material)
    {
        LayoutContainer propertiesPanel = new LayoutContainer();
        propertiesPanel.setLayout(new RowLayout());
        Map<String, String> additionalProperties = new HashMap<String, String>();
        additionalProperties.put(MATERIAL_ID_DICT_MSG, material.getCode());
        PropertiesUtil.addProperties(material, propertiesPanel, additionalProperties,
                ScreeningConstants.GENE_SYMBOLS);
        return propertiesPanel;
    }
}
