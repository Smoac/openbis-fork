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

import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BorderLayoutDataFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
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
            IViewContext<IScreeningClientServiceAsync> screeningViewContext =
                    (IViewContext<IScreeningClientServiceAsync>) viewContext;
            screeningViewContext.getCommonService()
                    .getMaterialInformationHolder(materialIdentifier,
                            new MaterialFoundCallback(screeningViewContext, experiment));
        }
    }

    private static class MaterialFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {

        private final IEntityInformationHolderWithPermId experiment;

        MaterialFoundCallback(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
                IEntityInformationHolderWithPermId experiment)
        {
            super(screeningViewContext);
            this.experiment = experiment;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void process(IEntityInformationHolderWithPermId material)
        {
            IViewContext<IScreeningClientServiceAsync> screeningViewContext =
                    (IViewContext<IScreeningClientServiceAsync>) viewContext;
            AbstractTabItemFactory factory =
                    createTabFactory(screeningViewContext, experiment, material);
            DispatcherHelper.dispatchNaviEvent(factory);
        }
    }

    private static AbstractTabItemFactory createTabFactory(
            final IViewContext<IScreeningClientServiceAsync> viewContext,
            final IEntityInformationHolderWithPermId experiment,
            final IEntityInformationHolderWithPermId material)
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
                    IDisposableComponent tabComponent = createUI(viewContext, experiment, material);
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

    private static IDisposableComponent createUI(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithPermId experiment,
            IEntityInformationHolderWithPermId material)
    {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new BorderLayout());
        
        BorderLayoutData northLayoutData = BorderLayoutDataFactory.create(LayoutRegion.NORTH);
        // northLayoutData.setMargins(new Margins(0));
        panel.add(createNorth(), northLayoutData);

        final IDisposableComponent gridComponent =
                MaterialReplicaFeatureSummaryGrid.create(viewContext, new TechId(experiment),
                        new TechId(material));
        BorderLayoutData centerLayoutData = BorderLayoutDataFactory.create(LayoutRegion.CENTER);
        // centerLayoutData.setMargins(new Margins(0));
        panel.add(gridComponent.getComponent(), centerLayoutData);


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

    private static Widget createNorth()
    {
        LayoutContainer result = new LayoutContainer();
        result.setAutoHeight(true);

        // result.add(new Text("this is a text panel1"));
        // result.add(new Text("this is a text panel2"));
        // result.add(new Text("this is a text panel3"));
        return result;
    }

}
