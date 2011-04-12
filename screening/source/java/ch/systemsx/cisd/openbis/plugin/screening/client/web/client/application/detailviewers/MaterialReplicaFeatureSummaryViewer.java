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

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelWidgetWithListener.ISimpleChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellImage;

/**
 * A viewer that comprises several UI elements to produce a holistic UI for material replica feature
 * summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryViewer
{
    private static final int IMAGE_SIZE_PX = 400;

    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, MaterialIdentifier materialIdentifier)
    {
        MaterialReplicaFeatureSummaryViewer viewer =
                new MaterialReplicaFeatureSummaryViewer(screeningViewContext);
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId, viewer.createExperimentFoundCallback(materialIdentifier));
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    public MaterialReplicaFeatureSummaryViewer(
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

    private class ImagesFoundCallback extends AbstractAsyncCallback<List<WellContent>>
    {
        private final LayoutContainer panel;

        ImagesFoundCallback(LayoutContainer panel)
        {
            super(screeningViewContext);
            this.panel = panel;
        }

        @Override
        protected void process(List<WellContent> images)
        {
            panel.add(createImagesViewer(images));
            panel.layout();
        }
    }

    // TODO 2011-04-12, Tomasz Pylak: correct the height
    private Widget createImagesViewer(List<? extends WellImage> images)
    {
        if (images.isEmpty())
        {
            return new Text("No images available.");
        }
        String displayTypeId = ScreeningDisplayTypeIDGenerator.EXPERIMENT_CHANNEL.createID(null);
        final IDefaultChannelState defaultChannelState =
                new DefaultChannelState(screeningViewContext, displayTypeId);
        ChannelChooserPanel channelChooser = new ChannelChooserPanel(defaultChannelState);
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());
        Html headingWidget = createHeader("<br><br><hr>" + "Images" + "<br><br>");
        panel.add(headingWidget, new RowData(1, -1));
        panel.add(channelChooser);

        LayoutContainer imagePanel = new LayoutContainer();
        imagePanel.setScrollMode(Scroll.AUTOY);
        imagePanel.setLayout(new RowLayout());
        for (WellImage wellImage : images)
        {
            Widget imageViewer = createImageViewer(wellImage, channelChooser);
            imagePanel.add(imageViewer);
        }
        double imagePanelHeight =
                Math.min(IMAGE_SIZE_PX * 2.5, images.size() * IMAGE_SIZE_PX + 100);
        panel.add(imagePanel, new RowData(1, imagePanelHeight));
        return panel;
    }

    private Widget createImageViewer(final WellImage image, ChannelChooserPanel channelChooser)
    {
        assert image.tryGetImageDataset() != null;
        final ISimpleChanneledViewerFactory viewerFactory = new ISimpleChanneledViewerFactory()
            {
                public Widget create(List<String> channels)
                {
                    return WellContentDialog.createImageViewerForChannel(screeningViewContext,
                            image, IMAGE_SIZE_PX, IMAGE_SIZE_PX, channels);
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

        final Widget northPanel = createNorth(screeningViewContext, experiment, material);
        panel.add(northPanel);

        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.create(screeningViewContext, new TechId(
                        experiment), new TechId(material));
        panel.add(gridComponent.getComponent(), new RowData(1, LayoutUtils.ONE_PAGE_GRID_HEIGHT_PX));

        screeningViewContext.getService().listWellImages(new TechId(material.getId()),
                new TechId(experiment.getId()), new ImagesFoundCallback(panel));

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

    private static Widget createNorth(IViewContext<IScreeningClientServiceAsync> viewContext,
            Experiment experiment, Material material)
    {
        HorizontalPanel panel = new HorizontalPanel();

        Widget left = createNorthLeft(viewContext, experiment, material);
        panel.add(left);

        Widget right = createNorthRight(viewContext, experiment, material);
        panel.add(right);

        return panel;
    }

    private static Widget createNorthLeft(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));

        String headingText =
                getMaterialType(material) + " " + getMaterialName(material) + " in assay "
                        + experiment.getCode();

        Html headingWidget = createHeader(headingText);
        panel.add(headingWidget, new RowData(1, -1, new Margins(0, 0, 20, 0)));

        return panel;
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

    private static Html createHeader(String headingText)
    {
        Html headingWidget = new Html(headingText);
        // NOTE: this should be refactored to an external CSS style
        headingWidget.setTagName("h1");
        return headingWidget;
    }

    private static String getMaterialName(Material material)
    {
        if (material.getEntityType().getCode()
                .equalsIgnoreCase(ScreeningConstants.GENE_PLUGIN_TYPE_CODE))
        {
            for (IEntityProperty property : material.getProperties())
            {
                if (property.getPropertyType().getCode()
                        .equalsIgnoreCase(ScreeningConstants.GENE_SYMBOLS))
                {
                    return property.tryGetAsString();
                }
            }
        }
        return material.getCode();
    }

    private static Widget createNorthRight(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));

        // add link to feature vector summary for the experiment
        String linkUrl =
                ScreeningLinkExtractor
                        .createFeatureVectorSummaryBrowserLink(experiment.getPermId());
        String linkText = "Show assay " + experiment.getCode() + " summary";
        Widget linkWidget = LinkRenderer.getLinkWidget(linkText, new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    // TODO KE: ugly, ugly, ugly !!! We bind ourselves with
                    // the implementation of the other view instead of relying on the browser
                    // report a refactoring JIRA
                    FeatureVectorSummaryViewer.openTab(viewContext, experiment.getPermId());
                }
            }, linkUrl);

        HorizontalPanel linkPanel = new HorizontalPanel();
        linkPanel.add(linkWidget);
        panel.add(linkPanel, new RowData(-1, -1, new Margins(0, 0, 20, 200)));

        return panel;
    }
}
