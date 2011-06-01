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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningModule;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Opens an independent tab with {@link MaterialFeaturesFromAllExperimentsComponent}.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeaturesFromAllExperimentsViewer
{
    /**
     * Fetches experiment and opens a tab with {@link MaterialReplicaSummaryComponent}.
     * 
     * @param material should be enriched with properties
     */
    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String experimentPermId, Material material)
    {
        MaterialFeaturesFromAllExperimentsViewer viewer =
                new MaterialFeaturesFromAllExperimentsViewer(screeningViewContext);
        viewer.openTab(material, null);
    }

    /**
     * Fetches material and experiment and opens a tab with {@link MaterialReplicaSummaryComponent}.
     */
    public static void openTab(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            MaterialIdentifier materialIdentifier, BasicProjectIdentifier projectIdentifierOrNull)
    {
        MaterialFeaturesFromAllExperimentsViewer viewer =
                new MaterialFeaturesFromAllExperimentsViewer(screeningViewContext);

        if (projectIdentifierOrNull == null)
        {
            screeningViewContext.getCommonService().getMaterialInformationHolder(
                    materialIdentifier,
                    viewer.createMaterialFoundCallback(ExperimentSearchByProjectCriteria
                            .createAllExperimentsForAllProjects()));
        } else
        {
            screeningViewContext.getCommonService().getProjectInfo(projectIdentifierOrNull,
                    viewer.createProjectFoundCallback(materialIdentifier));
        }
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private MaterialFeaturesFromAllExperimentsViewer(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        this.screeningViewContext = screeningViewContext;
    }

    private class MaterialFoundCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final ExperimentSearchByProjectCriteria experimentSearchCriteria;

        MaterialFoundCallback(ExperimentSearchByProjectCriteria experimentSearchCriteria)
        {
            super(screeningViewContext);

            this.experimentSearchCriteria = experimentSearchCriteria;
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
                                openTab(result, experimentSearchCriteria);
                            }
                        });
        }
    }

    private MaterialFoundCallback createMaterialFoundCallback(
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        return new MaterialFoundCallback(experimentSearchCriteria);
    }

    private AsyncCallback<Project> createProjectFoundCallback(
            final MaterialIdentifier materialIdentifier)
    {
        return new AbstractAsyncCallback<Project>(screeningViewContext)
            {
                @Override
                protected void process(Project result)
                {
                    screeningViewContext.getCommonService().getMaterialInformationHolder(
                            materialIdentifier,
                            createMaterialFoundCallback(ExperimentSearchByProjectCriteria
                                    .createAllExperimentsForProject(result)));
                }
            };
    }

    private void openTab(Material material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        AbstractTabItemFactory factory = createTabFactory(material, experimentSearchCriteria);
        DispatcherHelper.dispatchNaviEvent(factory);
    }

    private AbstractTabItemFactory createTabFactory(final Material material,
            final ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        return new AbstractTabItemFactory()
            {

                @Override
                public String getId()
                {
                    return ScreeningModule.ID
                            + ScreeningLinkExtractor.MATERIAL_FEATURES_FROM_ALL_EXPERIMENTS_ACTION
                            + material.getPermId();
                }

                @Override
                public ITabItem create()
                {
                    IDisposableComponent tabComponent =
                            MaterialFeaturesFromAllExperimentsComponent.createViewer(
                                    screeningViewContext, material, experimentSearchCriteria);
                    return DefaultTabItem.create(getTabTitle(), tabComponent, screeningViewContext);
                }

                @Override
                public String tryGetLink()
                {
                    if (experimentSearchCriteria.tryGetProject() == null)
                    {
                        return ScreeningLinkExtractor.createMaterialFeaturesFromAllExperimentsLink(
                                material.getCode(), material.getEntityType().getCode());
                    } else
                    {
                        Project project = experimentSearchCriteria.tryGetProject();
                        return ScreeningLinkExtractor.createMaterialFeaturesFromAllExperimentsLink(
                                material.getCode(), material.getEntityType().getCode(), project
                                        .getSpace().getCode(), project.getCode());
                    }
                }

                @Override
                public String getTabTitle()
                {
                    return MaterialComponentUtils.getMaterialName(material)
                            + " features in all experiments";
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier
                            .createSpecific("Material features in all experiments");
                }

            };
    }

}
