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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * A viewer that comprises several UI elements to produce a holistic UI for material replica feature
 * summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryViewer
{

    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, MaterialIdentifier materialIdentifier)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new ExperimentFoundCallback(screeningViewContext, materialIdentifier));
    }

    private static class ExperimentFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final MaterialIdentifier materialIdentifier;

        ExperimentFoundCallback(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
                MaterialIdentifier materialIdentifier)
        {
            super(screeningViewContext);
            this.materialIdentifier = materialIdentifier;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void process(IEntityInformationHolderWithPermId experiment)
        {
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext =
                    (IViewContext<IScreeningClientServiceAsync>) viewContext;
            screeningViewContext.getCommonService().getExperimentInfo(new TechId(experiment), new AbstractAsyncCallback<Experiment>(screeningViewContext)
                {

                    @Override
                    protected void process(Experiment result)
                    {
                        viewContext.getCommonService().getMaterialInformationHolder(materialIdentifier,
                                new MaterialFoundCallback(screeningViewContext, result));
                    }
                }); 
        }
    }

    private static class MaterialFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final Experiment experiment;

        MaterialFoundCallback(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
                Experiment experiment)
        {
            super(screeningViewContext);
            this.experiment = experiment;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void process(IEntityInformationHolderWithPermId material)
        {
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext =
                    (IViewContext<IScreeningClientServiceAsync>) viewContext;

            screeningViewContext.getService().getMaterialInfo(new TechId(material),
                    new AbstractAsyncCallback<Material>(screeningViewContext)
                        {

                            @Override
                            protected void process(Material result)
                            {
                                AbstractTabItemFactory factory =
                                        createTabFactory(screeningViewContext, experiment, result);
                                DispatcherHelper.dispatchNaviEvent(factory);
                            }
                        });
        }
    }

    private static AbstractTabItemFactory createTabFactory(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment, final Material material)
    {
        return new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.MATERIAL_REPLICA_FEATURE_SUMMARY_ACTION
                            + experiment.getCode() + material.getPermId();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent = createViewer(viewContext, experiment, material);
                    return DefaultTabItem.create(getTabTitle(), tabComponent, viewContext);
                }

                @Override
                public String tryGetLink()
                {
                    return ScreeningLinkExtractor.createMaterialReplicaFeatureSummaryBrowserLink(
                            experiment.getPermId(), material.getCode(), material.getEntityType()
                                    .getCode());
                }

                @Override
                public String getTabTitle()
                {
                    return "Material Replica Feature Summary: " + experiment.getCode();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return null;
                }

            };
    }

    private static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext, Experiment experiment,
            Material material)
    {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        
        final Widget northPanel = createNorth(viewContext, experiment, material);
        panel.add(northPanel);

        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.create(viewContext, new TechId(experiment),
                        new TechId(material));
        panel.add(gridComponent.getComponent(), new RowData(1, 1));


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

        // NOTE: this should be refactored to an external CSS style
        String headingText = "Gene " + material.getCode() + " in assay " + experiment.getCode();
        Html headingWidget = new Html(headingText);
        headingWidget.setTagName("h2");
        // headingWidget.setStyleAttribute("margin-right", "20em");
        panel.add(headingWidget, new RowData(1, -1, headingTitleMargin()));

        Widget experimentProperties =
                createPropertiesSection(viewContext, experiment.getProperties(), "Assay properties");
        panel.add(experimentProperties, new RowData(-1, -1, propertiesMargin()));

        return panel;
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
        String linkText = "Show all genes in " + experiment.getCode();
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
        panel.add(linkPanel, new RowData(-1, -1, headingTitleMargin()));

        Widget materialProperties =
                createPropertiesSection(viewContext, material.getProperties(), "Gene properties");
        panel.add(materialProperties, new RowData(-1, -1, propertiesMargin()));

        return panel;
    }

    private static Widget createPropertiesSection(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            List<IEntityProperty> properties, String sectionTitle)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        Html panelTitle = new Html(sectionTitle);
        panelTitle.setTagName("h5");
        panel.add(panelTitle, new RowData(1, -1, propertiesTitleMargin()));

        // experiment properties
        Map<String, Object> propertyMap = new LinkedHashMap<String, Object>();
        PropertiesPanelUtils.addEntityProperties(viewContext, propertyMap, properties);

        PropertyGrid propertyGrid = new PropertyGrid(viewContext, propertyMap.size());
        final IPropertyValueRenderer<IEntityProperty> renderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(GenericEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(MaterialEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(ManagedEntityProperty.class, renderer);

        propertyGrid.setProperties(propertyMap);
        panel.add(propertyGrid);

        return panel;
    }

    private static Margins headingTitleMargin()
    {
        return new Margins(20, 20, 30, 20);
    }

    private static Margins propertiesTitleMargin()
    {
        return new Margins(0, 20, 10, 20);
    }

    private static Margins propertiesMargin()
    {
        return new Margins(0, 20, 20, 0);
    }
}
