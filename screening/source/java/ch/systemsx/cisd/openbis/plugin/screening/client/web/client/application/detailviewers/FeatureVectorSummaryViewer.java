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
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertiesPanelUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;

/**
 * A grid showing feature vector summary for an experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class FeatureVectorSummaryViewer
{

    public static void openTab(final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId)
    {
        screeningViewContext.getCommonService().getEntityInformationHolder(EntityKind.EXPERIMENT,
                experimentPermId,
                new ExperimentFoundCallback(screeningViewContext));
    }

    private static class ExperimentFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        ExperimentFoundCallback(IViewContext<IScreeningClientServiceAsync> screeningViewContext) {
            super(screeningViewContext);
        }
        
        @Override
        protected void process(IEntityInformationHolderWithPermId experiment)
        {

            viewContext.getCommonService().getExperimentInfo(
                    new TechId(experiment),
                    new AbstractAsyncCallback<Experiment>(viewContext)
                        {

                            @SuppressWarnings("unchecked")
                            @Override
                            protected void process(Experiment result)
                            {
                                AbstractTabItemFactory factory =
                                        createTabFactory(
                                                (IViewContext<IScreeningClientServiceAsync>) viewContext,
                                                result);
                                DispatcherHelper.dispatchNaviEvent(factory);
                            }
                        });
        }
    }
    
    
    private static AbstractTabItemFactory createTabFactory(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final Experiment experiment)
    {
        return new AbstractTabItemFactory()
        {

            @Override
            public String getId()
            {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.FEATURE_VECTOR_SUMMARY_ACTION
                            + experiment.getCode();
            }

            @Override
            public ITabItem create()
            {
                    IDisposableComponent tabComponent = createViewer(viewContext, experiment);
                return DefaultTabItem.create(getTabTitle(), tabComponent, viewContext);
            }

            @Override
            public String tryGetLink()
            {
                return ScreeningLinkExtractor.createFeatureVectorSummaryBrowserLink(experiment.getPermId());
            }

            @Override
            public String getTabTitle()
            {
                    return "Feature Vector Summary: " + experiment.getCode();
            }

            @Override
            public HelpPageIdentifier getHelpPageIdentifier()
            {
                return null;
            }

            };
    }

    private static IDisposableComponent createViewer(
            IViewContext<IScreeningClientServiceAsync> viewContext, Experiment experiment)
    {

        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));

        Widget northPanel = createNorth(viewContext, experiment);
        panel.add(northPanel);

        final IDisposableComponent gridComponent =
                FeatureVectorSummaryGrid.create(viewContext, experiment);
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
            Experiment experiment)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));

        // NOTE: this should be refactored to an external CSS style
        String headingText = "Assay " + experiment.getCode();
        Html headingWidget = new Html(headingText);
        headingWidget.setTagName("h2");
        panel.add(headingWidget, new RowData(1, -1, headingTitleMargin()));

        Widget experimentProperties =
                createPropertiesSection(viewContext, experiment.getProperties());
        panel.add(experimentProperties, new RowData(-1, -1, propertiesMargin()));

        return panel;
    }

    private static Widget createPropertiesSection(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            List<IEntityProperty> properties)
    {
        // experiment properties
        Map<String, Object> propertyMap = new LinkedHashMap<String, Object>();
        PropertiesPanelUtils.addEntityProperties(viewContext, propertyMap, properties);

        PropertyGrid propertyGrid = new PropertyGrid(viewContext, propertyMap.size());
        // TODO KE: refactor this into an utility method
        final IPropertyValueRenderer<IEntityProperty> renderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(GenericEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(MaterialEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(ManagedEntityProperty.class, renderer);

        propertyGrid.setProperties(propertyMap);

        return propertyGrid;
    }

    private static Margins headingTitleMargin()
    {
        return new Margins(20, 20, 20, 20);
    }

    private static Margins propertiesMargin()
    {
        return new Margins(0, 20, 20, 0);
    }

}
