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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * Component displaying details for a material + a grid with the material's features extracted from
 * all available experiments.
 * 
 * @author Kaloyan Enimanev
 * @author Tomasz Pylak
 */
public class MaterialFeaturesFromAllExperimentsComponent
{
    private static final String MATERIAL_ID_DICT_MSG = "Id";

    public static IDisposableComponent createComponent(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext, Material material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        return new MaterialFeaturesFromAllExperimentsComponent(screeningViewContext).createComponent(
                material, experimentSearchCriteria);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private MaterialFeaturesFromAllExperimentsComponent(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext)
    {
        this.screeningViewContext = screeningViewContext;
    }

    private IDisposableComponent createComponent(Material material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        final LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Orientation.VERTICAL));
        panel.setScrollMode(Scroll.AUTO);

        Widget materialInfo = createMaterialInfo(screeningViewContext, material);
        panel.add(materialInfo, new RowData(-1, -1, PropertiesUtil.createHeaderInfoMargin()));

        TechId materialTechId = new TechId(material);
        final IDisposableComponent gridComponent =
                MaterialFeaturesFromAllExperimentsGrid.create(screeningViewContext, materialTechId,
                        experimentSearchCriteria);
        // NOTE: if the width is 100% then the vertical scrollbar of the grid is not visible
        panel.add(gridComponent.getComponent(), new RowData(0.97, 400));

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
            final IViewContext<IScreeningClientServiceAsync> viewContext, final Material material)
    {
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout());

        Widget headerWidget = createHeaderWithLinks(viewContext, material);
        panel.add(headerWidget, PropertiesUtil.createHeaderTitleLayoutData());

        LayoutContainer materialPropertiesPanel = createMaterialPropertiesPanel(material);
        panel.add(materialPropertiesPanel);

        return panel;
    }

    private static Widget createHeaderWithLinks(
            final IViewContext<IScreeningClientServiceAsync> viewContext, final Material material)
    {
        Widget headingWidget = createHeaderTitle(material);
        Text emptyBox = new Text();
        emptyBox.setWidth(200);
        LayoutContainer headerPanel = new LayoutContainer();
        headerPanel.setLayout(new ColumnLayout());
        headerPanel.add(headingWidget);
        headerPanel.add(emptyBox);
        return headerPanel;
    }

    private static Html createHeaderTitle(final Material material)
    {
        String headingText =
                MaterialComponentUtils.getMaterialTypeAsTitle(material) + " "
                        + MaterialComponentUtils.getMaterialName(material) + " in all assays";
        return PropertiesUtil.createHeaderTitle(headingText);
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
